package ftypes.log

import cats.effect.Sync

case class DefaultLogger[F[_]]()(implicit F: Sync[F]) extends Logging[F] {
  
  override def log(cls: UnderlyingLogger[F], message: Message): F[Unit] = F.map(cls.logger) { l =>
    message match {
      case Level.Trace(msg, ex) => ex.fold(l.trace(msg))(l.trace(msg, _))
      case Level.Debug(msg, ex) => ex.fold(l.debug(msg))(l.debug(msg, _))
      case Level.Info(msg,  ex) => ex.fold(l.info(msg))(l.info(msg,   _))
      case Level.Warn(msg,  ex) => ex.fold(l.warn(msg))(l.warn(msg,   _))
      case Level.Error(msg, ex) => ex.fold(l.error(msg))(l.error(msg, _))
    }
  }
}
