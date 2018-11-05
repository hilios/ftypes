package ftypes.log

import cats.effect.Sync
import ftypes.log.impl.Slf4jLogger

import scala.annotation.tailrec
import scala.reflect.macros.blackbox

trait Logger[F[_]] {
  def name: String

  def log(message: Message): F[Unit]
}

object Logger {
  implicit def apply[F[_]](implicit F: Sync[F]): Logger[F] = macro materializeSlf4jLogger[F]

  def materializeSlf4jLogger[F[_]](c: blackbox.Context)(F: c.Expr[Sync[F]]): c.Expr[Slf4jLogger[F]] = {
    import c.universe._

    @tailrec def getClassSymbol(s: Symbol): Symbol = if (s.isClass || s.isModule) s
    else getClassSymbol(s.owner)

    val cls = getClassSymbol(c.internal.enclosingOwner)
    val name = cls.fullName.stripSuffix("$").stripSuffix(".$anon").stripSuffix(".$anonfun")

    assert(cls.isClass || cls.isModule, "Enclosing class is always either a module or a class")
    c.Expr[Slf4jLogger[F]](q"""new ftypes.log.impl.Slf4jLogger($name)($F)""")
  }
}

