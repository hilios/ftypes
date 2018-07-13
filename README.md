# ftypes

### General purpose type classes for building FP programs in Scala.

[![Build Status](https://travis-ci.org/hilios/ftypes.svg?branch=master)](https://travis-ci.org/hilios/ftypes)

## Overview

Ftypes is a collection of opinionated, general purpose type classes for writing pure functional programming in Scala. 
Providing common functionality that several applications needs as logging or managing the lifecycle and dependencies of
software components which have runtime state.  

It embraces the [tagless final](https://blog.scalac.io/exploring-tagless-final.html) pattern to build modular
components that can be mixed and manipulated in a monadic style using for comprehension. 
Leveraging [cats-effect](https://github.com/typelevel/cats-effect) to suspend side-effects without making any
compromise about effect it self.

In practice one can use any implementation of `Effect[F]` type class - being `F` the effect class as 
cats `IO`, Monix `Task` or even Scala's `Future` :confounded: – thus providing flexibility to developer to
choose which one fits best its own application.

The intetion of the type classes found in this library it's to integrate out of the box with other libraries
that uses the *tagless final* approach: for instance any Typelevel projects
[doobie](http://tpolecat.github.io/doobie/),
[http4s](https://github.com/http4s/http4s),
[pureconfig](https://github.com/pureconfig/pureconfig),
[fs2](https://github.com/functional-streams-for-scala/fs2);
and others like the upcoming
[elastic4s](https://github.com/sksamuel/elastic4s),
[scalacache](https://github.com/cb372/scalacache), and so on.
Nevertheless it can be ealisy integrated with any other library once the it's lifted into a `Effect`.

## Install

The only hard dependency for this project are the `cats` and `cats-effect` libraries, and these must be provided 
by the user:

```sbt
scalacOptions += "-Ypartial-unification"

libraryDependencies += "org.typelevel"     %% "cats-core"   % "1.1.0"
libraryDependencies += "org.typelevel"     %% "cats-effect" % "0.10.1"
libraryDependencies += "com.github.hilios" %% "ftypes-core" % "0.1.0-SNAPSHOT"
```

This will pull in the `ftype-core` module. Other functionalities can be imported as needed from other modules:

```sbt
libraryDependencies += "com.github.hilios" %% "ftypes-kamon"       % "0.1.0-SNAPSHOT"
libraryDependencies += "com.github.hilios" %% "ftypes-kafka"       % "0.1.0-SNAPSHOT"
libraryDependencies += "com.github.hilios" %% "ftypes-kafka-circe" % "0.1.0-SNAPSHOT"
```

## Logging

Lift a SLF4J Logger instance into a `Effect` suspending side effects and allowing monadic composition.

```scala
import cats.effect.Effect
import cats.implicits._
import ftype.Logging
import org.http4s._
import org.http4s.circe._

case class UserService[F[_]](httpClient: Client[F])(implicit F: Effect[F], L: Logging[F]) {

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

def test(implicit L: Logger[IO]): IO[Unit] = for {
  _ <- L.trace("Go ahead and leave me...")
  _ <- L.debug("I think I'd prefer to stay inside...")
  _ <- L.warn("Maybe you'll find someone else to help you.")
  _ <- L.info("Maybe Black Mesa?")
  _ <- L.error("That was a joke. Ha Ha. Fat Chance!")
} yield ()

implicit val log = SilentLog[IO]

test.unsafeRunSync()

log.messages should contain allOf (
  Trace("Go ahead and leave me...", None),
  Debug("I think I'd prefer to stay inside...", None),
  Warn("Maybe you'll find someone else to help you.", None),
  Info("Maybe Black Mesa?", None),
  Error("That was a joke. Ha Ha. Fat Chance!", None)
)

```

#### `PrintLog[F]`

Provide a pretty print log implementation that will make your log output look beautiful.

## Kafka

Basic type classes for creating Kafka consumers and producers.

### Consumer

Provides a DSL similar to [http4s](https://http4s.org/) to create a topic consumers. Where the consumer is just a function `Record[F] => Return[F]` lifted on a effect, therefore, can be described as a `Kleisli[F, Record[F], Return[F]]`.

Declare your consumers as a partial function with pattern matching for the topics:

```scala
import ftypes.kafka._

trait Consumers extends KafkaDsl {
  def consumers = KafkaConsumer {
    case msg @ Topic("tweets") => for {
      m <- msg.as[String]
    } yield ()

    case msg @ Topic("facebook") => for {
      m <- msg.as[String]
    } yield ()
  }
}
```

#### `SimpleKafkaConsumer[F]`

```scala
import cats.effect.IO
import ftypes.kafka.io._
import java.util.Properties
import org.apache.kafka.clients.consumer.KafkaConsumer
import org.apache.kafka.common.serialization.ByteArraySerializer

object Main extends Consumers {
  def main(args: Array[String]): Unit = {
    val props = new Properties()
    props.put("bootstrap.servers", "localhost:9092")

    val kafka = new KafkaConsumer[ByteArray, ByteArray](props)

    val consumer = new SimpleKafkaConsumer[IO](kafka, "my-topic-1", "my-topic-2")

    consumer.mountConsumer(consumers) // Start consumer
    consumer.stop()
  }
}
```

#### `SimpleKafkaProducer[F]`

```scala
import cats.effect.IO
import ftypes.kafka.io._
import java.util.Properties
import org.apache.kafka.clients.producer.KafkaProducer
import org.apache.kafka.common.serialization.ByteArraySerializer

object Main {
  def main(args: Array[String]): Unit = {
    val props = new Properties()
    props.put("bootstrap.servers", "localhost:9092")

    val kafka = new KafkaProducer[ByteArray, ByteArray](props)

    val producer = new SimpleKafkaProducer[IO](kafka)
    
    producer.produce("my-topic", "Hello, World!")
  }
}
```

### License

Copyright (c) 2018 Edson Hilios. This is a free software is licensed under the MIT License.

*   [Edson Hilios](http://edson.hilios.com.br). Mail me: edson (at) hilios (dot) com (dot) br
