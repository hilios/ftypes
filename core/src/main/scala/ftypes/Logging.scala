package ftypes

import cats.effect.Sync
import org.slf4j.{Logger, LoggerFactory}

import scala.reflect.ClassTag

/**
  * Suspend logging side-effects allowing monadic composition in terms of a Sync effect.
  */
class Logging[F[_]](val logger: Logger = LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME))(implicit F: Sync[F]) {

  def get: Logging[F] = macro LoggingMacros.materializeLogging[F]
  def withName(name: String): Logging[F] = new Logging[F](LoggerFactory.getLogger(name))
  def forClass[T](implicit ct: ClassTag[T]): Logging[F] = new Logging[F](LoggerFactory.getLogger(ct.runtimeClass))

  def trace(message: => String): F[Unit] =
    F.delay(logger.trace(message))

  def trace(message: => String, ex: Throwable): F[Unit] =
    F.delay(logger.trace(message, ex))

  def debug(message: => String): F[Unit] =
    F.delay(logger.debug(message))

  def debug(message: => String, ex: Throwable): F[Unit] =
    F.delay(logger.debug(message, ex))

  def info(message: => String): F[Unit] =
    F.delay(logger.info(message))

  def info(message: => String, ex: Throwable): F[Unit] =
    F.delay(logger.info(message, ex))

  def warn(message: => String): F[Unit] =
    F.delay(logger.warn(message))

  def warn(message: => String, ex: Throwable): F[Unit] =
    F.delay(logger.warn(message, ex))

  def error(message: => String): F[Unit] =
    F.delay(logger.error(message))

  def error(message: => String, ex: Throwable): F[Unit] =
    F.delay(logger.error(message, ex))
}

object Logging {
//  implicit def foo[F[_], T[_]]: Logging[F] = ???

  implicit def apply[F[_]](implicit F: Sync[F]): Logging[F] = macro LoggingMacros.materializeLoggingF[F]
  def get[T]: Logger = macro LoggingMacros.materializeLoggerT[T]
  def getLogger: Logger = macro LoggingMacros.materializeLogger
}
