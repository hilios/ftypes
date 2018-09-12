val Version = new {
  val scala = Seq("2.11.12", "2.12.6")
  val scalaMacroParadise = "2.1.0"
  val kindProjector      = "0.9.6"

  val scalatest = "3.0.5"
  val scalamock = "4.1.0"

  val cats       = "1.1.0"
  val catsEffect = "0.10.1"

  val logback         = "1.2.3"
  val slf4j           = "1.7.25"
  val sourcecode      = "0.1.4"

  val kamon              = "1.1.3"
  val kamonExecutors     = "1.0.2"
  val kamonTestKit       = "1.1.1"

  val circe  = "0.9.3"
  val kafka  = "1.0.2"
}

lazy val ftypes = (project in file(".")).
  aggregate(log, kamon, kafka, `kafka-circe`)
  .settings(
    aggregate in update := false
  )

lazy val log = (project in file("log"))
  .settings(moduleName := "ftypes-log")
  .settings(
    inThisBuild(commonSettings),
    libraryDependencies ++= Seq(
      "org.slf4j"    % "slf4j-api"    % Version.slf4j,
      "org.slf4j"    % "slf4j-simple" % Version.slf4j % Test,
      compilerPlugin("org.scalamacros" %% "paradise" % Version.scalaMacroParadise cross CrossVersion.patch)
    )
  )

lazy val kamon = (project in file("kamon"))
  .settings(moduleName := "ftypes-kamon")
  .settings(
    inThisBuild(commonSettings),
    libraryDependencies ++= Seq(
      "io.kamon" %% "kamon-core"      % Version.kamon,
      "io.kamon" %% "kamon-executors" % Version.kamonExecutors,
      "io.kamon" %% "kamon-testkit"   % Version.kamonTestKit % Test
    ),
  )

lazy val kafka = (project in file("kafka"))
  .settings(moduleName := "ftypes-kafka")
  .configs(IntegrationTest)
  .settings(Defaults.itSettings)
  .dependsOn(log % "test->test;compile->compile")
  .settings(
    inThisBuild(commonSettings),
    libraryDependencies ++= Seq(
      "org.apache.kafka" % "kafka-clients"   % Version.kafka,
      "org.slf4j"        % "slf4j-api"       % Version.slf4j     % Provided,
      "ch.qos.logback"   % "logback-classic" % Version.logback   % IntegrationTest,
      "org.scalatest"   %% "scalatest"       % Version.scalatest % IntegrationTest
    )
  )

lazy val `kafka-circe` = (project in file("kafka-circe"))
  .settings(moduleName := "ftypes-kafka-circe")
  .dependsOn(kafka % "test->test;compile->compile")
  .settings(
    inThisBuild(commonSettings),
    libraryDependencies ++= Seq(
      "io.circe"           %% "circe-core"       % Version.circe % Provided,
      "io.circe"           %% "circe-parser"     % Version.circe % Provided,
      "io.circe"           %% "circe-literal"    % Version.circe % Test,
      "org.apache.kafka"    % "kafka-clients"    % Version.kafka % Test,
    )
  )

lazy val `kafka-kamon` = (project in file("kafka-kamon"))
  .settings(moduleName := "ftype-kafka-kamon")
  .configs(IntegrationTest)
  .settings(Defaults.itSettings)
  .dependsOn(
    kafka % "test->test;compile->compile",
    kamon % "test->test;compile->compile"
  )
  .settings(
    inThisBuild(commonSettings),
    libraryDependencies ++= Seq(
      "ch.qos.logback"    % "logback-classic" % Version.logback   % IntegrationTest,
      "org.scalatest"    %% "scalatest"       % Version.scalatest % IntegrationTest,
    )
  )

lazy val commonSettings = Seq(
  organization := "com.github.hilios",
  version := "0.1.0-SNAPSHOT",
  crossScalaVersions := Version.scala,
  libraryDependencies ++= Seq(
    "org.typelevel" %% "cats-core"   % Version.cats       % Provided,
    "org.typelevel" %% "cats-effect" % Version.catsEffect % Provided,
    "org.scalatest" %% "scalatest"   % Version.scalatest  % Test,
    compilerPlugin("org.spire-math" %% "kind-projector" % Version.kindProjector)
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
