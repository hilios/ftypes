package ftypes.log

import cats.Show
import cats.effect.Sync
import cats.implicits._

case class ConsoleLogger[F[_]](level: LogLevel)(implicit F: Sync[F], S: Show[LogMessage]) extends Logger[F] {
  def log(cls: EnclosingClass, message: LogMessage): F[Unit] = F.delay {
    if(message.level >= level) println(S.show(message))
  }
}

object ConsoleLogger {
  def apply[F[_]](implicit F: Sync[F], S: Show[LogMessage]): ConsoleLogger[F] = ConsoleLogger(LogLevel.All)(F, S)
}
