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

package test

import java.util.UUID

import org.scalatest.BeforeAndAfterAll
import play.api.test.Helpers._
import play.api.test.{FakeRequest}
import play.api.Play
import play.api.test._
import play.api.mvc._
import play.api.mvc.Results._
import service.KeystoreService

import uk.gov.hmrc.play.http.{HeaderCarrier, HttpPost, HttpResponse}
import uk.gov.hmrc.play.http.SessionKeys
import uk.gov.hmrc.play.test.UnitSpec
import scala.concurrent.Future

class SimpleBaseSpec extends UnitSpec {
  val SESSION_ID = s"session-${UUID.randomUUID}"

  implicit val request = FakeRequest()

  trait MockKeystoreFixture {
    object MockKeystore extends KeystoreService {
      var map = Map(SessionKeys.sessionId -> SESSION_ID)

      override def storeValue[T](data: T, key: String)
                                (implicit hc: HeaderCarrier,
                                          format: play.api.libs.json.Format[T], 
                                          request: Request[Any]): Future[Option[T]] = {
        map = map + (key -> data.toString)
        Future.successful(Some(data))
      }

      override def store(data: String, key: String)
                        (implicit hc: HeaderCarrier, request: Request[Any]) : Future[Option[String]] = {
        map = map + (key -> data.toString)
        Future.successful(Some(data))
      }

      override def read[T](key: String)
                          (implicit hc: HeaderCarrier,
                           format: play.api.libs.json.Format[T],
                           request: Request[Any])
      : Future[Option[T]] = {
        Future.successful((map get key).map(_.asInstanceOf[T]))
      }
    }
  }
}

class BaseSpec extends SimpleBaseSpec with BeforeAndAfterAll {
  val app = FakeApplication()

  override def beforeAll() {
    Play.start(app)
    super.beforeAll() // To be stackable, must call super.beforeEach
  }

  override def afterAll() {
    try {
      super.afterAll()
    } finally Play.stop()
  }
}
