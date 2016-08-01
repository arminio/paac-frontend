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

import play.api.mvc.Result
import play.api.test.FakeRequest
import play.api.test.Helpers._
import service.KeystoreService
import uk.gov.hmrc.play.http.{HeaderCarrier, SessionKeys}

import scala.concurrent.Future

class TaxYearSelectionSpec extends test.BaseSpec {
  val endPointURL = "/paac/taxyearselection"

  trait ControllerWithMockKeystore extends MockKeystoreFixture {
    object ControllerWithMockKeystore extends TaxYearSelectionController {
      val keystorekey = "TaxYearSelection"
      override val keystore: KeystoreService = MockKeystore
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
        val result : Future[Result] = ControllerWithMockKeystore.onPageLoad()(request)

        // check
        status(result) shouldBe 200
        val htmlPage = contentAsString(await(result))
      }

      "have keystore with TaxYearSelection value when page is revisited" in new ControllerWithMockKeystore {
        // setup
        MockKeystore.map = MockKeystore.map + ("TaxYearSelection" -> "2015")
        implicit val hc = HeaderCarrier()
        val request = FakeRequest(GET,"").withSession{(SessionKeys.sessionId,SESSION_ID)}
        // test
        val result : Future[Result] = ControllerWithMockKeystore.onPageLoad()(request)

        // check
        status(result) shouldBe 200
        MockKeystore.map should contain key ("TaxYearSelection")
        MockKeystore.map should contain value ("2015")
      }

