lazy val commonSettings = Seq(
  scalaVersion := "3.3.1"
)

val compilerPlugins = Seq(
)

val compilerOptions = Seq(
  scalacOptions -= "-Xfatal-warnings"
)

lazy val coreDependencies = {
  val cats = Seq(
    "org.typelevel" %% "cats-core" % "2.10.0",
    "org.typelevel" %% "cats-effect" % "3.5.1",
    "org.typelevel" %% "cats-mtl" % "1.3.1",
    "co.fs2" %% "fs2-core" % "3.9.2"
  )

  val logging = Seq(
    "ch.qos.logback" % "logback-classic" % "1.2.3",
    "org.typelevel" %% "log4cats-slf4j" % "2.6.0"
  )

  Seq(
    libraryDependencies ++= cats ++ logging
  )
}

lazy val rootDependencies = {
  val CirceVersion = "0.14.6"

  val circe = Seq(
    "io.circe" %% "circe-generic" % CirceVersion exclude ("aopalliance", "aopalliance"),
    "io.circe" %% "circe-parser" % CirceVersion
  )

  Seq(
    libraryDependencies ++= circe
  )
}

lazy val testSettings = {
  val dependencies =
    libraryDependencies ++= Seq(
      "org.scalactic" %% "scalactic" % "3.2.17",
      "org.scalatest" %% "scalatest" % "3.2.17"
    ).map(_ % Test)

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
    testSettings,
    libraryDependencies ++= Seq(
      "com.nrinaudo" %% "kantan.csv-java8"
    ).map(_ % "0.6.1").map(_ cross CrossVersion.for3Use2_13)
  )
  .dependsOn(core)

lazy val taxes = (project in file("taxes"))
  .settings(
    name := "stock-portfolio-tracker-taxes",
    commonSettings,
    compilerPlugins,
    compilerOptions,
    testSettings
  )
  .dependsOn(core)
