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

trait KeystoreService {
  val SOURCE = "paac-frontend"
  val sessionCache: SessionCache = PaacSessionCache

  /**
   * Store data to Keystore using a key
   */
  def storeValue[T](data: T, key: String)(implicit hc: HeaderCarrier, format: play.api.libs.json.Format[T], request: Request[Any]): Future[Option[T]] = {
    request.session.get(SessionKeys.sessionId) match {
      case Some(id) => sessionCache.cache[T](SOURCE, id, key, data) map { case x => x.getEntry[T](key) }
      case None => Future.successful(None)
    }
  }

  /**
   * Store data to Keystore using a key
   */
  def store(data: String, key: String)(implicit hc: HeaderCarrier, request: Request[Any]): Future[Option[String]] = {
    request.session.get(SessionKeys.sessionId) match {
      case Some(id) => sessionCache.cache[String](SOURCE, id, key, data) map { case x => x.getEntry[String](key) }
      case None => Future.successful(None)
    }
  }

  /**
   * get particular key out of keystore
   */
  def read[T](key: String)(implicit hc: HeaderCarrier, format: play.api.libs.json.Format[T], request: Request[Any]): Future[Option[T]] = {
    request.session.get(SessionKeys.sessionId) match {
      case Some(id) => sessionCache.fetchAndGetEntry[T](SOURCE, id, key)
      case None => Future.successful(None)
    }
  }

  def read[T](keys: List[String])(implicit marshall: (String, Option[T]) => (String, T), hc: HeaderCarrier, format: play.api.libs.json.Format[T], request: Request[Any]): Future[Map[String, T]] = {
    val reads: List[Future[(String, T)]] = keys.map((key)=>read[T](key).map(marshall(key,_)))
    Future.sequence(reads).map((fields) =>Map[String, T](fields: _*))
  }

  def save[T](values: List[(Option[T],String)], defaultT: T)(implicit hc: HeaderCarrier, format: play.api.libs.json.Format[T], request: Request[Any]): Future[List[Option[T]]] = {
    Future.sequence(values.map{
      (pair)=>
      val v = pair._1.getOrElse(defaultT)
      storeValue[T](v, pair._2)
    })
  }

  def save[T: ClassTag ,U](values: List[Option[(U,String)]], defaultT: T)(implicit hc: HeaderCarrier, format: play.api.libs.json.Format[T], request: Request[Any]): Future[List[Option[T]]] = {
    Future.sequence(values.filter(_ != None).map{
      (maybePair)=>
      val pair = maybePair.get
      storeValue[T](convert[T](pair._1.asInstanceOf[AnyRef], defaultT), pair._2)
    })
  }

  def save[T](values: List[(T,String)])(implicit hc: HeaderCarrier, format: play.api.libs.json.Format[T], request: Request[Any]): Future[List[Option[T]]] = {
    Future.sequence(values.map{
      (pair)=>
      storeValue[T](pair._1, pair._2)
    })
  }

  def convert[T : ClassTag](value: AnyRef): Option[T] = {
    val ct = implicitly[ClassTag[T]]
    val typeStr = ct.toString()
    value match {
      case ct(x) => Some(x)
      case x if typeStr == "java.lang.String" => Some(x.toString().asInstanceOf[T])
      case _ => None
    }
  }

  def convert[T : ClassTag](value: AnyRef, defaultT: T): T = {
    val ct = implicitly[ClassTag[T]]
    val typeStr = ct.toString()
    value match {
      case ct(x) => x
      case x if typeStr == "java.lang.String" => x.toString().asInstanceOf[T]
      case _ => defaultT
    }
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
  val SCHEME_TYPE_KEY = "schemeType"
  val CURRENT_INPUT_YEAR_KEY = "Current"
  val SELECTED_INPUT_YEARS_KEY = "SelectedYears"
  val DB_PREFIX = "definedBenefit_"
  val DC_PREFIX = "definedContribution_"
  val DB_FLAG = "definedBenefit"
  val DC_FLAG = "definedContribution"
  val TH_PREFIX = "thresholdIncome_"
  val AI_PREFIX = "adjustedIncome_"
  val TA_PREFIX = "taperedAllowance_"
  val TE_YES_NO_KEY = "yesnoForMPAATriggerEvent"
  val IS_EDIT_KEY = "isEdit"

  def toStringPair(key: String, value: Option[String]): (String, String) = value match {
    case None => (key, "")
    case Some(v) => (key, v)
  }
}
