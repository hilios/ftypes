package ftypes.log

import org.slf4j

import scala.annotation.tailrec
import scala.reflect.macros.blackbox

/**
  * Macros to infer the SLF4J logger instance automatically
  */
private[ftypes] class LoggingMacros(val c: blackbox.Context) {
  import c.universe._

  val INCORRECT_SYMBOL = "Enclosing class is always either a module or a class"

  @tailrec private def getClassSymbol(s: Symbol): Symbol = if (s.isClass || s.isModule) s
  else getClassSymbol(s.owner)

  final def materializeLogger: c.Expr[Logger] = {
    val cls = getClassSymbol(c.internal.enclosingOwner)
    val name = cls.fullName.stripSuffix("$").stripSuffix(".$anon")

    assert(cls.isClass || cls.isModule, INCORRECT_SYMBOL)
    
    c.Expr[Logger](q"""
      new Logger {
        def get: org.slf4j.Logger = org.slf4j.LoggerFactory.getLogger($name)
      }
    """)
  }

  final def materializeLoggerFactory: c.Expr[slf4j.Logger] = {
    val cls = getClassSymbol(c.internal.enclosingOwner)
    val name = cls.fullName.stripSuffix("$").stripSuffix(".$anon")

    assert(cls.isClass || cls.isModule, INCORRECT_SYMBOL)
    c.Expr[slf4j.Logger](q"""org.slf4j.LoggerFactory.getLogger($name)""")
  }

  final def materializeLoggerFactoryT[T: c.WeakTypeTag]: c.Expr[slf4j.Logger] = {
    val cls = c.symbolOf[T]
    val name = cls.fullName.stripSuffix("$").stripSuffix(".$anon")

    assert(cls.isClass || cls.isModule, INCORRECT_SYMBOL)
    c.Expr[slf4j.Logger](q"""org.slf4j.LoggerFactory.getLogger($name)""")
  }
}
