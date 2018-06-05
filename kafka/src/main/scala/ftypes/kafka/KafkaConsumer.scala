package ftypes.kafka

import cats.data.{Kleisli, OptionT}
import cats.effect.Sync

object KafkaConsumer {
  def apply[F[_]: Sync](pf: PartialFunction[Record[F], F[Unit]]): KafkaConsumer[F] = {
    Kleisli(record => pf.andThen(OptionT.liftF(_)).applyOrElse(record, Function.const(OptionT.none)))
  }
}
