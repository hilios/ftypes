package ftypes.log.loggers

import cats.effect.IO
import ftypes.log.{Level, Message, StillAlive}
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

class SilentLoggerSpec extends AnyFlatSpec with Matchers {

  it should "should store all log messages in a queue" in {
    implicit val logger = SilentLogger[IO]
    StillAlive[IO].log.unsafeRunSync()

    logger.messages should contain allOf (
      Message(Level.Trace, "Go ahead and leave me...", None),
      Message(Level.Debug, "I think I'd prefer to stay inside...", None),
      Message(Level.Warn,  "Maybe you'll find someone else to help you.", None),
      Message(Level.Info,  "Maybe Black Mesa?", None),
      Message(Level.Error, "That was a joke. Ha Ha. Fat Chance!", None)
    )
  }
  
  it should "allow to control the log level" in {
    implicit val logger = SilentLogger[IO](Level.Off)
    StillAlive[IO].log.unsafeRunSync()

    logger.messages shouldBe empty
  }
}
