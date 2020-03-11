package ftypes.log

import cats.{Applicative, Apply, Monoid}
import sourcecode.{Enclosing, Line}

trait Logger[F[_]] { self =>
  def log(message: Message, enclosing: Enclosing, line: Line): F[Unit]

  def andThen(that: Logger[F])(implicit ap: Apply[F]): Logger[F] = new Logger[F] {

    def log(message: Message, enclosing: Enclosing, line: Line): F[Unit] =
      ap.productR(self.log(message, enclosing, line))(that.log(message, enclosing, line))
  }
}

object Logger {

  implicit def apply[F[_]: Applicative]: Logger[F] = void

  def void[F[_]](implicit F: Applicative[F]): Logger[F] = new Logger[F] {
    def log(message: Message, enclosing: Enclosing, line: Line): F[Unit] = F.unit
  }

  implicit def monoidInstanceForLogger[F[_]](implicit F: Applicative[F]): Monoid[Logger[F]] = new Monoid[Logger[F]] {
    def empty: Logger[F] = Logger.void

    def combine(x: Logger[F], y: Logger[F]): Logger[F] = x andThen y
  }
}
