package ftypes.log

import cats.Applicative
import cats.effect.Sync
import cats.kernel.Monoid
import ftypes.log.LogMessage._

/**
  * Suspend logging side-effects allowing monadic composition in terms of an effect.
  */
trait Logger[F[_]] { self =>

  protected def log(cls: EnclosingClass, message: LogMessage): F[Unit]

  def trace(message: String)(implicit cls: EnclosingClass): F[Unit] = log(cls, TraceLog(message, None))
  def debug(message: String)(implicit cls: EnclosingClass): F[Unit] = log(cls, DebugLog(message, None))
  def info (message: String)(implicit cls: EnclosingClass): F[Unit] = log(cls, InfoLog (message, None))
  def warn (message: String)(implicit cls: EnclosingClass): F[Unit] = log(cls, WarnLog (message, None))
  def error(message: String)(implicit cls: EnclosingClass): F[Unit] = log(cls, ErrorLog(message, None))

  def trace(message: String, ex: Throwable)(implicit cls: EnclosingClass): F[Unit] = log(cls, TraceLog(message, Some(ex)))
  def debug(message: String, ex: Throwable)(implicit cls: EnclosingClass): F[Unit] = log(cls, DebugLog(message, Some(ex)))
  def info (message: String, ex: Throwable)(implicit cls: EnclosingClass): F[Unit] = log(cls, InfoLog (message, Some(ex)))
  def warn (message: String, ex: Throwable)(implicit cls: EnclosingClass): F[Unit] = log(cls, WarnLog (message, Some(ex)))
  def error(message: String, ex: Throwable)(implicit cls: EnclosingClass): F[Unit] = log(cls, ErrorLog(message, Some(ex)))

  def andThen(that: Logger[F])(implicit ap: Applicative[F]): Logger[F] =
    (cls: EnclosingClass, message: LogMessage) => ap.productR(self.log(cls, message))(that.log(cls, message))
}

object Logger {
  implicit def apply[F[_]](implicit F: Sync[F]): Logger[F] = Slf4jLogger[F]

  implicit def monoidInstanceForLogger[F[_]](implicit F: Applicative[F]): Monoid[Logger[F]] = new Monoid[Logger[F]] {
    def empty: Logger[F] =
      (_: EnclosingClass, _: LogMessage) => F.pure(())

    def combine(x: Logger[F], y: Logger[F]): Logger[F] =
      x andThen y
  }
}
