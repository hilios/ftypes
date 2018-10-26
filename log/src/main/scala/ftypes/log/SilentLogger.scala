package ftypes.log

import cats.effect.Sync
import cats.implicits._

import scala.collection.mutable

case class SilentLogger[F[_]](level: Level)(implicit F: Sync[F]) extends Logging[F] {
  private val _messages = new mutable.MutableList[Message]()

  def messages: List[Message] = _messages.toList.reverse

  def clear(): Unit = _messages.clear()

  def log(cls: UnderlyingLogger[F], message: Message): F[Unit] = F.delay {
    if (message.level >= level) _messages += message
  }
}

object SilentLogger {
  def apply[F[_]](implicit F: Sync[F]): SilentLogger[F] = SilentLogger(Level.All)(F)
}
