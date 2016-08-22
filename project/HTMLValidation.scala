import play.PlayRunHook
import sbt._

/*
  Grunt runner
*/
object HTMLValidation {
  def apply(base: File): PlayRunHook = {

    object HTMLValidationProcess extends PlayRunHook {

      var validationRun: Option[Process] = None

      override def beforeStarted(): Unit = {
        val env = "dev"
        validationRun = Some(validationProcess(base, env).run)
      }

      override def afterStopped(): Unit = {
        // Stop when play run stops
       validationRun.map(p => p.destroy())
       validationRun = None
      }

    }

    HTMLValidationProcess
  }

  def command(base: File) = Command.command("w3c") { state =>
    validationProcess(base) !;
    state
  }

  def validationProcess(base: File, args: String*) = {
    // have to use Process rather than fork because of NPE with get class loader in vnu tool
    val log = ConsoleLogger()
    log.info("Starting HTML Validation task...")
    Process("java" :: "-jar" :: s"${base.getAbsolutePath}/project/tools/vnu.jar" :: "--skip-non-html" :: "--verbose" :: "--format" :: "text" :: "--html" :: "--errors-only" :: s"${base.getAbsolutePath}/target/html" :: ">" :: "out.txt" :: args.toList, base)
  }
}
