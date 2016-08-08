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
import play.api.Logger
import service._
import uk.gov.hmrc.play.http.{HeaderCarrier, SessionKeys}
import scala.util.{Try, Success, Failure, Either}
import play.api.libs.json._
import play.api.libs.concurrent.Execution.Implicits._

trait WriteKeystore extends ActionBuilder[DataRequest] {
  def keystore: KeystoreService

  protected def convert[T](request: Request[T]): DataRequest[T] = {
      new DataRequest[T](request.session.data, request)
  }

  protected def updateKeystore[A](dataRequest: DataRequest[A], response: Future[Result]): Future[Result] = {
    response.flatMap {
      (r) =>
      implicit val hc = HeaderCarrier()
      implicit val request: Request[A] = dataRequest
      Logger.debug(s"""Saving to keystore ${r.session.data.mkString(" ")}""")
      keystore.saveData(r.session.data).map {
        (_)=>
        r
      }
    }
  }
}

case class WriteKeystoreAction(keystore: KeystoreService) extends WriteKeystore {
  def invokeBlock[A](r: Request[A], block: DataRequest[A] => Future[Result]): Future[Result] = {
    val request = convert(r)
    updateKeystore(request,block(request))
  }
}
