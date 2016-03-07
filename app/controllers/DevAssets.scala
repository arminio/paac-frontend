/*
 * Copyright 2016 HM Revenue & Customs
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package controllers

/**
 * Created by adamconder on 18/06/15.
 */

import java.io.File

import play.Logger
import play.api.Play
import play.api.mvc.{Action, AnyContent, Controller}

object DevAssets extends Controller {

  import play.api.Play.current

  val AbsolutePath = """^(/|[a-zA-Z]:\\).*""".r

  def at(rootPath: String, file: String): Action[AnyContent] = Action { request =>
    val fileToServe = rootPath match {
      case AbsolutePath(_) => new File(rootPath, file)
      case _ => new File(Play.application.getFile(rootPath), file)
    }

    if (fileToServe.exists) {
      Ok.sendFile(fileToServe, inline = true)
    } else {
      Logger.error("DevAssets controller failed to serve a file: " + file)
      NotFound
    }
  }

}
