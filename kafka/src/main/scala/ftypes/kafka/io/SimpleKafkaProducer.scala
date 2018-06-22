package ftypes.kafka.io

import cats.effect.Async
import cats.implicits._
import ftypes.kafka.{ByteArray, DefaultProducerRecord, Producer}
import ftypes.log.Logging
import org.apache.kafka.clients.producer.{RecordMetadata, Producer => KafkaProducer}

case class SimpleKafkaProducer[F[_]](producer: KafkaProducer[ByteArray, ByteArray])
                                     (implicit F: Async[F], L: Logging[F]) extends Producer[F] {

  def produce(record: DefaultProducerRecord): F[Unit] = for {
    _ <- L.debug(s"Producing message to topic ${record.topic()}")
    _ <- F.async[RecordMetadata] { cb =>
      producer.send(record, (metadata: RecordMetadata, exception: Exception) => {
        if (exception == null) cb(Right(metadata))
        else cb(Left(exception))
      })
      ()
    }
  } yield ()

  def stop: F[Unit] = F.delay(producer.close())
}
