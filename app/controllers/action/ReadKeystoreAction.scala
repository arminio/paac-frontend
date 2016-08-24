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
import play.api.Logger
import metrics._
import uk.gov.hmrc.play.http._

trait ReadKeystore extends ActionBuilder[DataRequest] with Metrics {
  def keystore: KeystoreService

  protected def convert[T](r: Request[T]): Future[DataRequest[T]] = {
    implicit val hc = HeaderCarrier()
    implicit val request: Request[T] = r
    keystore.readData().map {
      (data)=>
      Logger.debug(s"""Reading from keystore: ${data.mkString(", ")}""")
      new DataRequest[T](data, request)
    }.andThen {
      case Failure(t) => {
        log(t)
        throw t
      }
      case Success(results) => results
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

  protected def log(t: Throwable): Unit = {
    Logger.error(s"Keystore error: ${t.getMessage()}")
    t match {
      case _: BadRequestException => keystoreStatusCode(400)
      case _: NotFoundException => keystoreStatusCode(404)
      case u: Upstream4xxResponse => keystoreStatusCode(u.reportAs)
      case u: Upstream5xxResponse => keystoreStatusCode(u.reportAs)
      case _: GatewayTimeoutException => keystoreStatusCode(504)
      case _: BadGatewayException => keystoreStatusCode(502)
      case _ => {
        val msg = t.getMessage
        if (msg.contains(" failed with status ")) {
          keystoreStatusCode(msg.split("\\.")(0).split(" ").reverse(0).toInt)
        }
      }
    }
  }
}

case class ReadKeystoreAction(keystore: KeystoreService) extends ReadKeystore with metrics.GraphiteMetrics {
  def invokeBlock[A](r: Request[A], block: DataRequest[A] => Future[Result]): Future[Result] = {
    convert(r).flatMap((kr)=>updateSession(kr,block(kr)))
  }
}