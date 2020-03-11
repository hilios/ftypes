package ftypes.log

import cats.Show
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

class MessageSpec extends AnyFlatSpec with Matchers {
  it should "have a show instance" in {
    Show[Message].show(Message(Level.Trace, "Test", None)) should include(s"[trace] Test")
    Show[Message].show(Message(Level.Debug, "Test", None)) should include(s"[debug] Test")
    Show[Message].show(Message(Level.Info, "Test", None)) should include(s"[info] Test")
    Show[Message].show(Message(Level.Warn, "Test", None)) should include(s"[warn] Test")
    Show[Message].show(Message(Level.Error, "Test", None)) should include(s"[error] Test")
  }

  it should "render the exception stack trace" in {
    val ex = new Exception("Boom!")
    val st = new StackTraceElement("MyClass", "fn", "path/to/MyClass.scala", 20)
    ex.setStackTrace(Array(st))

    Show[Message].show(Message(Level.Error, "Test", Some(ex))).split("\n").map(_.trim) should contain(
      "at MyClass.fn(path/to/MyClass.scala:20)"
    )
  }

  ".unapply" should "extract the message bits" in {
    val Message(level, value, ex) = Level.Info("Hello, world!", None)
    level shouldBe Level.Info
    value shouldBe "Hello, world!"
    ex shouldBe None
  }
}
