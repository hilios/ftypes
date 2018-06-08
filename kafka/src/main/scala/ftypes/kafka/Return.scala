package ftypes.kafka

sealed trait Return[F[_]] {
  def record: Record[F]
}

object Return {
  final case class Ack[F[_]](record: Record[F])                  extends Return[F]
  final case class NotFound[F[_]](record: Record[F])             extends Return[F]
  final case class Error[F[_]](record: Record[F], ex: Throwable) extends Return[F]
}
