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

import scala.concurrent.Future
import play.api.mvc._

import service._
import service.KeystoreService._
import uk.gov.hmrc.play.http.{HeaderCarrier, SessionKeys}
import scala.util.{Try, Success, Failure, Either}
import play.api.libs.json._
import play.api.libs.concurrent.Execution.Implicits._

case class ReadKeystoreAction(keystore: KeystoreService) extends ActionBuilder[DataRequest] {
  def invokeBlock[A](r: Request[A], block: DataRequest[A] => Future[Result]): Future[Result] = {
    convert(r).flatMap((kr)=>updateSession(kr,block(kr)))
  }

  private def isValue(key: String): Boolean = {
    !List(SCHEME_TYPE_KEY,
          FIRST_DC_YEAR_KEY,
          DB_FLAG_PREFIX,
          DC_FLAG_PREFIX,
          TRIGGER_DATE_KEY,
          CURRENT_INPUT_YEAR_KEY,
          SELECTED_INPUT_YEARS_KEY,
          TE_YES_NO_KEY,
          TI_YES_NO_KEY_PREFIX,
          IS_EDIT_KEY).contains(key)
  }

  private def convert[T](r: Request[T]): Future[DataRequest[T]] = {
    implicit val hc = HeaderCarrier()
    implicit val request: Request[T] = r
    keystore.readData().map {
      (d)=>
      val data = d.map {
        (entry) =>
        val (key, value) = entry
        val Pattern = "([0-9]+)".r
        val newValue: String = value match {
           case Pattern(v) if isValue(key) => f"${(v.toInt / 100.00)}%2.0f".trim
           case v => v
        }
        (key,newValue)
      }
      new DataRequest[T](data, request)
    }
  }

  protected def updateSession[A](dataRequest: DataRequest[A], response: Future[Result]): Future[Result] = {
    response.map {
      (result) =>
      implicit val request: RequestHeader = dataRequest
      val updatedResponse = dataRequest.data.foldLeft(result.removingFromSession(dataRequest.data.keySet.toSeq: _*)) {
        (r, entry) =>
        r.addingToSession(entry._1 -> entry._2)
      }
      updatedResponse
    }
  }
}