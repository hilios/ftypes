package ftypes.kafka.consumer

import cats.effect.Sync

trait KafkaDsl {
  
  object Topic {
    def unapply[F[_]](message: Message[F]): Option[String] = Some(message.topic)
  }

  implicit class KafkaServiceOps[F[_]](service: KafkaService[F])(implicit F: Sync[F]) {
    def compile: KafkaConsumer[F] = KafkaConsumer.of(service)
  }
}

