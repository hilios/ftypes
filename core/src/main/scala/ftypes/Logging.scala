package ftypes

import cats.effect.Sync
import org.slf4j

/**
  * Suspend logging side-effects allowing monadic composition in terms of an effect.
  */
class Logging[F[_]](implicit F: Sync[F]) {

  def logger(implicit l: Logger): slf4j.Logger = l.get

  def trace(message: => String)(implicit l: Logger): F[Unit] =
    F.delay(logger.trace(message))

  def trace(message: => String, ex: Throwable)(implicit l: Logger): F[Unit] =
    F.delay(logger.trace(message, ex))

  def debug(message: => String)(implicit l: Logger): F[Unit] =
    F.delay(logger.debug(message))

  def debug(message: => String, ex: Throwable)(implicit l: Logger): F[Unit] =
    F.delay(logger.debug(message, ex))

  def info(message: => String)(implicit l: Logger): F[Unit] =
    F.delay(logger.info(message))

  def info(message: => String, ex: Throwable)(implicit l: Logger): F[Unit] =
    F.delay(logger.info(message, ex))

  def warn(message: => String)(implicit l: Logger): F[Unit] =
    F.delay(logger.warn(message))

  def warn(message: => String, ex: Throwable)(implicit l: Logger): F[Unit] =
    F.delay(logger.warn(message, ex))

  def error(message: => String)(implicit l: Logger): F[Unit] =
    F.delay(logger.error(message))

  def error(message: => String, ex: Throwable)(implicit l: Logger): F[Unit] =
    F.delay(logger.error(message, ex))
}

object Logging {
  implicit def apply[F[_]](implicit F: Sync[F]): Logging[F] = new Logging[F]
}
