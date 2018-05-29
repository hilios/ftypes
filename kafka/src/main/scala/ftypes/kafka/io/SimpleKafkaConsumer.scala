package ftypes.kafka.io

import java.util

import cats.data.{Ior, NonEmptyList => Nel}
import cats.effect.{Async, Concurrent}
import cats.implicits._
import ftypes.Logging
import ftypes.kafka.DefaultConsumer
import ftypes.kafka.consumer.Return.{Ack, Error}
import ftypes.kafka.consumer.{KafkaConsumer, KafkaDsl, KafkaService, Message}
import org.apache.kafka.clients.consumer.{OffsetAndMetadata, KafkaConsumer => ApacheKafkaConsumer}
import org.apache.kafka.common.TopicPartition
import org.apache.kafka.common.serialization.ByteArrayDeserializer

import scala.collection.JavaConverters._
import scala.concurrent.ExecutionContext

case class SimpleKafkaConsumer[F[_]](kafkaConsumer: DefaultConsumer)
                                    (implicit F: Async[F], L: Logging[F]) extends KafkaDsl {

  private def logErrors(errors: List[Error[F]]): F[Unit] = errors.map { err =>
    val message = err.message
    L.error(s"Could not process message offset ${message.offset} from topic ${message.topic}", err.ex)
  }.reduce(_ *> _)

  private def commitOffset(messages: List[Message[F]]): F[Unit] = F.async[Unit] { cb =>
    kafkaConsumer.commitAsync((_: util.Map[TopicPartition, OffsetAndMetadata], exception: Exception) => {
      if (exception == null) cb(Right(()))
      else cb(Left(exception))
    })
  }

  private def poll(consumer: KafkaConsumer[F]): F[Unit Ior Unit] = {
    val records = kafkaConsumer.poll(Long.MaxValue)
    records.iterator.asScala.map { record =>
      val msg = Message(record)
      consumer.apply(msg).map {
        case out: Ack[F]   => Ior.right(Nel.one(out))
        case out: Error[F] => Ior.left(Nel.one(out))
      }
    }.toList.sequence.map(_.reduce(_ |+| _).bitraverse(
      e => logErrors(e.toList),
      m => commitOffset(m.map(_.message).toList)
    )).flatten
  }

  def mountService(consumer: KafkaService[F])(implicit ec: ExecutionContext): F[Unit] = for {
    run   <- F.pure(consumer.compile)
    fiber <- Async.shift(ec) *> F.delay {
      while (true) {
        poll(run)
      }
    }
  } yield fiber


  def shutdown: F[Unit] = F.delay(kafkaConsumer.close())
}

object SimpleKafkaConsumer {

  def apply[F[_]](conf: Map[String, Object])(implicit F: Concurrent[F], L: Logging[F]): SimpleKafkaConsumer[F] = {
    apply(new ApacheKafkaConsumer(conf.asJava, new ByteArrayDeserializer, new ByteArrayDeserializer))
  }
}
