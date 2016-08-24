import sbt._

object FrontendBuild extends Build with MicroService {

  import com.typesafe.sbt.web.SbtWeb.autoImport._
  import play.PlayImport.PlayKeys._
  import sbt.Keys._

  val appName = "paac-frontend"

  override lazy val appDependencies: Seq[ModuleID] = AppDependencies()

  // play settings
  override lazy val playSettings : Seq[Setting[_]] = Seq(
    routesImport ++= Seq("uk.gov.hmrc.domain._"),
    // Turn off play's internal less compiler
    lessEntryPoints := Nil,
    // Turn off play's internal javascript compiler
    javascriptEntryPoints := Nil,
    // Add the views to the dist
    unmanagedResourceDirectories in Assets += baseDirectory.value / "app" / "assets",
    // Dont include the source assets in the dist package (public folder)
    excludeFilter in Assets := "js*" || "sass*" || "img*",
    test in Test <<= (validateHTML in Test) dependsOn (test in Test)
  )
}

private object AppDependencies {
  import play.PlayImport._
  import play.core.PlayVersion

  private val playHealthVersion = "1.1.0"
  private val playJsonLoggerVersion = "2.1.1"
  private val frontendBootstrapVersion = "6.4.0"
  private val govukTemplateVersion = "4.0.0"
  private val playUiVersion = "4.9.0"
  private val playPartialsVersion = "4.2.0"
  private val playAuthorisedFrontendVersion = "4.5.0"
  private val playConfigVersion = "2.0.1"
  private val httpCachingClientVersion ="5.2.0"
  private val scalaTestVersion = "2.2.2"
  private val scalaTestPlusVersion = "1.2.0"
  private val jSoupVersion = "1.7.3"
  private val mockitoVersion = "1.10.19"
  private val hmrcTestVersion = "1.4.0"
  private val pegDownVersion = "1.4.2"
  private val playWhitelistFilterVersion = "1.1.0"
  private val playMetrics = "2.3.0_0.2.1"
  private val metricsGraphite = "3.0.2"

  val compile = Seq(
    ws,
    "uk.gov.hmrc" %% "frontend-bootstrap" % frontendBootstrapVersion,
    "uk.gov.hmrc" %% "play-partials" % playPartialsVersion,
    "uk.gov.hmrc" %% "play-authorised-frontend" % playAuthorisedFrontendVersion,
    "uk.gov.hmrc" %% "play-config" % playConfigVersion,
    "uk.gov.hmrc" %% "play-json-logger" % playJsonLoggerVersion,
    "uk.gov.hmrc" %% "govuk-template" % govukTemplateVersion,
    "uk.gov.hmrc" %% "play-health" % playHealthVersion,
    "uk.gov.hmrc" %% "play-ui" % playUiVersion,
    "uk.gov.hmrc" %% "http-caching-client" % httpCachingClientVersion,
    "uk.gov.hmrc" %% "secure" % "7.0.0",
    "uk.gov.hmrc" %% "play-whitelist-filter" % playWhitelistFilterVersion,
    "com.kenshoo" %% "metrics-play" % playMetrics,
    "com.codahale.metrics" % "metrics-graphite" % metricsGraphite
  )

  trait TestDependencies {
    lazy val scope: String = "test"
    lazy val test : Seq[ModuleID] = Seq()
  }

  object Test {
    def apply() = new TestDependencies {
      override lazy val test = Seq(
        "uk.gov.hmrc" %% "hmrctest" % hmrcTestVersion % scope,
        "org.scalatest" %% "scalatest" % scalaTestVersion % scope,
        "org.scalatestplus" %% "play" % scalaTestPlusVersion % scope,
        "com.typesafe.play" %% "play-test" % PlayVersion.current % scope,
        "org.mockito" % "mockito-all" % mockitoVersion % scope,
        "org.jsoup" % "jsoup" % jSoupVersion % scope,
        "org.pegdown" % "pegdown" % pegDownVersion % scope
      )
    }.test
  }

  def apply() = { compile ++ Test() }
}


