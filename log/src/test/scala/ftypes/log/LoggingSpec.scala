package ftypes.log

import cats.effect.{IO, Sync}
import cats.implicits._
import ftypes.log.LoggingSpec.TestLogging
import org.scalatest.{FlatSpec, Matchers}

class LoggingSpec extends FlatSpec with Matchers {
  
  "Slf4jLogger" should "implicitly resolved in the context by the effect class" in {
    val test = new TestLogging[IO]
    test.prog.unsafeRunSync()
  }

  "ConsoleLogger" should "render the log" in {
    implicit val print = ConsoleLogger[IO]
    val test = new TestLogging[IO]
    test.prog.unsafeRunSync()
  }

  "SilentLogger" should "render the log" in {
    implicit val silent = SilentLogger[IO]
    val test = new TestLogging[IO]
    test.prog.unsafeRunSync()

    silent.messages should contain allOf (
      Message(Level.Trace, "Go ahead and leave me...", None),
      Message(Level.Debug, "I think I'd prefer to stay inside...", None),
      Message(Level.Warn,  "Maybe you'll find someone else to help you.", None),
      Message(Level.Info,  "Maybe Black Mesa?", None),
      Message(Level.Error, "That was a joke. Ha Ha. Fat Chance!", None)
    )
  }

  it should "disable all logs" in {
    implicit val silent = SilentLogger[IO](Level.Off)
    val test = new TestLogging[IO]
    test.prog.unsafeRunSync()

    silent.messages shouldBe empty
  }

  "#andThen" should "combine multiple logs instances" in {
    val silent = SilentLogger[IO]
    implicit val logger: Logging[IO] = ConsoleLogger[IO] andThen DefaultLogger[IO] andThen silent
    
    val test = new TestLogging[IO]
    test.prog.unsafeRunSync()
    silent.messages should have length 5
  }
}

object LoggingSpec {
  class TestLogging[F[_]](implicit F: Sync[F], L: Logging[F]) {
    def prog: F[Unit] = for {
      _ <- L.trace("Go ahead and leave me...")
      _ <- L.debug("I think I'd prefer to stay inside...")
      _ <- L.warn("Maybe you'll find someone else to help you.")
      _ <- L.info("Maybe Black Mesa?")
      _ <- L.error("That was a joke. Ha Ha. Fat Chance!")
    } yield ()
  }
}
