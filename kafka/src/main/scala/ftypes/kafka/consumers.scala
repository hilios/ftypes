package ftypes.kafka

import cats.data.{Kleisli, OptionT}
import cats.effect.Sync

object consumers {

  type KafkaService[F[_], G[_]] = Kleisli[F, KafkaMessage[G], Return[G]]

  object KafkaService {
    def apply[F[_]](f: KafkaMessage[F] => F[Return[F]]): KafkaService[F, F] = Kleisli(f)
  }

  type KafkaConsumer[F[_]] = KafkaService[OptionT[F, ?], F]

  object KafkaConsumer {
    def apply[F[_]: Sync](pf: PartialFunction[KafkaMessage[F], F[Return[F]]]): KafkaConsumer[F] = {
      Kleisli(record => pf.andThen(OptionT.liftF(_)).applyOrElse(record, Function.const(OptionT.none)))
    }
  }
}
