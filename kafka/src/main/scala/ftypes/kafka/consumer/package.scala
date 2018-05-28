package ftypes.kafka

import cats.data.{Kleisli, OptionT}

package object consumer {
  type KafkaConsumer[F[_], G[_]] = Kleisli[F, Message[G], Return[G]]
  type KafkaService[F[_]] = Kleisli[OptionT[F, ?], Message[F], Any]
}
