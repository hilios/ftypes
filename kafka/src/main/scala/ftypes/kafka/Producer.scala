package ftypes.kafka

import ftypes.kafka.serializers.KafkaEncoder

trait Producer[F[_]] {
  def produce[V](topicAndMessage: SingleMessage[V])(implicit v: KafkaEncoder[V]): F[Unit] = {
    val (topic, message) = topicAndMessage
    produce(topic, message)
  }

  def produce[V](topic: String, message: V)(implicit v: KafkaEncoder[V]): F[Unit] = {
    val record = new DefaultProducerRecord(topic, v.encode(message))
    produce(record)
  }

  def produce[K, V](topic: String, key: K, message: V)(implicit k: KafkaEncoder[K], v: KafkaEncoder[V]): F[Unit] = {
    val record = new DefaultProducerRecord(topic, k.encode(key), v.encode(message))
    produce(record)
  }

  def produce[K, V](topic: String, partition: Int, key: K, message: V)
                   (implicit k: KafkaEncoder[K], v: KafkaEncoder[V]): F[Unit] = {
    val record = new DefaultProducerRecord(topic, partition, k.encode(key), v.encode(message))
    produce(record)
  }

  def produce(record: DefaultProducerRecord): F[Unit]

  def stop: F[Unit]
}

