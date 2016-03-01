import sbt._

object FrontendBuild extends Build with MicroService {

  val appName = "paac-frontend"

  override lazy val appDependencies: Seq[ModuleID] = AppDependencies()
}

private object AppDependencies {
  import play.PlayImport._
  import play.core.PlayVersion

  private val playHealthVersion = "1.1.0"    
  private val playJsonLoggerVersion = "2.1.1"      
  private val frontendBootstrapVersion = "6.1.0"
  private val govukTemplateVersion = "4.0.0"
  private val playUiVersion = "4.9.0"
  private val playPartialsVersion = "4.2.0"
  private val playAuthorisedFrontendVersion = "4.5.0"
  private val playConfigVersion = "2.0.1"
  private val httpCachingClientVersion ="5.2.0"
  private val scalaTestVersion = "2.2.2"
  private val jSoupVersion = "1.7.3"
  private val mockitoVersion = "1.10.19"
  private val hmrcTestVersion = "1.4.0"
  private val pegDownVersion = "1.4.2"
  
  val compile = Seq(
    ws,
    "uk.gov.hmrc" %% "frontend-bootstrap" % frontendBootstrapVersion,
    "uk.gov.hmrc" %% "play-partials" % playPartialsVersion,
    "uk.gov.hmrc" %% "play-authorised-frontend" % playAuthorisedFrontendVersion,
    "uk.gov.hmrc" %% "play-config" % playConfigVersion,
    "uk.gov.hmrc" %% "play-json-logger" % playJsonLoggerVersion,
    "uk.gov.hmrc" %% "govuk-template" % govukTemplateVersion,
    "uk.gov.hmrc" %% "play-health" % playHealthVersion,
    "uk.gov.hmrc" %% "play-ui" % playUiVersion
  )

  trait TestDependencies {
    lazy val scope: String = "test"
    lazy val test : Seq[ModuleID] = ???
  }

  object Test {
    def apply() = new TestDependencies {
      override lazy val test = Seq(
        "uk.gov.hmrc" %% "http-caching-client" % httpCachingClientVersion,
        "uk.gov.hmrc" %% "hmrctest" % hmrcTestVersion % scope,
        "org.scalatest" %% "scalatest" % scalaTestVersion % scope,
        "com.typesafe.play" %% "play-test" % PlayVersion.current % scope,
        "org.mockito" % "mockito-all" % mockitoVersion % scope,
        "org.jsoup" % "jsoup" % jSoupVersion % scope,
        "org.pegdown" % "pegdown" % pegDownVersion % scope
      )
    }.test
  }

  def apply() = compile ++ Test()
}


