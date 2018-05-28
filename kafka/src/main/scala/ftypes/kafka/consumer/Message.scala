package ftypes.kafka.consumer

import cats.effect.Sync
import ftypes.kafka.DefaultConsumerRecord
import ftypes.kafka.serializers.{KafkaDecoder, KafkaEncoder}

trait Message[F[_]] {
  def keyAs[T](implicit D: KafkaDecoder[T]): F[T]

  def as[T](implicit D: KafkaDecoder[T]): F[T]

  def topic: String

  def partition: Int

  def offset: Long

  def underlying: DefaultConsumerRecord
}

object Message {

  /**
    * Creates a KafkaMessage from a ConsumerRecord.
    */
  def apply[F[_]](record: DefaultConsumerRecord)
                 (implicit F: Sync[F]): Message[F] = new Message[F] {

    def keyAs[T](implicit D: KafkaDecoder[T]): F[T] = F.delay(D.decode(record.key()))

    def as[T](implicit D: KafkaDecoder[T]): F[T] = F.delay(D.decode(record.value()))

    def topic: String = record.topic()

    def partition: Int = record.partition()

    def offset: Long = record.offset()

    def underlying: DefaultConsumerRecord = record
  }

  def apply[F[_], T](topic: String, message: T)(implicit F: Sync[F], E: KafkaEncoder[T]): Message[F] = {
    val record = new DefaultConsumerRecord(topic, 0, 0L, Array.empty[Byte], E.encode(message))
    apply[F](record)
  }
}
