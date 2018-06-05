package ftypes.kafka.consumer

import cats.data.{Kleisli, OptionT}
import cats.effect.Sync
import ftypes.kafka.Record

object KafkaConsumer {
  def apply[F[_]: Sync](pf: PartialFunction[Record[F], F[Unit]]): KafkaConsumer[F] = {
    Kleisli(record => pf.andThen(OptionT.liftF(_)).applyOrElse(record, Function.const(OptionT.none)))
  }
}
