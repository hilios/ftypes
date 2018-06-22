package ftypes.log

/**
  * Suspend logging side-effects allowing monadic composition in terms of an effect.
  */
trait Logging[F[_]] {

  protected def log(message: LogMessage)(implicit L: Logger[F]): F[Unit] = L.log(message)

  def trace(message: String)(implicit L: Logger[F]): F[Unit] = log(Trace(message, None))
  def debug(message: String)(implicit L: Logger[F]): F[Unit] = log(Debug(message, None))
  def info (message: String)(implicit L: Logger[F]): F[Unit] = log(Info (message, None))
  def warn (message: String)(implicit L: Logger[F]): F[Unit] = log(Warn (message, None))
  def error(message: String)(implicit L: Logger[F]): F[Unit] = log(Error(message, None))

  def trace(message: String, ex: Throwable)(implicit L: Logger[F]): F[Unit] = log(Trace(message, Some(ex)))
  def debug(message: String, ex: Throwable)(implicit L: Logger[F]): F[Unit] = log(Debug(message, Some(ex)))
  def info (message: String, ex: Throwable)(implicit L: Logger[F]): F[Unit] = log(Info (message, Some(ex)))
  def warn (message: String, ex: Throwable)(implicit L: Logger[F]): F[Unit] = log(Warn (message, Some(ex)))
  def error(message: String, ex: Throwable)(implicit L: Logger[F]): F[Unit] = log(Error(message, Some(ex)))
}

object Logging {
  implicit def apply[F[_]]: Logging[F] = new Logging[F]{}
}
