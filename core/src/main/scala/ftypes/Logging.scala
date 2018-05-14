package ftypes

import cats.effect.Sync
import org.slf4j.Logger

import scala.annotation.tailrec
import scala.reflect.macros.blackbox

trait Logging[F[_]] {
  def name: String = ""
  def trace(message: => String): F[Unit]
  def trace(message: => String, ex: Throwable): F[Unit]
  def debug(message: => String): F[Unit]
  def debug(message: => String, ex: Throwable): F[Unit]
  def info(message: => String): F[Unit]
  def info(message: => String, ex: Throwable): F[Unit]
  def warn(message: => String): F[Unit]
  def warn(message: => String, ex: Throwable): F[Unit]
  def error(message: => String): F[Unit]
  def error(message: => String, ex: Throwable): F[Unit]
}

object Logging {
  implicit def apply[F[_]](implicit F: Sync[F]): Logging[F] = macro materializeLogging[F]

  def apply[F[_]](logger: Logger)(F: Sync[F]): Logging[F] = new Logging[F] {
    override val name = logger.getName

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

  final def materializeLogging[F[_]](c: blackbox.Context)(F: c.Expr[Sync[F]]): c.Expr[Logging[F]] = {
    import c.universe._

    @tailrec def getClassSymbol(s: Symbol): Symbol = if (s.isClass || s.isModule) s
    else getClassSymbol(s.owner)

    val cls = getClassSymbol(c.internal.enclosingOwner)
    val imp = c.inferImplicitValue(F.actualType)
    val name = cls.fullName.stripSuffix("$")

    assert(cls.isClass || cls.isModule, "Enclosing class is always either a module or a class")
    c.Expr[Logging[F]](q"""Logging(org.slf4j.LoggerFactory.getLogger($name))($imp)""")
  }
}
