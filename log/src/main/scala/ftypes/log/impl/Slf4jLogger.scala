package ftypes.log.impl

import cats.Eval
import cats.effect.Sync
import ftypes.log.{Level, Logger, Message}

final case class Slf4jLogger[F[_]](name: String)(implicit F: Sync[F]) extends Logger[F] {

  private val logger: Eval[org.slf4j.Logger] = Eval.later {
    org.slf4j.LoggerFactory.getLogger(name)
  }

  def log(message: Message): F[Unit] = F.delay {
    message match {
      case Level.Trace(msg, ex) => ex.fold(logger.value.trace(msg))(logger.value.trace(msg, _))
      case Level.Debug(msg, ex) => ex.fold(logger.value.debug(msg))(logger.value.debug(msg, _))
      case Level.Info(msg,  ex) => ex.fold(logger.value.info(msg)) (logger.value.info(msg,  _))
      case Level.Warn(msg,  ex) => ex.fold(logger.value.warn(msg)) (logger.value.warn(msg,  _))
      case Level.Error(msg, ex) => ex.fold(logger.value.error(msg))(logger.value.error(msg, _))
    }
  }
}
