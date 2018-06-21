package ftypes.log

import cats.Show
import cats.implicits._
import org.scalatest.{FlatSpec, Matchers}

class ImplicitsSpec extends FlatSpec with Matchers with Implicits {

  val render = implicitly[Show[LogMessage]]

  it should "" in {
    Trace("Test", None).level < Debug("Test", None).level shouldBe true
    Debug("Test", None).level < Info ("Test", None).level shouldBe true
    Info ("Test", None).level < Warn ("Test", None).level shouldBe true
    Warn ("Test", None).level < Error("Test", None).level shouldBe true
  }
  
  "Show[LogLevel]" should "render the message with the right color, level and message" in {
    render.show(Trace("Test", None)) should include (s"[trace] Test")
    render.show(Debug("Test", None)) should include (s"[debug] Test")
    render.show(Info ("Test", None)) should include (s"[info] Test")
    render.show(Warn ("Test", None)) should include (s"[warn] Test")
    render.show(Error("Test", None)) should include (s"[error] Test")
  }

  it should "render the exception stack trace" in {
    val ex = new Exception("Boom!")
    val st = new StackTraceElement("MyClass", "fn", "path/to/MyClass.scala", 20)
    ex.setStackTrace(Array(st))

    render.show(Error("Test", Some(ex))).split("\n").map(_.trim) should contain (
      "at MyClass.fn(path/to/MyClass.scala:20)"
    )
  }
}
