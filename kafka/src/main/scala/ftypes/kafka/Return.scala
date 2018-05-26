package ftypes.kafka

sealed trait Return[F[_]] {
  def message: KafkaMessage[F]
}

object Return {
  final case class Ack[F[_]](message: KafkaMessage[F])   extends Return[F]
  final case class Error[F[_]](message: KafkaMessage[F]) extends Return[F]
}
