package ftypes.log

import ftypes.log.LoggerSpec.Foo
import org.scalatest._

class LoggerSpec extends FlatSpec with Matchers {

  it should "implicitly get the a logger instance for the caller class" in {
    implicitly[Logger].get.getName shouldBe "ftypes.log.LoggerSpec"
  }

  ".get" should "materialize a logger instance for the caller class" in {
    Logger.get.getName shouldBe "ftypes.log.LoggerSpec"
  }

  ".forClass[T]" should "return a logger instance for the given type" in {
    Logger.forClass[Foo].getName shouldBe "ftypes.log.LoggerSpec.Foo"
  }

  "#withName(name)" should "return a logger instance with the given name" in {
    Logger.withName("Bar").getName shouldBe "Bar"
  }
}

object LoggerSpec {
  case class Foo()
}