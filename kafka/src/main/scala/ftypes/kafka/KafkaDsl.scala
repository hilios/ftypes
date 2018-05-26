package ftypes.kafka

import cats.effect.Sync
import ftypes.kafka.serializers.{KafkaDecoder, KafkaEncoder}

trait KafkaDsl {
  
  object Topic {
    def unapply(record: DefaultConsumerRecord): Option[String] = Some(record.topic())
  }

  implicit class ConsumerRecordOps(record: DefaultConsumerRecord) {
    def keyAs[T](implicit D: KafkaDecoder[T]): T = D.decode(record.key)
    
    def as[T](implicit D: KafkaDecoder[T]): T = D.decode(record.value)
  }
}

object KafkaDsl {
  trait KafkaMessage[F[_]] {
    def keyAs[T](implicit D: KafkaDecoder[T]): F[T]

    def as[T](implicit D: KafkaDecoder[T]): F[T]

    def topic: String

    def partition: Int

    def offset: Long

    def underlying: DefaultConsumerRecord
  }

  object KafkaMessage {
    def apply[F[_]](record: DefaultConsumerRecord)(implicit F: Sync[F]) = new KafkaMessage[F] {
      def keyAs[T](implicit D: KafkaDecoder[T]): F[T] = F.delay(D.decode(record.key()))

      def as[T](implicit D: KafkaDecoder[T]): F[T] = F.delay(D.decode(record.value()))

      def topic: String = record.topic()

      def partition: Int = record.partition()

      def offset: Long = record.offset()

      def underlying: DefaultConsumerRecord = record
    }

    def apply[F[_], T](topic: String, message: T)(implicit E: KafkaEncoder[T], F: Sync[F]) = {
      val record = new DefaultConsumerRecord(topic, 0, 0L, Array.empty[Byte], E.encode(message))
      apply[F](record)
    }
  }
}
