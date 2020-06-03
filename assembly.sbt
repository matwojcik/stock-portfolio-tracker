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
