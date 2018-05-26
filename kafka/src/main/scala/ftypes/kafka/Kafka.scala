package ftypes.kafka

import cats.effect.Async
import cats.implicits._
import ftypes.kafka.serializers.KafkaEncoder
import ftypes.{Component, Logging}
import org.apache.kafka.clients.producer.{KafkaProducer, Producer, ProducerRecord, RecordMetadata}
import org.apache.kafka.common.serialization.ByteArraySerializer

import scala.collection.JavaConverters._

case class Kafka[F[_]](producer: Producer[ByteArray, ByteArray])
                      (implicit F: Async[F], L: Logging[F]) extends Component[F] {

  def produce[T](message: SingleMessage[T])(implicit e: KafkaEncoder[T]): F[Unit] =
    produce(message._1, message._2)

  def produce[T](topic: String, message: T)(implicit e: KafkaEncoder[T]): F[Unit] = for {
    msg <- F.delay(new ProducerRecord[ByteArray, ByteArray](topic, e.encode(message)))
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

  def start: F[Unit] = F.pure(())

  def stop: F[Unit] = F.delay(producer.close())
}

object Kafka {
  def apply[F[_]](kafkaConf: Map[String, Object])(implicit F: Async[F], L: Logging[F]): Kafka[F] = {
    val producer: Producer[ByteArray, ByteArray] =
      new KafkaProducer(kafkaConf.asJava, new ByteArraySerializer, new ByteArraySerializer)
    apply[F](producer)
  }
}

