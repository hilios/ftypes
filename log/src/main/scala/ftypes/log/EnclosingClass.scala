package ftypes.log

import scala.annotation.tailrec
import scala.reflect.macros.blackbox
import org.slf4j.LoggerFactory

final case class EnclosingClass(name: String) {
  lazy val logger = LoggerFactory.getLogger(name)
}

object EnclosingClass {

  implicit def materialize: EnclosingClass = macro materializeEnclosingClass

  final def materializeEnclosingClass(c: blackbox.Context): c.Expr[EnclosingClass] = {
    import c.universe._

    @tailrec def getClassSymbol(s: Symbol): Symbol = if (s.isClass || s.isModule) s
    else getClassSymbol(s.owner)

    val cls = getClassSymbol(c.internal.enclosingOwner)
    val name = cls.fullName.stripSuffix("$").stripSuffix(".$anon").stripSuffix(".$anonfun")

    assert(cls.isClass || cls.isModule, "Enclosing class must be either a module or a class")
    c.Expr[EnclosingClass](q"new EnclosingClass($name)")
  }
}
