package ftypes.log

import cats.implicits._
import org.scalatest.{FlatSpec, Matchers}

class LevelSpec extends FlatSpec with Matchers {
  it should "have ordering to filter the visibility" in {
    val all: Level   = Level.All
    val trace: Level = Level.Trace
    val debug: Level = Level.Debug
    val info: Level  = Level.Info
    val warn: Level  = Level.Warn
    val error: Level = Level.Error
    val off: Level   = Level.Off

    all   < trace shouldBe true
    trace < debug shouldBe true
    debug < info  shouldBe true
    info  < warn  shouldBe true
    warn  < error shouldBe true
    error < off   shouldBe true
  }

  "#name" should "return the level name" in {
    Level.All.name shouldBe "all"
    Level.Trace.name shouldBe "trace"
    Level.Debug.name shouldBe "debug"
    Level.Info.name shouldBe "info"
    Level.Warn.name shouldBe "warn"
    Level.Error.name shouldBe "error"
    Level.Off.name shouldBe "off"
  }

  "#unnaply" should "match log messages by the level" in {
    val Level.Trace(trace, _) = Message(Level.Trace, "Trace", None)
    trace shouldBe "Trace"

    val Level.Debug(debug, _) = Message(Level.Debug, "Debug", None)
    debug shouldBe "Debug"

    val Level.Info(info, _)   = Message(Level.Info,  "Info",  None)
    info shouldBe "Info"
    
    val Level.Warn(warn, _)   = Message(Level.Warn,  "Warn",  None)
    warn shouldBe "Warn"

    val Level.Error(error, _) = Message(Level.Error, "Error", None)
    error shouldBe "Error"
  }
}
