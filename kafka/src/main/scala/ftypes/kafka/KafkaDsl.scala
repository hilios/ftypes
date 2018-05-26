package ftypes.kafka

import ftypes.kafka.serializers.KafkaDecoder

trait KafkaDsl {
  
  object Topic {
    def unapply[F[_]](message: KafkaMessage[F]): Option[String] = Some(message.topic)
  }

  implicit class ConsumerRecordOps(record: DefaultConsumerRecord) {
    def keyAs[T](implicit D: KafkaDecoder[T]): T = D.decode(record.key)
    
    def as[T](implicit D: KafkaDecoder[T]): T = D.decode(record.value)
  }
}

