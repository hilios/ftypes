package ftypes.test.logging

import org.scalatest.{FlatSpec, Matchers}

class PrintLogSpec extends FlatSpec with Matchers {
  "LogLevel" should "provide the right name for each sub type" in {
    Trace.name shouldBe "[trace]"
    Debug.name shouldBe "[debug]"
    Info.name  shouldBe "[info]"
    Warn.name  shouldBe "[warn]"
    Error.name shouldBe "[error]"
  }
}
