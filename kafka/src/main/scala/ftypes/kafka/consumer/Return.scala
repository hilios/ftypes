package ftypes.kafka.consumer

sealed trait Return[F[_]] {
  def message: Message[F]
}

object Return {
  final case class Ack[F[_]](message: Message[F])                  extends Return[F]
  final case class Error[F[_]](message: Message[F], ex: Throwable) extends Return[F]
}
