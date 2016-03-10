import com.typesafe.sbt.packager.Keys._
import sbt.Keys._
import sbt._

/**
 * Build of UI in JavaScript
 */
object JavaScriptBuild {

  import play.PlayImport.PlayKeys._

  val jsDirectory = SettingKey[File]("js-directory")

  val gruntBuild = TaskKey[Int]("grunt-build")
  val gruntWatch = TaskKey[Int]("grunt-watch")
  val gruntTest = TaskKey[Int]("grunt-test")
  val npmInstall = TaskKey[Int]("npm-install")


  val javaScriptUiSettings = Seq(

    // the JavaScript application resides in "ui"
    jsDirectory <<= (baseDirectory in Compile) { _ /"app" / "assets" / "js"},

    // add "npm" and "grunt" commands in sbt
    commands <++= jsDirectory { base => Seq(Grunt.gruntCommand(base), npmCommand(base))},

    npmInstall := Grunt.npmProcess(jsDirectory.value, "install").run().exitValue(),
    gruntBuild := Grunt.gruntProcess(jsDirectory.value, "prod").run().exitValue(),

    gruntWatch := Grunt.gruntProcess(jsDirectory.value, "watch").run().exitValue(),
    gruntTest := Grunt.gruntProcess(jsDirectory.value, "test").run().exitValue(),

    gruntTest <<= gruntTest dependsOn npmInstall,
    gruntBuild <<= gruntBuild dependsOn npmInstall,

    // runs grunt before staging the application
    dist <<= dist dependsOn gruntBuild,

    (test in Test) <<= (test in Test) dependsOn gruntTest,

    // Turn off play's internal less compiler
    lessEntryPoints := Nil,

    // Turn off play's internal JavaScript and CoffeeScript compiler
    javascriptEntryPoints := Nil,
    coffeescriptEntryPoints := Nil,

    // integrate JavaScript build into play build
    playRunHooks <+= jsDirectory.map(ui => Grunt(ui))
  )

  def npmCommand(base: File) = Command.args("npm", "<npm-command>") { (state, args) =>
    Process("npm" :: args.toList, base) !;
    state
  }

}