      "if TaxYearSelection value in keystore displays checked checkboxes" in new ControllerWithMockKeystore {
        // set up
        MockKeystore.map = MockKeystore.map + (KeystoreService.SELECTED_INPUT_YEARS_KEY -> "2015,2014,2013")
        implicit val hc = HeaderCarrier()
        val request = FakeRequest(GET,"").withSession{(SessionKeys.sessionId,SESSION_ID)}

        // test
        val result : Future[Result] = ControllerWithMockKeystore.onPageLoad()(request)

        // check
        status(result) shouldBe 200
        val htmlPage = contentAsString(await(result))
        htmlPage should include ("""<input id="TaxYear2015" type="checkbox" name="TaxYear2015" checked>""")
        htmlPage should include ("""<input id="TaxYear2014" type="checkbox" name="TaxYear2014" checked>""")
        htmlPage should include ("""<input id="TaxYear2013" type="checkbox" name="TaxYear2013" checked>""")
      }
    }
  }

  "onYearSelected with POST request" should {
    "display error message if no years selected" in new ControllerWithMockKeystore {
      // set up
      implicit val hc = HeaderCarrier()
      implicit val request = FakeRequest(POST, endPointURL).withSession((SessionKeys.sessionId,SESSION_ID)).withFormUrlEncodedBody(("csrfToken" -> "blah"),("previous"->""))

      // test
      val result: Future[Result] = ControllerWithMockKeystore.onYearSelected()(request)

      // check
      status(result) shouldBe 200
      val htmlPage = contentAsString(await(result))
      htmlPage should include ("""Please select years you were a member of a pension scheme.""")
    }

    "not return result NOT_FOUND" in {
      val result: Option[Future[Result]] = route(FakeRequest(POST, endPointURL))
      result.isDefined shouldBe true
      status(result.get) should not be NOT_FOUND
    }

    "return 303 for valid GET request" in {
      val result: Option[Future[Result]] = route(FakeRequest(POST, endPointURL))
      status(result.get) shouldBe 303
    }

    "have valid TaxYearSelection saved to keystore" in new ControllerWithMockKeystore {
      // set up
      implicit val hc = HeaderCarrier()
      implicit val request = FakeRequest(POST, endPointURL).withSession((SessionKeys.sessionId,SESSION_ID)).withFormUrlEncodedBody(("csrfToken" -> "blah"),("TaxYear2015" -> "2015"),("previous"->""))
      MockKeystore.map = MockKeystore.map + ("TaxYearSelection" -> "2015")
      MockKeystore.map = MockKeystore.map + (KeystoreService.IS_EDIT_KEY -> "false")
      MockKeystore.map = MockKeystore.map + (KeystoreService.TE_YES_NO_KEY -> "false")

      // test
      val result: Future[Result] = ControllerWithMockKeystore.onYearSelected()(request)

      // check
      status(result) shouldBe 303
      MockKeystore.map(KeystoreService.CURRENT_INPUT_YEAR_KEY) shouldBe ("2015")
      MockKeystore.map(KeystoreService.SELECTED_INPUT_YEARS_KEY) shouldBe ("2015")
      redirectLocation(result) shouldBe Some("/paac/scheme/2015")
    }

    "when unselecting a previously selected year reset year's data value" in new ControllerWithMockKeystore {
      // set up
      implicit val hc = HeaderCarrier()
      implicit val request = FakeRequest(POST, endPointURL).withSession((SessionKeys.sessionId,SESSION_ID)).withFormUrlEncodedBody(("csrfToken" -> "blah"),("TaxYear2014" -> "2014"),("previous"->"2014,2013"))
      MockKeystore.map = MockKeystore.map + ("TaxYearSelection" -> "2014,2013")
      MockKeystore.map = MockKeystore.map + (KeystoreService.IS_EDIT_KEY -> "false")
      MockKeystore.map = MockKeystore.map + (KeystoreService.TE_YES_NO_KEY -> "false")
      MockKeystore.map = MockKeystore.map + (KeystoreService.DB_PREFIX + "2013" -> "100")
      MockKeystore.map = MockKeystore.map + (KeystoreService.DC_PREFIX + "2013" -> "200")

      // test
      val result: Future[Result] = ControllerWithMockKeystore.onYearSelected()(request)

      // check
      status(result) shouldBe 303
      MockKeystore.map(KeystoreService.DB_PREFIX + "2013") shouldBe ("")
      MockKeystore.map(KeystoreService.DC_PREFIX + "2013") shouldBe ("")
      MockKeystore.map(KeystoreService.CURRENT_INPUT_YEAR_KEY) shouldBe ("2014")
      MockKeystore.map(KeystoreService.SELECTED_INPUT_YEARS_KEY) shouldBe ("2014")
    }

    "when unselecting 2015 when previously selected reset 2015's data values" in new ControllerWithMockKeystore {
      // set up
      implicit val hc = HeaderCarrier()
      implicit val request = FakeRequest(POST, endPointURL).withSession((SessionKeys.sessionId,SESSION_ID)).withFormUrlEncodedBody(("csrfToken" -> "blah"),("TaxYear2014" -> "2014"),("previous"->"2015,2013"))
      MockKeystore.map = MockKeystore.map + ("TaxYearSelection" -> "2014")
      MockKeystore.map = MockKeystore.map + (KeystoreService.IS_EDIT_KEY -> "false")
      MockKeystore.map = MockKeystore.map + (KeystoreService.P1_DB_KEY -> "100")
      MockKeystore.map = MockKeystore.map + (KeystoreService.P1_DC_KEY -> "200")
      MockKeystore.map = MockKeystore.map + (KeystoreService.P2_DB_KEY -> "300")
      MockKeystore.map = MockKeystore.map + (KeystoreService.P2_DC_KEY -> "400")
      MockKeystore.map = MockKeystore.map + (KeystoreService.P1_TRIGGER_DC_KEY -> "500")
      MockKeystore.map = MockKeystore.map + (KeystoreService.P2_TRIGGER_DC_KEY -> "600")
      MockKeystore.map = MockKeystore.map + (KeystoreService.TRIGGER_DATE_KEY -> "2015-11-1")
      MockKeystore.map = MockKeystore.map + (KeystoreService.TE_YES_NO_KEY -> "Yes")

      // test
      val result: Future[Result] = ControllerWithMockKeystore.onYearSelected()(request)

      // check
      status(result) shouldBe 303
      MockKeystore.map(KeystoreService.CURRENT_INPUT_YEAR_KEY) shouldBe ("2014")
      MockKeystore.map(KeystoreService.SELECTED_INPUT_YEARS_KEY) shouldBe ("2014")
      MockKeystore.map(KeystoreService.P1_DB_KEY) shouldBe ("")
      MockKeystore.map(KeystoreService.P1_DC_KEY) shouldBe ("")
      MockKeystore.map(KeystoreService.P2_DB_KEY) shouldBe ("")
      MockKeystore.map(KeystoreService.P2_DC_KEY) shouldBe ("")
      MockKeystore.map(KeystoreService.P1_TRIGGER_DC_KEY) shouldBe ("")
      MockKeystore.map(KeystoreService.P2_TRIGGER_DC_KEY) shouldBe ("")
      MockKeystore.map(KeystoreService.TRIGGER_DATE_KEY) shouldBe ("")
      MockKeystore.map(KeystoreService.TE_YES_NO_KEY) shouldBe ("")
    }

    "return TaxYearSelection results from keystore" in new ControllerWithMockKeystore {
      // set up
      val request = FakeRequest(POST, endPointURL).withSession {(SessionKeys.sessionId,SESSION_ID)}

      // test
      val result: Future[Result] = ControllerWithMockKeystore.onYearSelected()(request)

      // check
      val htmlSummaryPage = contentAsString(await(result))
      htmlSummaryPage should include ("Choose tax years")
    }

  }
}
