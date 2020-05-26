lazy val root = (project in file(".")).settings(
  commonSettings,
  compilerPlugins,
  compilerOptions,
  dependencies,
  testSettings
)

lazy val commonSettings = Seq(
  name := "stock-portfolio-tracker",
  scalaVersion := "2.13.2"
)

val compilerPlugins = Seq(
  addCompilerPlugin("org.typelevel" % "kind-projector" % "0.11.0" cross CrossVersion.full),
  addCompilerPlugin("com.kubukoz" % "better-tostring" % "0.2.2" cross CrossVersion.full),
  addCompilerPlugin("com.olegpy" %% "better-monadic-for" % "0.3.1")
)

val compilerOptions = Seq(
  scalacOptions -= "-Xfatal-warnings",
  scalacOptions ++= Seq("-Ymacro-annotations")
)

lazy val dependencies = {
  val CirceVersion = "0.13.0"

  val cats = Seq(
    "org.typelevel" %% "cats-core" % "2.1.0",
    "org.typelevel" %% "cats-effect" % "2.1.3",
    "org.typelevel" %% "cats-mtl-core" % "0.7.0",
    "com.github.mpilquist" %% "simulacrum" % "0.19.0",
    "org.typelevel" %% "cats-tagless-macros" % "0.11"
  )

  val config = Seq(
    "com.github.pureconfig" %% "pureconfig" % "0.12.3",
    "eu.timepit" %% "refined-pureconfig" % "0.9.14"
  )

  val logging = Seq(
    "ch.qos.logback" % "logback-classic" % "1.2.3",
    "com.typesafe.scala-logging" %% "scala-logging" % "3.9.2",
    "io.chrisdavenport" %% "log4cats-slf4j" % "1.1.1"
  )

  val circe = Seq(
    "io.circe" %% "circe-generic" % CirceVersion exclude ("aopalliance", "aopalliance"),
    "io.circe" %% "circe-parser" % CirceVersion
  )

  val others = Seq(
    "io.estatico" %% "newtype" % "0.4.4"
  )

  Seq(
    libraryDependencies ++= cats ++ config ++ logging ++ circe ++ others
  )
}

lazy val testSettings = {
  val dependencies = {
    libraryDependencies ++= Seq(
      "org.scalactic" %% "scalactic" % "3.1.2",
      "org.scalatest" %% "scalatest" % "3.1.2",
      "com.ironcorelabs" %% "cats-scalatest" % "3.0.5"
    ).map(_ % Test)
  }

  Seq(
    logBuffered in Test := false,
    dependencies
  )
}

import sbtassembly.MergeStrategy

test in assembly := {}

assemblyJarName in assembly := name.value + ".jar"

assemblyOutputPath in assembly := file("target/out/" + (assemblyJarName in assembly).value)

publishArtifact in (Compile, packageDoc) := false

assemblyMergeStrategy in assembly := {
  case PathList("javax", "jms", xs @ _*)                   => MergeStrategy.first
  case PathList(ps @ _*) if ps.last == "overview.html"     => MergeStrategy.first
  case PathList(ps @ _*) if ps.last == "RELEASE.txt"       => MergeStrategy.first
  case PathList(ps @ _*) if ps.last == "LICENSE-2.0.txt"   => MergeStrategy.first
  case PathList(ps @ _*) if ps.last endsWith ".properties" => MergeStrategy.concat
  case x if x.contains("pureconfig")                       => MergeStrategy.first
  case x if x.contains("netty")                            => MergeStrategy.last
  case x if x.contains("aop.xml")                          => MergeStrategy.last
  case x if x.contains("cinnamon-reference.conf")          => MergeStrategy.concat
  case PathList("META-INF", "aop.xml")                     => aopMergeStrategy
  case x =>
    val oldStrategy = (assemblyMergeStrategy in assembly).value
    oldStrategy(x)
}

val aopMergeStrategy: MergeStrategy = new MergeStrategy {
  val name = "aopMerge"

  import scala.xml._
  import scala.xml.dtd._

  def apply(tempDir: File, path: String, files: Seq[File]): Either[String, Seq[(File, String)]] = {
    val dt = DocType("aspectj", PublicID("-//AspectJ//DTD//EN", "http://www.eclipse.org/aspectj/dtd/aspectj.dtd"), Nil)
    val file = MergeStrategy.createMergeTarget(tempDir, path)
    val xmls: Seq[Elem] = files.map(XML.loadFile)
    val aspectsChildren: Seq[Node] = xmls.flatMap(_ \\ "aspectj" \ "aspects" \ "_")
    val weaverChildren: Seq[Node] = xmls.flatMap(_ \\ "aspectj" \ "weaver" \ "_")
    val options: String = xmls.map(x => (x \\ "aspectj" \ "weaver" \ "@options").text).mkString(" ").trim
    val weaverAttr = if (options.isEmpty) Null else new UnprefixedAttribute("options", options, Null)
    val aspects = new Elem(null, "aspects", Null, TopScope, false, aspectsChildren: _*)
    val weaver = new Elem(null, "weaver", weaverAttr, TopScope, false, weaverChildren: _*)
    val aspectj = new Elem(null, "aspectj", Null, TopScope, false, aspects, weaver)
    XML.save(file.toString, aspectj, "UTF-8", xmlDecl = false, dt)
    IO.append(file, IO.Newline.getBytes(IO.defaultCharset))
    Right(Seq(file -> path))
  }
}
