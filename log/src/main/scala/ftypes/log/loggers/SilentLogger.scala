package ftypes.log.loggers

import cats.effect.Sync
import cats.implicits._
import ftypes.log.{Level, Logger, Message}
import sourcecode.{Enclosing, Line}

import scala.collection.mutable

final case class SilentLogger[F[_]](level: Level)(implicit F: Sync[F]) extends Logger[F] {
  val messages: mutable.Queue[Message] = mutable.Queue()

  def log(message: Message, enclosing: Enclosing, line: Line): F[Unit] = F.delay {
    if(message.level >= level) messages.enqueue(message)
  }
}

object SilentLogger {
  def apply[F[_]: Sync]: SilentLogger[F] = SilentLogger[F](Level.All)
}
