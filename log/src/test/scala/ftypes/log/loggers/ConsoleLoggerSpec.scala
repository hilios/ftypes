package ftypes.log.loggers

import cats.effect.IO
import ftypes.log.StillAlive
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

class ConsoleLoggerSpec extends AnyFlatSpec with Matchers {
  it should "print the log in the system out" in {
    implicit val logger = ConsoleLogger[IO]
    StillAlive[IO].log.unsafeRunSync()
  }
}
