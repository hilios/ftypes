package ftypes.log

import cats.Eval
import cats.effect.Sync
import org.slf4j.{Logger, LoggerFactory}

import scala.annotation.tailrec
import scala.reflect.macros.blackbox

final class UnderlyingLogger[F[_]](name: String)(implicit F: Sync[F]) {

  private val slf4jLogger: Eval[Logger] = Eval.later {
    LoggerFactory.getLogger(name)
  }

  def logger: F[Logger] = F.pure(slf4jLogger.value)
}

object UnderlyingLogger {

  implicit def apply[F[_]](implicit F: Sync[F]): UnderlyingLogger[F] = macro materializeUnderlyingLogger[F]

  final def materializeUnderlyingLogger[F[_]](c: blackbox.Context)(F: c.Expr[Sync[F]]): c.Expr[UnderlyingLogger[F]] = {
    import c.universe._

    @tailrec def getClassSymbol(s: Symbol): Symbol = if (s.isClass || s.isModule) s
    else getClassSymbol(s.owner)

    val cls = getClassSymbol(c.internal.enclosingOwner)
    val name = cls.fullName.stripSuffix("$").stripSuffix(".$anon").stripSuffix(".$anonfun")

    assert(cls.isClass || cls.isModule, "Enclosing class must be either a module or a class")
    c.Expr[UnderlyingLogger[F]](q"new UnderlyingLogger($name)($F)")
  }
}
