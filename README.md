# ftypes

### General purpose type classes for building FP programs in Scala.

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
libraryDependencies += "org.typelevel"     %% "cats-effect" % "1.0.0-RC"
libraryDependencies += "com.github.hilios" %% "ftypes-core" % "0.1.0-SNAPSHOT"
```

This will pull in the `ftype-core` module. Other functionalities can be imported as needed from other modules:

```sbt
libraryDependencies += "com.github.hilios" %% "ftypes-test-utils"  % "0.1.0-SNAPSHOT"
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

## Kafka

Basic type classes for creating Kafka consumers and producers.

### Consumer

Provides a dsl *á la* [http4s](https://http4s.org/) to create a topic consumers. Where the consumer is just a function `Record[F] => Return[F]` lifted on a effect, therefore, can be described as a `Kleisli[F, Record[F], Return[F]]`.

Declare your consumers as a partial function with pattern matching for the topics:

```scala
import ftypes.kafka.consumer._

object Service extends KafkaDsl {
  def consumers = KafkaConsumer {
    case msg @ Topic("tweets") => form {
      m <- msg.as[String]
    } yield ()

    case msg @ Topic("facebook") => form {
      m <- msg.as[String]
    } yield ()
  }
}
```

### License

Copyright (c) 2018 Edson Hilios. This is a free software is licensed under the MIT License.

*   [Edson Hilios](http://edson.hilios.com.br). Mail me: edson (at) hilios (dot) com (dot) br
