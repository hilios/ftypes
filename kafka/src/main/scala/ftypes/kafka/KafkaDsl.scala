package ftypes.kafka.consumer

import cats.effect.Sync
import ftypes.kafka.SerializerImplicits

trait KafkaDsl extends SerializerImplicits {
  
  object Topic {
    def unapply[F[_]](record: Record[F]): Option[String] = Some(record.topic)
  }

  implicit class KafkaServiceOps[F[_]](service: KafkaConsumer[F])(implicit F: Sync[F]) {
    def compile: KafkaService[F] = KafkaService.of(service)
  }
}

