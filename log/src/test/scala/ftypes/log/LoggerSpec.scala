package ftypes.log

import cats.effect.IO
import org.scalatest.{FlatSpec, Matchers}

class LoggerSpec extends FlatSpec with Matchers {
  "#logger" should "materialize an instance logger instance with the owner class name" in {
    Logger[IO].name shouldBe "ftypes.log.LoggerSpec"
  }
}
