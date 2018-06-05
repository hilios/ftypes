package ftypes.kafka

import cats.effect.Sync

trait KafkaDsl extends SerializerImplicits {
  
  object Topic {
    def unapply[F[_]](record: Record[F]): Option[String] = Some(record.topic)
  }

  implicit class KafkaCompileOps[F[_]](consumer: KafkaConsumer[F])(implicit F: Sync[F]) {
    def seal: KafkaService[F] = ftypes.kafka.seal(consumer)
  }
}

