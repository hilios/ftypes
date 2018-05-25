package ftypes

import cats.effect.Sync
import org.slf4j.Logger

import scala.annotation.tailrec
import scala.reflect.macros.blackbox

class LoggingMacros(val c: blackbox.Context) {
  import c.universe._

  @tailrec private def getClassSymbol(s: Symbol): Symbol = if (s.isClass || s.isModule) s
  else getClassSymbol(s.owner)

  final def materializeLoggingF[F[_]](F: c.Expr[Sync[F]]): c.Expr[Logging[F]] = {
    val cls = getClassSymbol(c.internal.enclosingOwner)
    val imp = c.inferImplicitValue(F.actualType)

    val name = cls.fullName.stripSuffix("$").stripSuffix(".$anon")

    assert(cls.isClass || cls.isModule, "Enclosing class is always either a module or a class")
    c.Expr[Logging[F]](q"""new Logging(org.slf4j.LoggerFactory.getLogger($name))($imp)""")
  }

  def materializeLogging[F[_]]: c.Expr[Logging[F]] = {
    val cls = getClassSymbol(c.internal.enclosingOwner)
    val name = cls.fullName.stripSuffix("$").stripSuffix(".$anon")

    c.Expr[Logging[F]](q"new Logging[F](org.slf4j.LoggerFactory.getLogger($name))")
  }

  final def materializeLogger: c.Expr[Logger] = {
    val cls = getClassSymbol(c.internal.enclosingOwner)
    val name = cls.fullName.stripSuffix("$").stripSuffix(".$anon")

    assert(cls.isClass || cls.isModule, "Enclosing class is always either a module or a class")
    c.Expr[Logger](q"""org.slf4j.LoggerFactory.getLogger($name)""")
  }

  final def materializeLoggerT[T: c.WeakTypeTag]: c.Expr[Logger] = {
    val cls = c.symbolOf[T]
    val name = cls.fullName.stripSuffix("$").stripSuffix(".$anon")

    assert(cls.isClass || cls.isModule, "Enclosing class is always either a module or a class")
    c.Expr[Logger](q"""org.slf4j.LoggerFactory.getLogger($name)""")
  }

  final def selfMaterializer[F[_]]: c.Expr[Logging[F]] = {
    c.Expr[Logging[F]](q"this")
  }
}
