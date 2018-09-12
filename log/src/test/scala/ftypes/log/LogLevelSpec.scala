package ftypes.log

import cats.implicits._
import org.scalatest.{FlatSpec, Matchers}

class LogLevelSpec extends FlatSpec with Matchers {
  it should "have ordering to filter the visibility" in {
    val all: LogLevel = LogLevel.All
    val tra: LogLevel = LogLevel.Trace
    val deb: LogLevel = LogLevel.Debug
    val inf: LogLevel = LogLevel.Info
    val war: LogLevel = LogLevel.Warn
    val err: LogLevel = LogLevel.Error
    val off: LogLevel = LogLevel.Off

    all < tra shouldBe true
    tra < deb shouldBe true
    deb < inf shouldBe true
    inf < war shouldBe true
    war < err shouldBe true
    err < off shouldBe true
  }
}
