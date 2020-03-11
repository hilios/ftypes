package ftypes.log

import cats.effect.IO
import ftypes.log.loggers.{ConsoleLogger, SilentLogger}
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

class LoggerSpec extends AnyFlatSpec with Matchers {

  "#andThen" should "combine multiple logger instances" in {
    val silent = SilentLogger[IO]
    implicit val logger: Logger[IO] = ConsoleLogger[IO] andThen silent

    StillAlive[IO].log.unsafeRunSync()
    silent.messages should have length 5
  }
}
