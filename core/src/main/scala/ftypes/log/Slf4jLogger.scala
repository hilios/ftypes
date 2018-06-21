package ftypes.log

import cats.effect.Sync
import org.slf4j

case class Slf4jLogger[F[_]](logger: slf4j.Logger)(implicit F: Sync[F]) extends Logger[F] {
  override def log(message: LogMessage): F[Unit] = F.delay {
    message match {
      case Trace(msg, ex) => ex.fold(logger.trace(msg))(logger.trace(msg, _))
      case Debug(msg, ex) => ex.fold(logger.debug(msg))(logger.debug(msg, _))
      case Info (msg, ex) => ex.fold(logger.info (msg))(logger.info (msg, _))
      case Warn (msg, ex) => ex.fold(logger.warn (msg))(logger.warn (msg, _))
      case Error(msg, ex) => ex.fold(logger.error(msg))(logger.error(msg, _))
    }
  }
}
