package ftypes.log

import cats.effect.IO
import org.scalatest.{FlatSpec, Matchers}

class UnderlyingLoggerSpec extends FlatSpec with Matchers {
  "#logger" should "materialize an instance logger instance with the owner class name" in {
    UnderlyingLogger[IO].logger.map(_.getName).unsafeRunSync() shouldBe "ftypes.log.UnderlyingLoggerSpec"
  }
}
