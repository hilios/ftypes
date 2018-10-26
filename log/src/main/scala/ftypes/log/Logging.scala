package ftypes.log

import cats.{Applicative, Apply}
import cats.effect.Sync
import cats.kernel.Monoid

/**
  * Suspend logging side-effects allowing monadic composition in terms of an effect.
  */
trait Logging[F[_]] { self =>

  protected def log(cls: UnderlyingLogger[F], message: Message): F[Unit]

  @inline def trace(message: String)(implicit cls: UnderlyingLogger[F]): F[Unit] = log(cls, Message(Level.Trace, message, None))
  @inline def debug(message: String)(implicit cls: UnderlyingLogger[F]): F[Unit] = log(cls, Message(Level.Debug, message, None))
  @inline def info (message: String)(implicit cls: UnderlyingLogger[F]): F[Unit] = log(cls, Message(Level.Info, message, None))
  @inline def warn (message: String)(implicit cls: UnderlyingLogger[F]): F[Unit] = log(cls, Message(Level.Warn, message, None))
  @inline def error(message: String)(implicit cls: UnderlyingLogger[F]): F[Unit] = log(cls, Message(Level.Error, message, None))

  @inline def trace(message: String, ex: Throwable)(implicit cls: UnderlyingLogger[F]): F[Unit] = log(cls, Message(Level.Trace, message, Some(ex)))
  @inline def debug(message: String, ex: Throwable)(implicit cls: UnderlyingLogger[F]): F[Unit] = log(cls, Message(Level.Debug, message, Some(ex)))
  @inline def info (message: String, ex: Throwable)(implicit cls: UnderlyingLogger[F]): F[Unit] = log(cls, Message(Level.Info, message, Some(ex)))
  @inline def warn (message: String, ex: Throwable)(implicit cls: UnderlyingLogger[F]): F[Unit] = log(cls, Message(Level.Warn, message, Some(ex)))
  @inline def error(message: String, ex: Throwable)(implicit cls: UnderlyingLogger[F]): F[Unit] = log(cls, Message(Level.Error, message, Some(ex)))

  def andThen(that: Logging[F])(implicit ap: Apply[F]): Logging[F] =
    (cls: UnderlyingLogger[F], message: Message) => ap.productR(self.log(cls, message))(that.log(cls, message))
}

object Logging {
  implicit def apply[F[_]](implicit F: Sync[F]): Logging[F] = DefaultLogger[F]

  def void[F[_]](implicit A: Applicative[F]): Logging[F] = (_: UnderlyingLogger[F], _: Message) => A.unit

  implicit def monoidInstanceForLogger[F[_]](implicit F: Applicative[F]): Monoid[Logging[F]] = new Monoid[Logging[F]] {
    def empty: Logging[F] = Logging.void

    def combine(x: Logging[F], y: Logging[F]): Logging[F] = x andThen y
  }
}
