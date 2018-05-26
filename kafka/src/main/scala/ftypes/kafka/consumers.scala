package ftypes.kafka

import cats.data.{Kleisli, OptionT}
import cats.effect.Sync

object consumers {

  type KafkaService[F[_]] = Kleisli[F, DefaultConsumerRecord, Return]

  object KafkaService {
    def apply[F[_]](f: DefaultConsumerRecord => F[Return]): KafkaService[F] = Kleisli(f)
  }

  type KafkaConsumer[F[_]] = KafkaService[OptionT[F, ?]]

  object KafkaConsumer {
    def apply[F[_]: Sync](pf: PartialFunction[DefaultConsumerRecord, F[Return]]): KafkaConsumer[F] = {
      Kleisli(record => pf.andThen(OptionT.liftF(_)).applyOrElse(record, Function.const(OptionT.none)))
    }
  }
}
