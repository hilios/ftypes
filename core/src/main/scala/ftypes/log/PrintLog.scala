package ftypes.log

import cats.Show
import cats.effect.Sync
import cats.implicits._

case class PrintLog[F[_]](level: LogLevel)(implicit F: Sync[F], S: Show[LogMessage]) extends Logging[F] {
  override protected def log(message: LogMessage)(implicit L: Logger[F]): F[Unit] = F.delay {
    if(message.level >= level) println(S.show(message))
  }
}

object PrintLog {
  def apply[F[_]](implicit F: Sync[F], S: Show[LogMessage]): PrintLog[F] = PrintLog(All)(F, S)
}
