lazy val commonSettings = Seq(
  scalaVersion := "2.13.11"
  // scalaVersion := "3.3.0"
)

val compilerPlugins = Seq(
  // addCompilerPlugin("org.typelevel" % "kind-projector" % "0.11.3" cross CrossVersion.full)
)

val compilerOptions = Seq(
  scalacOptions -= "-Xfatal-warnings",
  // scalacOptions += "-Ykind-projector",
  scalacOptions += "-Ytasty-reader",
  // scalacOptions ++= Seq("-Ymacro-annotations")
)

lazy val coreDependencies = {
  val cats = Seq(
    "org.typelevel" %% "cats-core" % "2.6.1",
    "org.typelevel" %% "cats-effect" % "3.5.1",
    "org.typelevel" %% "cats-mtl" % "1.3.1",
    "co.fs2" %% "fs2-core" % "3.9.2"
  )//.map(_ cross CrossVersion.for2_13Use3)
  val logging = Seq(
    "ch.qos.logback" % "logback-classic" % "1.2.3",
    "org.typelevel" %% "log4cats-slf4j" % "2.6.0" //cross CrossVersion.for2_13Use3
  )

  // val others = Seq(
  //   "io.estatico" %% "newtype" % "0.4.4"
  // ).map(_ cross CrossVersion.for2_13Use3)

  Seq(
    libraryDependencies ++= cats ++ logging
  )
}

lazy val rootDependencies = {
  val CirceVersion = "0.14.6"

  val circe = Seq(
    "io.circe" %% "circe-generic" % CirceVersion exclude ("aopalliance", "aopalliance"),
    "io.circe" %% "circe-parser" % CirceVersion
  )//.map(_ cross CrossVersion.for2_13Use3)

  Seq(
    libraryDependencies ++= circe
  )
}

lazy val testSettings = {
  val dependencies = {
    libraryDependencies ++= Seq(
      "org.scalactic" %% "scalactic" % "3.2.17",
      "org.scalatest" %% "scalatest" % "3.2.17"
    ).map(_ % Test //cross CrossVersion.for2_13Use3
    )  
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
    coreDependencies,
    testSettings,
    libraryDependencies ++= Seq(
      "com.nrinaudo" %% "kantan.csv-java8",
      // "com.nrinaudo" %% "kantan.csv-cats",
      "com.nrinaudo" %% "kantan.csv-generic",
      // "com.nrinaudo" %% "kantan.csv-refined"
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
