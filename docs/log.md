## Log

Suspend logs side effects into a `Effect` allowing it monadic composition.

```scala
import cats.effect.Effect
import cats.implicits._
import ftype.log._
import org.http4s._

case class UserService[F[_]](httpClient: Client[F])(implicit F: Effect[F], L: Log[F]) {

  def find(id: Long): F[Option[User]] = for {
    url       <- F.delay {
      Uri.unsafeFromString(s"https://host.com/users/$id")
    }
    _         <- L.info(s"Requesting user ID=$id")
    maybeUser <- httpClient.expect[Option[User]] recoverWith {
     case ex: Exception =>
       L.error("Error calling the users service", ex) *> F.raiseError(e)
    }
  } yield maybeUser
}
```

### Extras

The `ftypes.log` package offers two implementations that not rely on the SLF4J thus making a better choice for
testing environments – like unit or integration tests.

#### `SilentLog[F]`

Suppress all log messages from being outputed by the standard output collecting all log messages that can be inspected
later allowing to test the logs side-effects.

```scala
import cats.effect.IO
import ftypes.log._
import ftypes.log.logger._      

implicit val logger = SilentLog[IO]

def test(implicit L: Log[IO]): IO[Unit] = for {
  _ <- L.trace("Go ahead and leave me...")
  _ <- L.debug("I think I'd prefer to stay inside...")
  _ <- L.warn("Maybe you'll find someone else to help you.")
  _ <- L.info("Maybe Black Mesa?")
  _ <- L.error("That was a joke. Ha Ha. Fat Chance!")
} yield ()

test.unsafeRunSync()

logger.messages should contain allOf (
  Trace("Go ahead and leave me...", None),
  Debug("I think I'd prefer to stay inside...", None),
  Warn("Maybe you'll find someone else to help you.", None),
  Info("Maybe Black Mesa?", None),
  Error("That was a joke. Ha Ha. Fat Chance!", None)
)

```

#### `PrintLog[F]`

Provide a pretty print log implementation that will make your log output look beautiful.
