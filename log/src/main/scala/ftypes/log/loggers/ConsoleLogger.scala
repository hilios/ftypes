package ftypes.log.loggers

import cats.Show
import cats.effect.Sync
import cats.implicits._
import ftypes.log._
import sourcecode.{Enclosing, Line}

final case class ConsoleLogger[F[_]](level: Level)(implicit F: Sync[F]) extends Logger[F] {
  def log(message: Message, enclosing: Enclosing, line: Line): F[Unit] = F.delay {
    if(message.level >= level) println(Show[Message].show(message))
  }
}

object ConsoleLogger {
  def apply[F[_] : Sync]: ConsoleLogger[F] = new ConsoleLogger[F](Level.All)
}
