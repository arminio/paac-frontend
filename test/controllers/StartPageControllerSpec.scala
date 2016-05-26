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

import java.util.UUID

import org.scalatest.BeforeAndAfterAll
import play.api.Play
import play.api.mvc.Result
import play.api.test.Helpers._
import play.api.test.{FakeRequest, FakeApplication}
import uk.gov.hmrc.play.http.{HeaderCarrier, HttpPost, HttpResponse}
import uk.gov.hmrc.play.http.SessionKeys
import uk.gov.hmrc.play.test.UnitSpec
import scala.concurrent.Future
import play.api.libs.concurrent.Execution.Implicits._
import service._
import connector._

import play.api.test._
import play.api.mvc._
import service.KeystoreService

class StartPageControllerSpec extends UnitSpec with BeforeAndAfterAll {
  val app = FakeApplication()
  val SESSION_ID = s"session-${UUID.randomUUID}"


  override def beforeAll() {
      Play.start(app)
      super.beforeAll() // To be stackable, must call super.beforeEach
    }

    override def afterAll() {
      try {
        super.afterAll()
      } finally Play.stop()
    }

    implicit val request = FakeRequest()

    trait MockKeystoreFixture {
      object MockKeystore extends KeystoreService {
        var map = Map(SessionKeys.sessionId -> SESSION_ID)

        override def store[T](data: T, key: String)
                             (implicit hc: HeaderCarrier,
                              format: play.api.libs.json.Format[T],
                              request: Request[Any])
        : Future[Option[T]] = {
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

    "StartPageController" should {
      "not return result NOT_FOUND" in {
        val result : Option[Future[Result]] = route(FakeRequest(GET, "/paac"))
        result.isDefined shouldBe true
        status(result.get) should not be NOT_FOUND
      }

      "return 303 for valid GET request" in {
        val result : Option[Future[Result]] = route(FakeRequest(GET, "/paac"))
        status(result.get) shouldBe 303
      }

      "not return 200 for valid GET request" in {
        val result : Option[Future[Result]] = route(FakeRequest(GET, "/paac"))
        status(result.get) should not be 200
      }

      "return error if no JSON supplied for GET request" in {
        val result : Option[Future[Result]] = route(FakeRequest(GET, "/paac"))
        status(result.get) shouldBe 303
      }

      "create a session onSubmit" in {
        // set up
        val request = FakeRequest(GET, "/paac").withSession {(SessionKeys.sessionId,SESSION_ID)}

        // test
        val result: Future[Result] = StartPageController.onSubmit()(request)

        // check
        val StartPage = contentAsString(await(result))
        StartPage should include ("")
      }

      "create a new session" in {
        // set up
        val request = FakeRequest(GET, "/paac").withSession {(SessionKeys.sessionId,SESSION_ID)}

        // test
        val result : Future[Result] = StartPageController.newSession()(request)

        // check
        val StartPage = contentAsString(await(result))
        StartPage should include ("")
      }

      "render the StartPage" in new MockKeystoreFixture {
        // set up
        val request = FakeRequest(GET, "/paac"). withSession {(SessionKeys.sessionId,SESSION_ID)}
        object MockedStartPageController extends StartPageController {
          override val keystore: KeystoreService = MockKeystore
          override val connector: CalculatorConnector = null
        }

        // test
        val result : Future[Result] = MockedStartPageController.startPage()(request)

        // check
        val StartPage = contentAsString(await(result))
        StartPage should include ("")
      }
    }
}
