package ftypes.log

import cats.Show
import cats.effect.Sync
import cats.implicits._

case class ConsoleLogger[F[_]](level: Level)(implicit F: Sync[F], S: Show[Message]) extends Logging[F] {
  def log(logger: Logger[F], message: Message): F[Unit] = F.delay {
    if(message.level >= level) println(S.show(message))
  }
}

object ConsoleLogger {
  def apply[F[_]](implicit F: Sync[F], S: Show[Message]): ConsoleLogger[F] = ConsoleLogger(Level.All)(F, S)
}
