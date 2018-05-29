package ftypes.kafka.producer

import ftypes.kafka.SingleMessage
import ftypes.kafka.serializers.KafkaEncoder


trait KafkaProducer[F[_]] {
  def produce[T](message: SingleMessage[T])(implicit E: KafkaEncoder[T]): F[Unit]

  def produce[T](topic: String, message: T)(implicit E: KafkaEncoder[T]): F[Unit]

  //def produce[K, V](topic: String, key: K, message: V)(implicit K: KafkaEncoder[K], V: KafkaEncoder[V]): F[Unit]

  //def produce[K, V](topic: String, partition: Int, key: K, message: V)(implicit K: KafkaEncoder[K], V: KafkaEncoder[V]): F[Unit]

  def shutdown: F[Unit]
}

