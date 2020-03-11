package ftypes.log

import sourcecode.{Enclosing, Line}

trait Log[F[_]] {
  def log(message: Message, enclosing: Enclosing, line: Line): F[Unit]

  @inline def trace(message: String)(implicit enclosing: Enclosing, line: Line): F[Unit] =
    log(Level.Trace(message, None), enclosing, line)
  
  @inline def debug(message: String)(implicit enclosing: Enclosing, line: Line): F[Unit] =
    log(Level.Debug(message, None), enclosing, line)

  @inline def info(message: String)(implicit enclosing: Enclosing, line: Line): F[Unit]  =
    log(Level.Info(message, None), enclosing, line)

  @inline def warn(message: String)(implicit enclosing: Enclosing, line: Line): F[Unit]  =
    log(Level.Warn(message, None), enclosing, line)

  @inline def error(message: String)(implicit enclosing: Enclosing, line: Line): F[Unit] =
    log(Level.Error(message, None), enclosing, line)

  @inline def trace(message: String, ex: Throwable)(implicit enclosing: Enclosing, line: Line): F[Unit] =
    log(Level.Trace(message, Some(ex)), enclosing, line)

  @inline def debug(message: String, ex: Throwable)(implicit enclosing: Enclosing, line: Line): F[Unit] =
    log(Level.Debug(message, Some(ex)), enclosing, line)

  @inline def info(message: String, ex: Throwable)(implicit enclosing: Enclosing, line: Line): F[Unit] =
    log(Level.Info(message, Some(ex)), enclosing, line)

  @inline def warn(message: String, ex: Throwable)(implicit enclosing: Enclosing, line: Line): F[Unit] =
    log(Level.Warn(message, Some(ex)), enclosing, line)

  @inline def error(message: String, ex: Throwable)(implicit enclosing: Enclosing, line: Line): F[Unit] =
    log(Level.Error(message, Some(ex)), enclosing, line)
}

object Log {

  implicit def apply[F[_]](implicit L: Logger[F]): Log[F] = new Log[F] {
    def log(message: Message, enclosing: Enclosing, line: Line): F[Unit] = L.log(message, enclosing, line)
  }
}
