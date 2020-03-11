import sbt._

object Dependencies {

  val scala212 = "2.12.10"
  val scala213 = "2.13.1"
  
  val scala = Seq(scala212, scala213)

  val Version = new {
    val scalaMacroParadise = "2.1.0"
    val kindProjector      = "0.11.0"

    val betterMonadicFor = "0.3.1"

    val scalatest = "3.1.1"
    val scalamock = "4.1.0"

    val cats       = "2.1.1"
    val catsEffect = "2.1.2"

    val logback         = "1.2.3"
    val slf4j           = "1.7.30"
    val sourcecode      = "0.1.9"
    
    val kamonCore         = "2.0.5"
  }

  val Libs = new {
    val scalaMacros      = "org.scalamacros" %% "paradise"           % Version.scalaMacroParadise cross CrossVersion.patch
    val kindProjector    = "org.typelevel"   %% "kind-projector"     % Version.kindProjector  cross CrossVersion.full
    val betterMonadicFor = "com.olegpy"      %% "better-monadic-for" % Version.betterMonadicFor

    val cats             = "org.typelevel"   %% "cats-core"          % Version.cats
    val catsEffect       = "org.typelevel"   %% "cats-effect"        % Version.catsEffect

    // Logs
    val slf4j            = "org.slf4j"       % "slf4j-api"           % Version.slf4j
    val slf4jSimple      = "org.slf4j"       % "slf4j-simple"        % Version.slf4j
    val sourcecode       = "com.lihaoyi"     %% "sourcecode"         % Version.sourcecode

    // Kamon
    val kamonCore    = "io.kamon" %% "kamon-core"    % Version.kamonCore
    val kamonTestKit = "io.kamon" %% "kamon-testkit" % Version.kamonCore

    // Testing
    val scalaTest        = "org.scalatest"   %% "scalatest"          % Version.scalatest
  }
}
