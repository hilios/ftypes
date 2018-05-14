
lazy val ftypes = (project in file(".")).
  aggregate(core, `test-utils`, kafka, `kafka-circe`)
  .settings(
    aggregate in update := false
  )

lazy val core = (project in file("core"))
  .settings(moduleName := "ftypes-core")
  .settings(
    inThisBuild(commonSettings),
    libraryDependencies ++= Seq(
      "org.slf4j"           % "slf4j-api"        % "1.8.0-beta2",
      "org.slf4j"           % "slf4j-simple"     % "1.8.0-beta2"
    )
  )

lazy val `test-utils` = (project in file("test"))
  .settings(moduleName := "ftypes-test")
  .dependsOn(core % "test->test;compile->compile")
  .settings(
    inThisBuild(commonSettings),
    libraryDependencies ++= Seq()
  )

lazy val kafka = (project in file("kafka"))
  .settings(moduleName := "ftypes-kafka")
  .dependsOn(core % "test->test;compile->compile", `test-utils` % "test->test")
  .settings(
    inThisBuild(commonSettings),
    libraryDependencies ++= Seq(
      "org.apache.kafka"   % "kafka-clients" % "1.1.0",
    )
  )

lazy val `kafka-circe` = (project in file("kafka-circe"))
  .settings(moduleName := "ftypes-kafka-circe")
  .dependsOn(kafka % "test->test;compile->compile", `test-utils` % "test->test")
  .settings(
    inThisBuild(commonSettings),
    libraryDependencies ++= Seq(
      "io.circe"           %% "circe-core"       % "0.9.3"   % Provided,
      "io.circe"           %% "circe-parser"     % "0.9.3"   % Provided,
      "io.circe"           %% "circe-literal"    % "0.9.3"   % Test
    )
  )

lazy val commonSettings = Seq(
  organization := "com.github.hilios",
  version := "0.1.0-SNAPSHOT",
  scalaOrganization := "org.typelevel",
  scalaVersion := "2.12.4-bin-typelevel-4",
  libraryDependencies ++= Seq(
    "org.scalatest"      %% "scalatest"        % "3.0.5"    % Test,
    "org.typelevel"      %% "cats-core"        % "1.1.0"    % Provided,
    "org.typelevel"      %% "cats-effect"      % "1.0.0-RC" % Provided,
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
    "-Yinduction-heuristics",
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
