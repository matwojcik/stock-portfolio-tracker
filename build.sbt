lazy val commonSettings = Seq(
  scalaVersion := "2.13.4"
)

val compilerPlugins = Seq(
  addCompilerPlugin("org.typelevel" % "kind-projector" % "0.11.3" cross CrossVersion.full),
  addCompilerPlugin("com.kubukoz" % "better-tostring" % "0.2.6" cross CrossVersion.full),
  addCompilerPlugin("com.olegpy" %% "better-monadic-for" % "0.3.1")
)

val compilerOptions = Seq(
  scalacOptions -= "-Xfatal-warnings",
  scalacOptions ++= Seq("-Ymacro-annotations")
)

lazy val coreDependencies = {
  val cats = Seq(
    "org.typelevel" %% "cats-core" % "2.3.1",
    "org.typelevel" %% "cats-effect" % "2.3.0",
    "org.typelevel" %% "cats-mtl-core" % "0.7.1",
    "com.github.mpilquist" %% "simulacrum" % "0.19.0",
    "org.typelevel" %% "cats-tagless-macros" % "0.12",
    "com.olegpy" %% "meow-mtl-core" % "0.4.1",
    "com.olegpy" %% "meow-mtl-effects" % "0.4.1",
    "co.fs2" %% "fs2-core" % "2.3.0"
  )
  val logging = Seq(
    "ch.qos.logback" % "logback-classic" % "1.2.3",
    "com.typesafe.scala-logging" %% "scala-logging" % "3.9.2",
    "io.chrisdavenport" %% "log4cats-slf4j" % "1.1.1"
  )

  val others = Seq(
    "io.estatico" %% "newtype" % "0.4.4"
  )

  Seq(
    libraryDependencies ++= cats ++ logging ++ others
  )
}

lazy val rootDependencies = {
  val CirceVersion = "0.13.0"
  val config = Seq(
    "com.github.pureconfig" %% "pureconfig" % "0.14.0",
    "eu.timepit" %% "refined-pureconfig" % "0.9.20"
  )

  val circe = Seq(
    "io.circe" %% "circe-generic" % CirceVersion exclude ("aopalliance", "aopalliance"),
    "io.circe" %% "circe-parser" % CirceVersion
  )

  Seq(
    libraryDependencies ++= config ++ circe
  )
}

lazy val testSettings = {
  val dependencies = {
    libraryDependencies ++= ((if (scalaVersion.value.startsWith("2.")) Seq("com.ironcorelabs" %% "cats-scalatest" % "3.1.1")
                              else Seq.empty) ++ Seq(
      "org.scalactic" %% "scalactic" % "3.2.3",
      "org.scalatest" %% "scalatest" % "3.2.3"
    )).map(_ % Test)

  }

  Seq(
    logBuffered in Test := false,
    dependencies
  )
}

lazy val root = (project in file("."))
  .settings(
    name := "stock-portfolio-tracker",
    commonSettings,
    compilerPlugins,
    compilerOptions,
    rootDependencies,
    testSettings
  )
  .settings(scalacOptions += "-Ytasty-reader")
  .aggregate(core, portfolio, history, reporting, prices, importing, taxes)
  .dependsOn(portfolio, history, reporting, prices, importing, taxes)

lazy val core = (project in file("core")).settings(
  name := "stock-portfolio-tracker-core",
  commonSettings,
  compilerPlugins,
  compilerOptions,
  coreDependencies,
  testSettings
)

lazy val portfolio = (project in file("portfolio"))
  .settings(
    name := "stock-portfolio-tracker-portfolio",
    commonSettings,
    compilerPlugins,
    compilerOptions,
    testSettings
  )
  .dependsOn(core)

lazy val history = (project in file("history"))
  .settings(
    name := "stock-portfolio-tracker-history",
    commonSettings,
    compilerPlugins,
    compilerOptions,
    testSettings
  )
  .dependsOn(core)

lazy val reporting = (project in file("reporting"))
  .settings(
    name := "stock-portfolio-tracker-reporting",
    commonSettings,
    compilerPlugins,
    compilerOptions,
    testSettings
  )
  .dependsOn(core)

lazy val prices = (project in file("prices"))
  .settings(
    name := "stock-portfolio-tracker-prices",
    commonSettings,
    compilerPlugins,
    compilerOptions,
    testSettings
  )
  .dependsOn(core)

lazy val importing = (project in file("importing"))
  .settings(
    name := "stock-portfolio-tracker-importing",
    commonSettings,
    compilerPlugins,
    compilerOptions,
    testSettings
  )
  .dependsOn(core)

lazy val taxes = (project in file("taxes"))
  .settings(
    name := "stock-portfolio-tracker-taxes",
    commonSettings,
    compilerOptions,
    testSettings
  )
  .settings(scalaVersion := "3.0.0-M3")
  .dependsOn(core)
