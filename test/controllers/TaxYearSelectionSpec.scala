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
import play.api.mvc.{Result, Request}
import play.api.test.Helpers._
import play.api.test.{FakeRequest, FakeApplication}
import service.KeystoreService
import uk.gov.hmrc.play.http.{HeaderCarrier, SessionKeys}
import uk.gov.hmrc.play.test.UnitSpec
import scala.concurrent.Future

class TaxYearSelectionSpec extends UnitSpec with BeforeAndAfterAll {
  val app = FakeApplication()
  val SESSION_ID = s"session-${UUID.randomUUID}"
  val endPointURL = "/paac/taxyearselection"

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

  trait ControllerWithMockKeystore extends MockKeystoreFixture {
    object MockTaxYearSelectionWithMockKeystore extends TaxYearSelectionController {
      val keystorekey = "TaxYearSelection"
      override val keystore: KeystoreService = MockKeystore
    }
  }


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

  "TaxYearSelection" when {
    "GET with routes".should {
      "not return result NOT_FOUND" in {
        val result: Option[Future[Result]] = route(FakeRequest(GET, endPointURL))
        result.isDefined shouldBe true
        status(result.get) should not be NOT_FOUND
      }
      "return 303 for valid GET request" in {
        val result: Option[Future[Result]] = route(FakeRequest(GET, endPointURL))
        status(result.get) shouldBe 303
      }
      "not return 200 for valid GET request" in {
        val result: Option[Future[Result]] = route(FakeRequest(GET, endPointURL))
        status(result.get) should not be 200
      }
    }

    "onPageLoad with GET request" should {
      "have keystore with no values and display Tax Year Selection options" in new ControllerWithMockKeystore {
        // setup
        val request = FakeRequest(GET,"").withSession{(SessionKeys.sessionId,SESSION_ID)}

        // test
        val result : Future[Result] = MockTaxYearSelectionWithMockKeystore.onPageLoad()(request)

        // check
        status(result) shouldBe 200
        val htmlPage = contentAsString(await(result))
      }

      "have keystore with TaxYearSelection value when page is revisited" in new ControllerWithMockKeystore {
        // setup
        val request = FakeRequest(GET,"").withSession{(SessionKeys.sessionId,SESSION_ID)}
        MockKeystore.map = MockKeystore.map + ("TaxYearSelection" -> "2015")

        // test
        val result : Future[Result] = MockTaxYearSelectionWithMockKeystore.onPageLoad()(request)

        // check
        status(result) shouldBe 200
        MockKeystore.map should contain key ("TaxYearSelection")
        MockKeystore.map should contain value ("2015")
      }
    }
  }

  "onYearSelected with POST request" should {
    "not return result NOT_FOUND" in {
      val result: Option[Future[Result]] = route(FakeRequest(POST, endPointURL))
      result.isDefined shouldBe true
      status(result.get) should not be NOT_FOUND
    }

    "return 303 for valid GET request" in {
      val result: Option[Future[Result]] = route(FakeRequest(POST, endPointURL))
      status(result.get) shouldBe 303
    }

//    "have valid TaxYearSelection saved to keystore" in new ControllerWithMockKeystore{
//      // set up
//      implicit val hc = HeaderCarrier()
//      implicit val request = FakeRequest(POST, endPointURL).withSession((SessionKeys.sessionId,SESSION_ID)).withFormUrlEncodedBody(("TaxYear2015" -> "2015"))
//
//      // test
//      val result: Future[Result] = MockTaxYearSelectionWithMockKeystore.onYearSelected()(request)
//
//      // check
//      status(result) shouldBe 200
//      MockKeystore.map should contain key ("TaxYearSelection")
//      MockKeystore.map should contain value ("2015")
//    }


  }
}