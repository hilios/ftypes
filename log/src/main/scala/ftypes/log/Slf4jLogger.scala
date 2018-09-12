package ftypes.log

import cats.effect.Sync

case class Slf4jLogger[F[_]]()(implicit F: Sync[F]) extends Logger[F] {
  
  override def log(cls: EnclosingClass, message: LogMessage): F[Unit] = F.delay {
    message match {
      case LogMessage.TraceLog(msg, ex) => ex.fold(cls.logger.trace(msg))(cls.logger.trace(msg, _))
      case LogMessage.DebugLog(msg, ex) => ex.fold(cls.logger.debug(msg))(cls.logger.debug(msg, _))
      case LogMessage.InfoLog (msg, ex) => ex.fold(cls.logger.info (msg))(cls.logger.info (msg, _))
      case LogMessage.WarnLog (msg, ex) => ex.fold(cls.logger.warn (msg))(cls.logger.warn (msg, _))
      case LogMessage.ErrorLog(msg, ex) => ex.fold(cls.logger.error(msg))(cls.logger.error(msg, _))
    }
  }
}
