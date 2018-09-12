package ftypes.log

import cats.effect.Sync
import cats.implicits._

import scala.collection.mutable

case class SilentLogger[F[_]](level: LogLevel)(implicit F: Sync[F]) extends Logger[F] {
  private val _messages = new mutable.MutableList[LogMessage]()

  def messages: List[LogMessage] = _messages.toList.reverse

  def clear(): Unit = _messages.clear()

  def log(cls: EnclosingClass, message: LogMessage): F[Unit] = F.delay {
    if (message.level >= level) _messages += message
  }
}

object SilentLogger {
  def apply[F[_]](implicit F: Sync[F]): SilentLogger[F] = SilentLogger(LogLevel.All)(F)
}
