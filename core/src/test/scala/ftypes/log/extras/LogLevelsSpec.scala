package ftypes.log.extras

import org.scalatest.{FlatSpec, Matchers}

class LogLevelsSpec extends FlatSpec with Matchers {
  it should "provide the right name for each sub type" in {
    Trace.name shouldBe "[trace]"
    Debug.name shouldBe "[debug]"
    Info.name  shouldBe "[info]"
    Warn.name  shouldBe "[warn]"
    Error.name shouldBe "[error]"
  }
}