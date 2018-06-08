package ftypes.kafka.io

import cats.effect.Async
import cats.implicits._
import ftypes.kafka.{ByteArray, DefaultProducerRecord, Producer}
import org.apache.kafka.clients.producer.{MockProducer, RecordMetadata, Producer => KafkaProducer}
import org.apache.kafka.common.serialization.ByteArraySerializer

case class SimpleKafkaProducer[F[_]](producer: KafkaProducer[ByteArray, ByteArray])
                                     (implicit F: Async[F]) extends Producer[F] {
  
  def produce(record: DefaultProducerRecord): F[Unit] = for {
    _   <- F.async[RecordMetadata] { cb =>
      producer.send(record, (metadata: RecordMetadata, exception: Exception) => {
        if (exception == null) cb(Right(metadata))
        else cb(Left(exception))
      })
      ()
    }
  } yield ()

  def stop: F[Unit] = F.delay(producer.close())
}

object SimpleKafkaProducer {
  def mock = new MockProducer(true, new ByteArraySerializer, new ByteArraySerializer)
}
