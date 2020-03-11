# [F]types

### Makes easy build FP programs in Scala by lifting.

[![Build Status](https://travis-ci.org/hilios/ftypes.svg?branch=master)](https://travis-ci.org/hilios/ftypes)

## Overview

[F]types is a collection of opinionated algebras for writing pure functional programming in Scala.
 
It aims to close the gap between impure code that it's needed to develop production grade software with the FP 
goodies available on [cats](https://github.com/typelevel/cats).

It embraces the [tagless final](https://blog.scalac.io/exploring-tagless-final.html) pattern to build modular
components that can be mixed and manipulated in a monadic style using for comprehension. 

Leveraging [cats-effect](https://github.com/typelevel/cats-effect) to suspend side-effects without making any
compromise about effect it self.

In practice one can use any implementation of `Effect[F]` type class - being `F` the effect class as 
cats `IO`, Monix `Task` – thus providing flexibility to developer to
choose which one fits best its own application.

The intention of the type classes found in this library it's to integrate out of the box with other libraries
that uses the *tagless final* approach: for instance any Typelevel projects
[doobie](http://tpolecat.github.io/doobie/),
[http4s](https://github.com/http4s/http4s),
[pureconfig](https://github.com/pureconfig/pureconfig),
[fs2](https://github.com/functional-streams-for-scala/fs2);

and/or with any other library once the it's lifted into a `Effect`.

## Install

The only hard dependency for this project are the `cats` and `cats-effect` libraries, and these must be provided 
by the user:

```sbt
scalacOptions += "-Ypartial-unification"

libraryDependencies += "org.typelevel"     %% "cats-core"    % "1.1.0"
libraryDependencies += "org.typelevel"     %% "cats-effect"  % "0.10.1"
libraryDependencies += "com.github.hilios" %% "ftypes-log"   % "0.1.0-SNAPSHOT"
libraryDependencies += "com.github.hilios" %% "ftypes-kamon" % "0.1.0-SNAPSHOT"
```

### License

Copyright (c) 2018 Edson Hilios. This is a free software is licensed under the MIT License.

*   [Edson Hilios](http://edson.hilios.com.br). Mail me: edson (at) hilios (dot) com (dot) br
