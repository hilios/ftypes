package ftypes.kafka.consumer

sealed trait Return[F[_]] {
  def message: Record[F]
}

object Return {
  final case class Ack[F[_]](message: Record[F])                  extends Return[F]
  final case class NotFound[F[_]](message: Record[F])             extends Return[F]
  final case class Error[F[_]](message: Record[F], ex: Throwable) extends Return[F]
}
