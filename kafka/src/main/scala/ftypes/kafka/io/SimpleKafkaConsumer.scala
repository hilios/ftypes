package ftypes.kafka.io

import cats.effect.Async
import ftypes.kafka
import ftypes.kafka.{ByteArray, Consumer}
import org.apache.kafka.clients.consumer.{Consumer => KafkaConsumer}

case class SimpleKafkaConsumer[F[_]](consumer: KafkaConsumer[ByteArray, ByteArray])
                                    (implicit F: Async[F]) extends Consumer[F] {

  def mountConsumer(consumer: kafka.KafkaConsumer[F]): F[Unit] = {
    val fn = kafka.seal(consumer)
    ???
  }

  def start: F[Unit] = ???

  def stop: F[Unit] = ???

}
