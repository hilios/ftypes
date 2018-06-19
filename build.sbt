
lazy val ftypes = (project in file(".")).
  aggregate(core, kafka, `kafka-circe`, kamon)
  .settings(
    aggregate in update := false
  )

lazy val core = (project in file("core"))
  .settings(moduleName := "ftypes-core")
  .settings(
    inThisBuild(commonSettings),
    libraryDependencies ++= Seq(
      "org.slf4j" % "slf4j-api"    % "1.8.0-beta2",
      "org.slf4j" % "slf4j-simple" % "1.8.0-beta2" % Test,
      compilerPlugin("org.scalamacros" %% "paradise" % "2.1.0" cross CrossVersion.patch)
    )
  )

lazy val kafka = (project in file("kafka"))
  .settings(moduleName := "ftypes-kafka")
  .dependsOn(core % "test->test;compile->compile")
  .settings(
    inThisBuild(commonSettings),
    libraryDependencies ++= Seq(
      "org.apache.kafka"   % "kafka-clients" % "1.1.0" % Provided,
    )
  )

lazy val `kafka-circe` = (project in file("kafka-circe"))
  .settings(moduleName := "ftypes-kafka-circe")
  .dependsOn(kafka % "test->test;compile->compile")
  .settings(
    inThisBuild(commonSettings),
    libraryDependencies ++= Seq(
      "io.circe"           %% "circe-core"       % "0.9.3" % Provided,
      "io.circe"           %% "circe-parser"     % "0.9.3" % Provided,
      "io.circe"           %% "circe-literal"    % "0.9.3" % Test,
      "org.apache.kafka"    % "kafka-clients"    % "1.1.0" % Test,
    )
  )

lazy val kamon = (project in file("kamon"))
  .settings(moduleName := "ftypes-kamon")
  .dependsOn(core % "test->test;compile->compile")
  .settings(
    inThisBuild(commonSettings),
    libraryDependencies ++= Seq(
      "io.kamon" %% "kamon-core" % "1.1.2"
    )
  )

lazy val commonSettings = Seq(
  organization := "com.github.hilios",
  version := "0.1.0-SNAPSHOT",
  crossScalaVersions := Seq("2.11.12", "2.12.6"),
  libraryDependencies ++= Seq(
    "org.scalatest"      %% "scalatest"        % "3.0.5"     % Test,
    "org.typelevel"      %% "cats-core"        % "1.1.0"     % Provided,
    "org.typelevel"      %% "cats-effect"      % "1.0.0-RC2" % Provided,
    compilerPlugin("org.spire-math" %% "kind-projector" % "0.9.6")
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
