package ftypes.test.logging

import cats.effect.Sync
import ftypes.Logging
import ftypes.LoggingMacros
import ftypes.test.logging.SilentLog.LogMessage

import scala.collection.mutable

case class SilentLog[F[_]]()(implicit F: Sync[F]) extends Logging[F] {

  private var logs = new mutable.MutableList[LogMessage]()

  private def accumulate(level: LogLevel, message: String, ex: Option[Throwable]): F[Unit] = F.delay {
    logs += LogMessage(level, message, ex)
    ()
  }

  def messages: List[LogMessage] = logs.toList.reverse

  override def get: Logging[F] = macro LoggingMacros.selfMaterializer[F]

  override def trace(message: => String): F[Unit] =
    accumulate(Trace, message, None)
  override def trace(message: => String, ex: Throwable): F[Unit] =
    accumulate(Trace, message, None)
  override def debug(message: => String): F[Unit] =
    accumulate(Debug, message, None)
  override def debug(message: => String, ex: Throwable): F[Unit] =
    accumulate(Debug, message, None)
  override def info(message: => String): F[Unit] =
    accumulate(Info, message, None)
  override def info(message: => String, ex: Throwable): F[Unit] =
    accumulate(Info, message, None)
  override def warn(message: => String): F[Unit] =
    accumulate(Warn, message, None)
  override def warn(message: => String, ex: Throwable): F[Unit] =
    accumulate(Warn, message, None)
  override def error(message: => String): F[Unit] =
    accumulate(Error, message, None)
  override def error(message: => String, ex: Throwable): F[Unit] =
    accumulate(Error, message, None)
}

object SilentLog {
  case class LogMessage(level: LogLevel, message: String, exception: Option[Throwable])
}

