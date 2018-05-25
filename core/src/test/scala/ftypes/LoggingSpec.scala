package ftypes

import cats.effect.{IO, Sync}
import cats.implicits._
import ftypes.LoggingSpec.TestLogging
import org.scalatest._

class LoggingSpec extends FlatSpec with Matchers {
  ".getLogger" should "materialize a sl4j logger instance for the caller class" in {
    val logger = Logging.getLogger
    logger.getName shouldBe "ftypes.LoggingSpec"
  }

  it should "implicitly resolved in the context by the effect class" in {
    val t = new TestLogging[IO]
    t.prog.unsafeRunSync()
  }

  "#getLogger" should "return a logging instance with the slf4j logger for the caller class" in {
    val t = new TestLogging[IO]
    t.log.logger.getName shouldBe "ftypes.LoggingSpec.TestLogging"
  }

  "#withName(name)" should "return a logging instance with the slf4j logger for the caller class" in {
    val t = new TestLogging[IO]
    t.logN.logger.getName shouldBe "Bar"
  }

  "#forClass[T]" should "return a logging instance with the slf4j logger for the caller class" in {
    val t = new TestLogging[IO]
    t.logT.logger.getName shouldBe "ftypes.LoggingSpec$Foo"
  }
}

object LoggingSpec {
  case class Foo()

  class TestLogging[F[_]](implicit F: Sync[F], L: Logging[F]) {

    val log = L.get
    val logT = L.forClass[Foo]
    val logN = L.withName("Bar")

    def prog: F[Unit] = for {
      _ <- L.trace("Go ahead and leave me...")
      _ <- L.debug("I think I'd prefer to stay inside...")
      _ <- L.warn("Maybe you'll find someone else to help you.")
      _ <- L.info("Maybe Black Mesa?")
      _ <- L.error("That was a joke. Ha Ha. Fat Chance!")
    } yield ()
  }
}
