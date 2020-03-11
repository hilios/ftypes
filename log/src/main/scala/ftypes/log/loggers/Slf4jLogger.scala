package ftypes.log.loggers

import cats.effect.Sync
import cats.implicits._
import ftypes.log.{Level, Logger, Message}
import sourcecode.{Enclosing, Line}

final class Slf4jLogger[F[_]] private (implicit F: Sync[F]) extends Logger[F] {

  def log(message: Message, enclosing: Enclosing, line: Line): F[Unit] =
    for {
      logger <- F.delay(org.slf4j.LoggerFactory.getLogger(enclosing.value))
      _ <- F.delay {
        message match {
          case Level.Trace(msg, ex) => ex.fold(logger.trace(msg))(logger.trace(msg, _))
          case Level.Debug(msg, ex) => ex.fold(logger.debug(msg))(logger.debug(msg, _))
          case Level.Info(msg, ex)  => ex.fold(logger.info(msg))(logger.info(msg, _))
          case Level.Warn(msg, ex)  => ex.fold(logger.warn(msg))(logger.warn(msg, _))
          case Level.Error(msg, ex) => ex.fold(logger.error(msg))(logger.error(msg, _))
        }
      }
    } yield ()
}

object Slf4jLogger {
  def apply[F[_]: Sync]: Slf4jLogger[F] = new Slf4jLogger[F]
}
