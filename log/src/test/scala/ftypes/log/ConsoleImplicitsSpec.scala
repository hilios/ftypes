package ftypes.log

import cats.Show
import org.scalatest.{FlatSpec, Matchers}

class ConsoleImplicitsSpec extends FlatSpec with Matchers with ConsoleImplicits {

  val render = implicitly[Show[Message]]

  "Show[LogLevel]" should "render the message with the right color, level and message" in {
    render.show(Message(Level.Trace, "Test", None)) should include (s"[trace] Test")
    render.show(Message(Level.Debug, "Test", None)) should include (s"[debug] Test")
    render.show(Message(Level.Info,  "Test", None)) should include (s"[info] Test")
    render.show(Message(Level.Warn,  "Test", None)) should include (s"[warn] Test")
    render.show(Message(Level.Error, "Test", None)) should include (s"[error] Test")
  }

  it should "render the exception stack trace" in {
    val ex = new Exception("Boom!")
    val st = new StackTraceElement("MyClass", "fn", "path/to/MyClass.scala", 20)
    ex.setStackTrace(Array(st))

    render.show(Message(Level.Error, "Test", Some(ex))).split("\n").map(_.trim) should contain (
      "at MyClass.fn(path/to/MyClass.scala:20)"
    )
  }
}
