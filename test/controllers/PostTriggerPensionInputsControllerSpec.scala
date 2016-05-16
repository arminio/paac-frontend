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

import org.scalatest.BeforeAndAfterAll
import uk.gov.hmrc.play.test.UnitSpec
import play.api.test.{FakeApplication, FakeRequest}
import service.KeystoreService
import uk.gov.hmrc.play.http.{HeaderCarrier, SessionKeys}
import scala.concurrent.Future
import play.api.Play
import play.api.mvc.{Request, Result}
import play.api.test.Helpers._

import java.util.UUID

class PostTriggerPensionInputsControllerSpec extends UnitSpec with BeforeAndAfterAll {
  val app = FakeApplication()
  val SESSION_ID = s"session-${UUID.randomUUID}"
  implicit val request = FakeRequest()
  val endPointURL = "/paac/moneyPurchasePostTriggerValue"

  override def beforeAll() {
    Play.start(app)
    super.beforeAll() // To be stackable, must call super.beforeEach
  }

  override def afterAll() {
    try {
      super.afterAll()
    } finally Play.stop()
  }

  trait ControllerWithMockKeystore extends MockKeystoreFixture {
    object ControllerWithMockKeystore extends PostTriggerPensionInputsController {
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

  "onPageLoad" should {
    "redirect if trigger date not present in keystore" in {
      val result: Option[Future[Result]] = route(FakeRequest(GET, endPointURL))
      result.isDefined shouldBe true
      status(result.get) shouldBe 303
    }

    "redirect if trigger date is blank in keystore" in new ControllerWithMockKeystore {
      MockKeystore.map = MockKeystore.map + (KeystoreService.TRIGGER_DATE_KEY -> "")
      val request = FakeRequest(GET,"").withSession{(SessionKeys.sessionId,SESSION_ID)}

      val result : Future[Result] = ControllerWithMockKeystore.onPageLoad()(request)

      status(result) shouldBe 303
    }

    "display p1 input amount page with previous value if trigger date was period 1" in new ControllerWithMockKeystore {
      // setup
      val request = FakeRequest(GET,"").withSession{(SessionKeys.sessionId,SESSION_ID)}
      MockKeystore.map = MockKeystore.map + (KeystoreService.TRIGGER_DATE_KEY -> "2015-6-15")
      MockKeystore.map = MockKeystore.map + (KeystoreService.P1_TRIGGER_DC_KEY -> "1234")
      MockKeystore.map = MockKeystore.map + (KeystoreService.P2_TRIGGER_DC_KEY -> "5678")

      // test
      val result : Future[Result] = ControllerWithMockKeystore.onPageLoad()(request)

      // check
      status(result) shouldBe 200
      val htmlPage = contentAsString(await(result))
      htmlPage should include ("""<input type="number" name="year2015.postTriggerDcAmount2015P1" id="year2015.postTriggerDcAmount2015P1" min="0" step="1" value='12.34' max="9999999.99" size="10" """)
    }

    "display p2 input amount page with previous value if trigger date was period 2" in new ControllerWithMockKeystore {
      // setup
      val request = FakeRequest(GET,"").withSession{(SessionKeys.sessionId,SESSION_ID)}
      MockKeystore.map = MockKeystore.map + (KeystoreService.TRIGGER_DATE_KEY -> "2015-11-15")
      MockKeystore.map = MockKeystore.map + (KeystoreService.P1_TRIGGER_DC_KEY -> "1234")
      MockKeystore.map = MockKeystore.map + (KeystoreService.P2_TRIGGER_DC_KEY -> "5678")

      // test
      val result : Future[Result] = ControllerWithMockKeystore.onPageLoad()(request)

      // check
      status(result) shouldBe 200
      val htmlPage = contentAsString(await(result))
      htmlPage should include ("""<input type="number" name="year2015.postTriggerDcAmount2015P2" id="year2015.postTriggerDcAmount2015P2" min="0" step="1" value='56.78' max="9999999.99" size="10" """)
    }
  }

