package ftypes.log

import cats.Show
import org.scalatest.{FlatSpec, Matchers}

class ConsoleImplicitsSpec extends FlatSpec with Matchers with ConsoleImplicits {

  val render = implicitly[Show[LogMessage]]

  "Show[LogLevel]" should "render the message with the right color, level and message" in {
    render.show(LogMessage.TraceLog("Test", None)) should include (s"[trace] Test")
    render.show(LogMessage.DebugLog("Test", None)) should include (s"[debug] Test")
    render.show(LogMessage.InfoLog ("Test", None)) should include (s"[info] Test")
    render.show(LogMessage.WarnLog ("Test", None)) should include (s"[warn] Test")
    render.show(LogMessage.ErrorLog("Test", None)) should include (s"[error] Test")
  }

  it should "render the exception stack trace" in {
    val ex = new Exception("Boom!")
    val st = new StackTraceElement("MyClass", "fn", "path/to/MyClass.scala", 20)
    ex.setStackTrace(Array(st))

    render.show(LogMessage.ErrorLog("Test", Some(ex))).split("\n").map(_.trim) should contain (
      "at MyClass.fn(path/to/MyClass.scala:20)"
    )
  }
}
