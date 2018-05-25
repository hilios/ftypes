package ftypes.test.logging

import cats.effect.Effect
import ftypes.Logging
import ftypes.LoggingMacros

case class PrintLog[F[_]](level: LogLevel)(implicit F: Effect[F]) extends Logging[F] {

  private def render(level: LogLevel, message: => String): F[Unit] = F.delay {
    if (level.weight >= this.level.weight) println(s"${level.color}${level.name} $message${Console.RESET}")
  }

  private def render(level: LogLevel, message: => String, ex: Throwable): F[Unit] = F.delay {
    if (level.weight >= this.level.weight) {
      println(s"${level.color}${level.name} $message")
      ex.printStackTrace()
      println(Console.RESET)
    }
  }

  override def get: Logging[F] = macro LoggingMacros.selfMaterializer[F]

  override def trace(message: => String): F[Unit] =
    render(Trace, message)
  override def trace(message: => String, ex: Throwable): F[Unit] =
    render(Trace, message, ex)
  override def debug(message: => String): F[Unit] =
    render(Debug, message)
  override def debug(message: => String, ex: Throwable): F[Unit] =
    render(Debug, message, ex)
  override def info(message: => String): F[Unit] =
    render(Info, message)
  override def info(message: => String, ex: Throwable): F[Unit] =
    render(Info, message, ex)
  override def warn(message: => String): F[Unit] =
    render(Warn, message)
  override def warn(message: => String, ex: Throwable): F[Unit] =
    render(Warn, message, ex)
  override def error(message: => String): F[Unit] =
    render(Error, message)
  override def error(message: => String, ex: Throwable): F[Unit] =
    render(Error, message, ex)
}

object PrintLog { self =>
  def apply[F[_]](implicit F: Effect[F]): PrintLog[F] = new PrintLog(Trace)
}
