package ftypes.kafka

import cats.data.{Kleisli, OptionT}

package object consumer {
  type KafkaApp[F[_], G[_]] = Kleisli[F, Message[G], Return[G]]
  type KafkaConsumer[F[_]] = KafkaApp[F, F]
  type KafkaService[F[_]] = Kleisli[OptionT[F, ?], Message[F], Unit]
}
