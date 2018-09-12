package ftypes.log

import cats.effect.{IO, Sync}
import cats.implicits._
import ftypes.log.LoggerSpec.TestLogging
import org.scalatest.{FlatSpec, Matchers}

class LoggerSpec extends FlatSpec with Matchers {
  
  "Slf4jLogging" should "implicitly resolved in the context by the effect class" in {
    val test = new TestLogging[IO]
    test.prog.unsafeRunSync()
  }

  "PrintLog" should "render the log" in {
    implicit val print = ConsoleLogger[IO]
    val test = new TestLogging[IO]
    test.prog.unsafeRunSync()
  }

  "SilentLog" should "render the log" in {
    implicit val silent = SilentLogger[IO]
    val test = new TestLogging[IO]
    test.prog.unsafeRunSync()

    silent.messages should contain allOf (
      LogMessage.TraceLog("Go ahead and leave me...", None),
      LogMessage.DebugLog("I think I'd prefer to stay inside...", None),
      LogMessage.WarnLog("Maybe you'll find someone else to help you.", None),
      LogMessage.InfoLog("Maybe Black Mesa?", None),
      LogMessage.ErrorLog("That was a joke. Ha Ha. Fat Chance!", None)
    )
  }

  it should "disable all logs" in {
    implicit val silent = SilentLogger[IO](LogLevel.Off)
    val test = new TestLogging[IO]
    test.prog.unsafeRunSync()

    silent.messages shouldBe empty
  }
}

object LoggerSpec {
  class TestLogging[F[_]](implicit F: Sync[F], L: Logger[F]) {
    def prog: F[Unit] = for {
      _ <- L.trace("Go ahead and leave me...")
      _ <- L.debug("I think I'd prefer to stay inside...")
      _ <- L.warn("Maybe you'll find someone else to help you.")
      _ <- L.info("Maybe Black Mesa?")
      _ <- L.error("That was a joke. Ha Ha. Fat Chance!")
    } yield ()
  }
}
