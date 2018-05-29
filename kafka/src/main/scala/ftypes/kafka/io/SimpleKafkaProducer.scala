package ftypes.kafka.io

import cats.effect.Async
import cats.implicits._
import ftypes.Logging
import ftypes.kafka.producer.KafkaProducer
import ftypes.kafka.serializers.KafkaEncoder
import ftypes.kafka.{DefaultProducer, DefaultProducerRecord, SingleMessage}
import org.apache.kafka.clients.producer.{RecordMetadata, KafkaProducer => ApacheKafkaProducer}
import org.apache.kafka.common.serialization.ByteArraySerializer

import scala.collection.JavaConverters._

case class SimpleKafkaProducer[F[_]](producer: DefaultProducer)
                               (implicit F: Async[F], L: Logging[F]) extends KafkaProducer[F] {

  def produce[T](message: SingleMessage[T])(implicit E: KafkaEncoder[T]): F[Unit] =
    produce(message._1, message._2)

  def produce[T](topic: String, message: T)(implicit E: KafkaEncoder[T]): F[Unit] = for {
    msg <- F.delay(new DefaultProducerRecord(topic, E.encode(message)))
    _   <- L.debug(s"Producing kafka message to $topic with payload $message")
    _   <- F.async[RecordMetadata] { cb =>
      producer.send(msg, (metadata: RecordMetadata, exception: Exception) => {
        if (exception == null) cb(Right(metadata))
        else cb(Left(exception))
      })
      ()
    } recoverWith {
      case ex: Throwable =>
        L.error(s"Could not produce message to $topic with payload $message", ex) *> F.raiseError(ex)
    }
  } yield ()

  def shutdown: F[Unit] = F.delay(producer.close())
}

object SimpleKafkaProducer {
  def apply[F[_]](conf: Map[String, Object])(implicit F: Async[F], L: Logging[F]): KafkaProducer[F] = {
    apply(new ApacheKafkaProducer(conf.asJava, new ByteArraySerializer, new ByteArraySerializer))
  }
}

