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

package controllers.action

import play.api.mvc._

class DataRequest[A](val data: Map[String,String], val request: Request[A])
  extends WrappedRequest[A](request) {
  lazy val form: Map[String, String] = {
    val maybeFormData = request.body match {
      case body @ AnyContentAsXml(_) => body.asFormUrlEncoded
      case body @ AnyContentAsText(_) => body.asFormUrlEncoded
      case body @ AnyContentAsMultipartFormData(_) => body.asFormUrlEncoded
      case body @ AnyContentAsRaw(_) => body.asFormUrlEncoded
      case body @ AnyContentAsJson(_) => body.asFormUrlEncoded
      case body @ AnyContentAsFormUrlEncoded(_) => body.asFormUrlEncoded
      case _ => None
    }
    maybeFormData.getOrElse(Map[String, Seq[String]]()).mapValues(_.head)
  }
}