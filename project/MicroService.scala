import sbt.Keys._
import sbt.Tests.{SubProcess, Group}
import sbt._
import uk.gov.hmrc.sbtdistributables.SbtDistributablesPlugin
import uk.gov.hmrc.sbtdistributables.SbtDistributablesPlugin._
import uk.gov.hmrc.versioning.SbtGitVersioning

trait MicroService {

  import uk.gov.hmrc.DefaultBuildSettings._
  import TestPhases._
  import uk.gov.hmrc.SbtAutoBuildPlugin
  import scoverage.ScoverageSbtPlugin._

  val appName: String

  val validateHTML = taskKey[Unit]("Runs W3C NU html validator")
  val validateHTMLTask = validateHTML := {
    val log = ConsoleLogger(ConsoleOut.systemOut, true, true, ConsoleLogger.noSuppressedMessage)
    val logger = new ProcessLogger() {
      def buffer[T](f: => T): T = { f }
      def error(s: => String): Unit = { log.error(s) }
      def info(s: => String): Unit = {  }
    }
    val userDir = new File(System.getProperty("user.dir"))
    HTMLValidation.validationProcess(userDir) ! logger
  }
  lazy val appDependencies : Seq[ModuleID] = ???
  lazy val plugins : Seq[Plugins] = Seq(play.PlayScala)
  lazy val playSettings : Seq[Setting[_]] = Seq.empty

  lazy val scoverageSettings = {
  import scoverage.ScoverageKeys
    Seq(
      ScoverageKeys.coverageExcludedPackages :=  "<empty>;Reverse.*;paac.Routes.*;models/.data/..*;views.*;uk.gov.hmrc.*;prod.*;paac.*;config.*;connectors.*;connectors.*;.*assets.*;testOnlyDoNotUseInAppConf.*",
      ScoverageKeys.coverageMinimum := 90,
      ScoverageKeys.coverageFailOnMinimum := false,
      ScoverageKeys.coverageHighlighting := true,
      parallelExecution in Test := false
    )
  }

  lazy val microservice = Project(appName, file("."))
    .enablePlugins(plugins : _*)
    .settings(playSettings ++ scoverageSettings : _*)
    .settings(scalaSettings: _*)
    .settings(publishingSettings: _*)
    .settings(defaultSettings(): _*)
    .enablePlugins(SbtAutoBuildPlugin, SbtGitVersioning)
    .enablePlugins(SbtDistributablesPlugin)
    .settings(
      targetJvm := "jvm-1.8",
      libraryDependencies ++= appDependencies,
      parallelExecution in Test := false,
      fork in Test := false,
      retrieveManaged := true,
      commands <++= (baseDirectory in Test) { base => Seq(HTMLValidation.command(base))},
      validateHTMLTask
    )
    .configs(IntegrationTest)
    .settings(inConfig(IntegrationTest)(Defaults.itSettings): _*)
    .settings(
      Keys.fork in IntegrationTest := false,
      unmanagedSourceDirectories in IntegrationTest <<= (baseDirectory in IntegrationTest)(base => Seq(base / "it")),
      addTestReportOption(IntegrationTest, "int-test-reports"),
      testGrouping in IntegrationTest := oneForkedJvmPerTest((definedTests in IntegrationTest).value),
      parallelExecution in IntegrationTest := false)
    .disablePlugins(sbt.plugins.JUnitXmlReportPlugin)
    .settings(resolvers += Resolver.bintrayRepo("hmrc", "releases"))
}

private object TestPhases {
  def oneForkedJvmPerTest(tests: Seq[TestDefinition]) =
    tests map {
      test => new Group(test.name, Seq(test), SubProcess(ForkOptions(runJVMOptions = Seq("-Dtest.name=" + test.name))))
    }
}
