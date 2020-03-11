package ftypes.log.loggers

import cats.effect.IO
import ftypes.log.StillAlive
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

class Slf4jLoggerSpec extends AnyFlatSpec with Matchers {

  it should "materialize an instance logger instance with the owner class name" in {
    implicit val logger = Slf4jLogger[IO]
    StillAlive[IO].log.unsafeRunSync()
  }
}
