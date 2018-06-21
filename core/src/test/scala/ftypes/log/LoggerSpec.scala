package ftypes.log

import cats.effect.IO
import org.scalatest._

class LoggerSpec extends FlatSpec with Matchers {

  it should "implicitly get a instance of the Slf4jLogger" in {
    implicitly[Logger[IO]] shouldBe a[Slf4jLogger[IO]]
  }

  it should "set the logger name with caller class" in {
    implicitly[Logger[IO]].asInstanceOf[Slf4jLogger[IO]].logger.getName shouldBe "ftypes.log.LoggerSpec"
  }
}

