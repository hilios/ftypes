package ftypes.log.utils

import cats.effect.Sync
import ftypes.log.utils.SilentLog.LogMessage
import ftypes.log.{Logger, Logging}

import scala.collection.mutable

case class SilentLog[F[_]]()(implicit F: Sync[F]) extends Logging[F] {

  private val logs = new mutable.MutableList[LogMessage]()

  private def accumulate(level: LogLevel, message: String, ex: Option[Throwable]): F[Unit] = F.delay {
    logs += LogMessage(level, message, ex)
    ()
  }

  def messages: List[LogMessage] = logs.toList.reverse
  
  def clear: Unit = logs.clear()

  override def trace(message: => String)(implicit logger: Logger): F[Unit] =
    accumulate(Trace, message, None)
  override def trace(message: => String, ex: Throwable)(implicit logger: Logger): F[Unit] =
    accumulate(Trace, message, None)
  override def debug(message: => String)(implicit logger: Logger): F[Unit] =
    accumulate(Debug, message, None)
  override def debug(message: => String, ex: Throwable)(implicit logger: Logger): F[Unit] =
    accumulate(Debug, message, None)
  override def info(message: => String)(implicit logger: Logger): F[Unit] =
    accumulate(Info, message, None)
  override def info(message: => String, ex: Throwable)(implicit logger: Logger): F[Unit] =
    accumulate(Info, message, None)
  override def warn(message: => String)(implicit logger: Logger): F[Unit] =
    accumulate(Warn, message, None)
  override def warn(message: => String, ex: Throwable)(implicit logger: Logger): F[Unit] =
    accumulate(Warn, message, None)
  override def error(message: => String)(implicit logger: Logger): F[Unit] =
    accumulate(Error, message, None)
  override def error(message: => String, ex: Throwable)(implicit logger: Logger): F[Unit] =
    accumulate(Error, message, None)
}

object SilentLog {
  case class LogMessage(level: LogLevel, message: String, exception: Option[Throwable])
}

