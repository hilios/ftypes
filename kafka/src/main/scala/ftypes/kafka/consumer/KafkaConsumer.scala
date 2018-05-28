package ftypes.kafka.consumer

import cats.data.{Kleisli, OptionT}
import cats.effect.Sync
import ftypes.kafka.consumer.Return.{Ack, Error}

object KafkaConsumer {

  def apply[F[_]](f: Message[F] => F[Return[F]]): KafkaConsumer[F, F] = Kleisli(f)

  def lift[F[_]](pf: PartialFunction[Message[F], F[Unit]])(implicit F: Sync[F]): KafkaConsumer[F, F] = {
    Kleisli(message => {
      lazy val error: Return[F] = Error(message, new RuntimeException(s"Consumer for topic ${message.topic} was not found"))

      val consumer = pf
        .andThen(OptionT.liftF(_))
        .applyOrElse(message, Function.const(OptionT.none))
        .map(_ => Ack(message))
        .getOrElse(error)

      F.recover(consumer) {
        case ex: Throwable => Error(message, ex)
      }
    })
  }

  def of[F[_]](service: KafkaService[F])(implicit F: Sync[F]): KafkaConsumer[F, F] = Kleisli(message => {
    lazy val error: Return[F] = Error(message, new RuntimeException(s"Consumer for topic ${message.topic} was not found"))

    F.recover(service(message).map(_ => Ack(message)).getOrElse(error)) {
      case ex: Throwable => Error(message, ex)
    }
  })
}
