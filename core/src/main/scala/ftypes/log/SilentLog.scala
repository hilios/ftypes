package ftypes.log

import cats.effect.Sync
import cats.implicits._

import scala.collection.mutable

case class SilentLog[F[_]](level: LogLevel = All)(implicit F: Sync[F]) extends Logging[F] {
  private val _messages = new mutable.MutableList[LogMessage]()

  def messages: List[LogMessage] = _messages.toList.reverse

  def clear(): Unit = _messages.clear()

  override def log(message: LogMessage)(implicit L: Logger[F]): F[Unit] = F.delay {
    if (message.level >= level) _messages += message
  }
}

object SilentLog {
  def apply[F[_]](implicit F: Sync[F]): SilentLog[F] = SilentLog(All)(F)
}
