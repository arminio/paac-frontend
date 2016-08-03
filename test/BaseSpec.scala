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

      override def saveData(data: Map[String,String])
                           (implicit hc: HeaderCarrier,
                                     request: Request[Any]): Future[Option[Map[String,String]]] = {
        map = map ++ data
        Future.successful(Some(map))
      }

      override def readData()
                           (implicit hc: HeaderCarrier,
                            request: Request[Any]): Future[Map[String,String]] = {
        Future.successful(map)
      }

      override def clear()(implicit hc: HeaderCarrier, request: Request[Any]): Future[Option[Boolean]] = {
        map = Map()
        Future.successful(Some(true))
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