  "onSubmit" should {
    "display errors if amount is negative" in new ControllerWithMockKeystore {
      // set up
      MockKeystore.map = MockKeystore.map + (KeystoreService.TRIGGER_DATE_KEY -> "2015-11-15")
      MockKeystore.map = MockKeystore.map + (KeystoreService.P1_TRIGGER_DC_KEY -> "1234")
      MockKeystore.map = MockKeystore.map + (KeystoreService.P2_TRIGGER_DC_KEY -> "5678")
      implicit val hc = HeaderCarrier()
      implicit val request = FakeRequest(POST, endPointURL).withSession((SessionKeys.sessionId,SESSION_ID)).withFormUrlEncodedBody(("year2015.postTriggerDcAmount2015P2" -> "-1"),
                               ("triggerDate", "2015-11-15"))

      // test
      val result: Future[Result] = ControllerWithMockKeystore.onSubmit()(request)

      // check
      status(result) shouldBe 200
      val htmlPage = contentAsString(await(result))
      htmlPage should include ("""<a href="input_year2015.postTriggerDcAmount2015P1" style="color:#b10e1e;font-weight: bold;">2015 amount was empty or negative. Please provide an amount between £0.00 and £99999999.99.</a>""")
    }

    "display errors if amount is blank" in new ControllerWithMockKeystore {
      // set up
      MockKeystore.map = MockKeystore.map + (KeystoreService.TRIGGER_DATE_KEY -> "2015-11-15")
      MockKeystore.map = MockKeystore.map + (KeystoreService.P1_TRIGGER_DC_KEY -> "1234")
      MockKeystore.map = MockKeystore.map + (KeystoreService.P2_TRIGGER_DC_KEY -> "5678")
      implicit val hc = HeaderCarrier()
      implicit val request = FakeRequest(POST, endPointURL).withSession((SessionKeys.sessionId,SESSION_ID)).withFormUrlEncodedBody(("year2015.postTriggerDcAmount2015P2" -> ""),
                               ("triggerDate", "2015-11-15"))

      // test
      val result: Future[Result] = ControllerWithMockKeystore.onSubmit()(request)

      // check
      status(result) shouldBe 200
      val htmlPage = contentAsString(await(result))
      htmlPage should include ("""<a href="input_year2015.postTriggerDcAmount2015P1" style="color:#b10e1e;font-weight: bold;">2015 amount was empty or negative. Please provide an amount between £0.00 and £99999999.99.</a>""")
    }

    "saves p2 amount in keystore if valid form" in new ControllerWithMockKeystore {
      // set up
      MockKeystore.map = MockKeystore.map + (KeystoreService.TRIGGER_DATE_KEY -> "2015-11-15")
      implicit val hc = HeaderCarrier()
      implicit val request = FakeRequest(POST, endPointURL).withSession((SessionKeys.sessionId,SESSION_ID)).withFormUrlEncodedBody(("year2015.postTriggerDcAmount2015P2" -> "40000"),
                               ("triggerDate", "2015-11-15"))

      // test
      val result: Future[Result] = ControllerWithMockKeystore.onSubmit()(request)

      // check
      status(result) shouldBe 303
      MockKeystore.map should contain key (KeystoreService.P2_TRIGGER_DC_KEY)
      MockKeystore.map should contain value ("4000000")
    }

    "saves p1 amount in keystore if valid form" in new ControllerWithMockKeystore {
      // set up
      MockKeystore.map = MockKeystore.map + (KeystoreService.TRIGGER_DATE_KEY -> "2015-4-15")
      implicit val hc = HeaderCarrier()
      implicit val request = FakeRequest(POST, endPointURL).withSession((SessionKeys.sessionId,SESSION_ID)).withFormUrlEncodedBody(("year2015.postTriggerDcAmount2015P1" -> "40000"),
                               ("triggerDate", "2015-4-15"))

      // test
      val result: Future[Result] = ControllerWithMockKeystore.onSubmit()(request)

      // check
      status(result) shouldBe 303
      MockKeystore.map should contain key (KeystoreService.P1_TRIGGER_DC_KEY)
      MockKeystore.map should contain value ("4000000")
    }
  }
}
