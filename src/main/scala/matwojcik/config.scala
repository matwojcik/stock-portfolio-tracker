package matwojcik

object config {
  case class Config(http: HttpConfig)
  case class HttpConfig(host: String, port: Int)
}
