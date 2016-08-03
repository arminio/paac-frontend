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

package service

import config.PaacSessionCache
import play.api.mvc.Request
import uk.gov.hmrc.http.cache.client.SessionCache
import uk.gov.hmrc.play.http.{HeaderCarrier, SessionKeys}
import play.api.libs.concurrent.Execution.Implicits._
import scala.concurrent.Future
import reflect.ClassTag
import uk.gov.hmrc.play.http._
import scala.util.{Failure, Success}
import uk.gov.hmrc.http.cache.client._
import play.Logger


trait KeystoreService {
  val SOURCE = "paac-frontend"
  val sessionCache: SessionCache = PaacSessionCache

  def readData()(implicit hc: HeaderCarrier, request: Request[Any]): Future[Map[String,String]] = {
    import play.api.libs.json._
    request.session.get(SessionKeys.sessionId) match {
      case Some(id) => {
        sessionCache.fetchAndGetEntry[JsValue](SOURCE, id, "calculatorForm").map {
          (maybeValue)=>
          maybeValue match {
            case Some(json) => json.as[Map[String, JsValue]].mapValues(_.as[String])
            case None => {
              Logger.error(s"Keystore fetchAndGetEntry did not find 'calculatorForm' resource for session ${id}. Returning empty data map.")
              Map[String,String]()
            }
          }
        }
      }
      case None => Future.successful(Map[String,String]())
    }
  }

  def saveData(data: Map[String,String])(implicit hc: HeaderCarrier, request: Request[Any]): Future[Option[Map[String,String]]] = {
    import play.api.libs.json._
    request.session.get(SessionKeys.sessionId) match {
      case Some(id) => {
        val json = Json.toJson(data.filterNot((entry)=>entry._1 == "csrfToken" || entry._1 == "sessionId"))
        sessionCache.cache[JsValue](SOURCE, id, "calculatorForm", json) map {
          (cache)=>
          cache.data.get("calculatorForm").map(_.as[Map[String, JsValue]].mapValues(_.as[String]))
        }
      }
      case None => Future.successful(None)
    }
  }

  def clear()(implicit hc: HeaderCarrier, request: Request[Any]): Future[Option[Boolean]] = {
    request.session.get(SessionKeys.sessionId) match {
      case Some(id) => {
        val uri = buildUri(sessionCache.defaultSource, id)
        Logger.info(s"Deleting keystore session ${uri}")
        sessionCache.delete(uri).map{
          (r)=>
          r.status match {
            case responseStatus if responseStatus >= 200 && responseStatus < 300 => Some(true)
            case responseStatus => {
              Logger.warn(s"""Deleting keystore session appears to have failed with status code: ${responseStatus}: ${r.body}.""")
              Some(false)
            }
          }

        }
      }
      case None => Future.successful(None)
    }
  }

  protected def buildUri(source: String, id: String): String = s"${sessionCache.baseUri}/${sessionCache.domain}/${source}/${id}"

  implicit class RichMap(map: Map[String,String]) {
    def bool(key: String): Boolean = if (!map.isDefinedAt(key) || map(key).isEmpty) false else map(key).toBoolean
    def int(key: String): Int = if (!map.isDefinedAt(key) || map(key).isEmpty) 0 else map(key).toInt
    def yesNo(key: String): Boolean = if (!map.isDefinedAt(key) || map(key).isEmpty) false else map(key) == "Yes"
  }
}

object KeystoreService extends KeystoreService {
  val TRIGGER_DATE_KEY = "dateOfMPAATriggerEvent"
  val P1_DB_KEY = "definedBenefit_2015_p1"
  val P1_DC_KEY = "definedContribution_2015_p1"
  val P2_DB_KEY = "definedBenefit_2015_p2"
  val P2_DC_KEY= "definedContribution_2015_p2"
  val P1_TRIGGER_DC_KEY = "triggerDefinedContribution_2015_p1"
  val P2_TRIGGER_DC_KEY= "triggerDefinedContribution_2015_p2"
  val TRIGGER_DC_KEY= "triggerDefinedContribution"
  val SCHEME_TYPE_KEY = "schemeType"
  val CURRENT_INPUT_YEAR_KEY = "Current"
  val SELECTED_INPUT_YEARS_KEY = "SelectedYears"
  val FIRST_DC_YEAR_KEY = "FirstDCYear"
  val DB_PREFIX = "definedBenefit_"
  val DC_PREFIX = "definedContribution_"
  val DB_FLAG_PREFIX = "isDefinedBenefit_"
  val DC_FLAG_PREFIX = "isDefinedContribution_"
  val TH_PREFIX = "thresholdIncome_"
  val AI_PREFIX = "adjustedIncome_"
  val TA_PREFIX = "taperedAllowance_"
  val TE_YES_NO_KEY = "yesnoForMPAATriggerEvent"
  val TI_YES_NO_KEY_PREFIX = "yesnoForThresholdIncome_"
  val IS_EDIT_KEY = "isEdit"
}
