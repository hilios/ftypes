import Dependencies._

ThisBuild / organization := "com.github.hilios"
ThisBuild / version      := "0.1.0-SNAPSHOT"
ThisBuild / scalaVersion := scala212

lazy val ftypes = (project in file("."))
  .aggregate(log, kamon)
  .settings(
    aggregate in update := false
  )

lazy val log = (project in file("log"))
  .settings(moduleName := "ftypes-log")
  .settings(
    inThisBuild(commonSettings),
    libraryDependencies ++= Seq(
      Libs.catsEffect,
      Libs.slf4j,
      Libs.sourcecode,
      Libs.slf4jSimple % Test,
      Libs.scalaTest   % Test,
      compilerPlugin(Libs.scalaMacros),
    )
  )

lazy val kamon = (project in file("kamon"))
  .settings(moduleName := "ftypes-kamon")
  .settings(
    inThisBuild(commonSettings),
    libraryDependencies ++= Seq(
      Libs.catsEffect,
      "io.kamon" %% "kamon-core"      % Version.kamon,
      "io.kamon" %% "kamon-executors" % Version.kamonExecutors,
      "io.kamon" %% "kamon-testkit"   % Version.kamonTestKit % Test
    ),
    dependencyOverrides ++= Seq(
      "io.kamon" %% "kamon-core"      % Version.kamon,
    )
  )

lazy val commonSettings = Seq(
  crossScalaVersions := Dependencies.scala,
  libraryDependencies ++= Seq(
    Libs.cats,
    compilerPlugin(Libs.kindProjector),
    compilerPlugin(Libs.betterMonadicFor),
  ),
  autoCompilerPlugins := true,
  fork in test := true,
  parallelExecution in Test := false,
  scalacOptions ++= Seq(
    "-target:jvm-1.8",
    "-encoding", "utf-8",
    "-deprecation",
    "-explaintypes",
    "-feature",
    "-language:reflectiveCalls",
    "-language:existentials",
    "-language:experimental.macros",
    "-language:higherKinds",
    "-language:implicitConversions",
    "-unchecked",
    "-Xcheckinit",
    "-Xfuture",
    "-Xlint:adapted-args",
    "-Xlint:by-name-right-associative",
    "-Xlint:constant",
    "-Xlint:delayedinit-select",
    "-Xlint:doc-detached",
    "-Xlint:inaccessible",
    "-Xlint:infer-any",
    "-Xlint:missing-interpolator",
    "-Xlint:nullary-override",
    "-Xlint:nullary-unit",
    "-Xlint:option-implicit",
    "-Xlint:package-object-classes",
    "-Xlint:poly-implicit-overload",
    "-Xlint:private-shadow",
    "-Xlint:stars-align",
    "-Xlint:type-parameter-shadow",
    "-Xlint:unsound-match",
    "-Xlog-free-terms",
    "-Yno-adapted-args",
    "-Ypartial-unification",
    "-Ywarn-dead-code",
    "-Ywarn-extra-implicit",
    "-Ywarn-inaccessible",
    "-Ywarn-infer-any",
    "-Ywarn-nullary-override",
    "-Ywarn-nullary-unit",
    "-Ywarn-numeric-widen",
    "-Ywarn-unused:implicits",
    "-Ywarn-unused:imports",
    "-Ywarn-unused:locals",
    "-Ywarn-unused:params",
    "-Ywarn-unused:patvars",
    "-Ywarn-unused:privates",
    "-Ywarn-value-discard"
  ),
  scalacOptions in Compile := (scalacOptions in Compile).value.filter(_ != "-Xfatal-warnings")
)
