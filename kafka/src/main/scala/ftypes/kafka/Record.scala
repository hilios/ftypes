package ftypes.kafka

import cats.effect.Sync
import ftypes.kafka.serializers.{KafkaDecoder, KafkaEncoder}

trait Record[F[_]] {
  def keyAs[T](implicit d: KafkaDecoder[T]): F[T]

  def as[T](implicit d: KafkaDecoder[T]): F[T]

  def topic: String

  def partition: Int

  def offset: Long

  def underlying: DefaultConsumerRecord
}

object Record {

  /**
    * Creates a Record from a Kafka ConsumerRecord instance.
    */
  def apply[F[_]](record: DefaultConsumerRecord)(implicit F: Sync[F]): Record[F] = new Record[F] {

    def keyAs[T](implicit d: KafkaDecoder[T]): F[T] = F.delay(d.decode(record.key()))

    def as[T](implicit d: KafkaDecoder[T]): F[T] = F.delay(d.decode(record.value()))

    def topic: String = record.topic()

    def partition: Int = record.partition()

    def offset: Long = record.offset()

    def underlying: DefaultConsumerRecord = record
  }

  def apply[F[_], T](topic: String, message: T)(implicit F: Sync[F], E: KafkaEncoder[T]): Record[F] = {
    val record = new DefaultConsumerRecord(topic, 0, 0L, Array.empty[Byte], E.encode(message))
    apply[F](record)
  }
}
