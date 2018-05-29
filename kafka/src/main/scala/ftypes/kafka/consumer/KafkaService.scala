package ftypes.kafka.consumer

import cats.data.{Kleisli, OptionT}
import cats.effect.Sync

object KafkaService {
  def apply[F[_]: Sync](pf: PartialFunction[Message[F], F[Unit]]): KafkaService[F] = {
    Kleisli(record => pf.andThen(OptionT.liftF(_)).applyOrElse(record, Function.const(OptionT.none)))
  }
}
