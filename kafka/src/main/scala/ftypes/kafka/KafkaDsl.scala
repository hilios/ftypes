package ftypes.kafka

import cats.data.Kleisli
import cats.effect.Sync
import ftypes.kafka.Return.{Ack, Error}
import ftypes.kafka.consumers.{KafkaConsumer, KafkaService}

trait KafkaDsl {
  
  object Topic {
    def unapply[F[_]](message: KafkaMessage[F]): Option[String] = Some(message.topic)
  }

  implicit class KafkaConsumerOps[F[_]](consumers: KafkaConsumer[F])(implicit F: Sync[F]) {
    def compile: KafkaService[F, F] = {
      Kleisli(message => {
        lazy val error: Return[F] = Error(message, new RuntimeException(s"Consumer for topic ${message.topic} was not found"))
        consumers(message).map(_ => Ack(message)).getOrElse(error)
      })
    }
  }
}

