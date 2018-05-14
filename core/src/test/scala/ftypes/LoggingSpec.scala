package ftypes

import cats.effect.IO
import ftypes.LoggingSpec.TestLogging
import org.scalatest._

class LoggingSpec extends FlatSpec with Matchers {
  it should "materialize the slf4j logger implicitly at compile type with the correct name" in {
    val t = new TestLogging[IO]
    t.logger.name shouldBe "ftypes.LoggingSpec"
  }
}

object LoggingSpec {
  class TestLogging[F[_]](implicit L: Logging[F]) {
    val logger: Logging[F] = L
  }
}
