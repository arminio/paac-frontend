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
import play.api.mvc.{AnyContent, Request}
import uk.gov.hmrc.http.cache.client.SessionCache
import uk.gov.hmrc.play.http.{HeaderCarrier, SessionKeys}
import play.api.libs.concurrent.Execution.Implicits._
import scala.concurrent.{Future}

trait KeystoreService {
  val SOURCE = "paac-frontend"
  val sessionCache: SessionCache = PaacSessionCache

  /**
   * Store data to Keystore using a key
   */
  def store[T](data: T, key: String)(implicit hc: HeaderCarrier, format: play.api.libs.json.Format[T], request: Request[Any]): Future[Option[T]] = {
    request.session.get(SessionKeys.sessionId) match {
      case Some(id) => sessionCache.cache[T](SOURCE, id, key, data) map { case x => x.getEntry[T](key) }
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
}

object KeystoreService extends KeystoreService
