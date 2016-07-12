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
import play.api.mvc.{ Request, Result}
import play.api.test.Helpers._
import play.api.test.{FakeApplication, FakeRequest}
import service.KeystoreService
import service.KeystoreService._
import uk.gov.hmrc.play.http.{HeaderCarrier, SessionKeys}
import uk.gov.hmrc.play.test.UnitSpec
import scala.concurrent.Future
import form._

class SelectSchemeControllerSpec extends test.BaseSpec {
  val endPointURL = "/paac/scheme/2015"
  val postEndPointURL = "/paac/scheme"

  trait ControllerWithMockKeystore extends MockKeystoreFixture {
    object MockSelectSchemeControllerWithMockKeystore extends SelectSchemeController {
      override val keystore: KeystoreService = MockKeystore
    }
  }

  "SelectSchemeController" when {
    "GET with routes" should {
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
      "have keystore with no values and display select scheme options" in new ControllerWithMockKeystore {
        // setup
        val request = FakeRequest(GET,"").withSession{(SessionKeys.sessionId,SESSION_ID)}

        // test
        val result : Future[Result] = MockSelectSchemeControllerWithMockKeystore.onPageLoad(2015)(request)

        // check
        status(result) shouldBe 200
        val htmlPage = contentAsString(await(result))
        htmlPage should include (""" <input type="checkbox" id="definedBenefit"""")
        htmlPage should include (""" <input type="checkbox" id="definedContribution"""")
      }

      "have keystore with DB and DC schemeType flag value when we revisit the same page" in new ControllerWithMockKeystore {
        // setup
        val request = FakeRequest(GET,"").withSession{(SessionKeys.sessionId,SESSION_ID)}
        MockKeystore.map = MockKeystore.map + (DB_FLAG_PREFIX + "2015" -> "true")
        MockKeystore.map = MockKeystore.map + (DC_FLAG_PREFIX + "2015" -> "true")

        // test
        val result : Future[Result] = MockSelectSchemeControllerWithMockKeystore.onPageLoad(2015)(request)

        // check
        status(result) shouldBe 200
      }

    }

    "onSubmit with POST request" should {
      "not return result NOT_FOUND" in {
        val result: Option[Future[Result]] = route(FakeRequest(POST, postEndPointURL))
        result.isDefined shouldBe true
        status(result.get) should not be NOT_FOUND
      }

      "return 303 for valid GET request" in {
        val result: Option[Future[Result]] = route(FakeRequest(POST, postEndPointURL))
        status(result.get) shouldBe 303
      }

      "with valid DB and DC schemeType flag value should save to keystore" in new ControllerWithMockKeystore{
        // set up
        MockKeystore.map = MockKeystore.map + (FIRST_DC_YEAR_KEY -> "")
        MockKeystore.map = MockKeystore.map + (SELECTED_INPUT_YEARS_KEY -> "2015")
        implicit val hc = HeaderCarrier()
        implicit val request = FakeRequest(POST, postEndPointURL).withSession((SessionKeys.sessionId,SESSION_ID))
                                               .withFormUrlEncodedBody(("definedContribution" -> "true"),("definedBenefit" -> "true"),("year"->"2015"),("firstDCYear"->"2015"))

        // test
        val result: Future[Result] = MockSelectSchemeControllerWithMockKeystore.onSubmit()(request)

        // check
        status(result) shouldBe 303
        MockKeystore.map should contain key (DB_FLAG_PREFIX + "2015")
        MockKeystore.map should contain key (DC_FLAG_PREFIX + "2015")
        MockKeystore.map should contain value ("true")
      }

      "with invalid DB and DC schemeType flag value should show errors" in new ControllerWithMockKeystore{
        // set up
        implicit val hc = HeaderCarrier()
        implicit val request = FakeRequest(POST, postEndPointURL).withSession((SessionKeys.sessionId,SESSION_ID))
                                               .withFormUrlEncodedBody(("definedContribution" -> ""),
                                                                       ("definedBenefit" -> ""),
                                                                       ("year"->"2015"),
                                                                       ("firstDCYear"->"2015"))

        // test
        val result: Future[Result] = MockSelectSchemeControllerWithMockKeystore.onSubmit()(request)

        // check
        status(result) shouldBe 200
        val htmlPage = contentAsString(await(result))
        htmlPage should include ("There was a problem")
      }

      "with false DB and DC schemeType flag value should show errors" in new ControllerWithMockKeystore{
        // set up
        implicit val hc = HeaderCarrier()
        implicit val request = FakeRequest(POST, postEndPointURL).withSession((SessionKeys.sessionId,SESSION_ID))
                                               .withFormUrlEncodedBody(("definedContribution" -> "false"),
                                                                       ("definedBenefit" -> "false"),
                                                                       ("year"->"2015"),
                                                                       ("firstDCYear"->"2015"))
        MockKeystore.map = MockKeystore.map + (FIRST_DC_YEAR_KEY -> "")

        // test
        val result: Future[Result] = MockSelectSchemeControllerWithMockKeystore.onSubmit()(request)

        // check
        status(result) shouldBe 200
        val htmlPage = contentAsString(await(result))
        htmlPage should include ("Please select each pension scheme type you are a member of.")
      }
    }
  }

  "SelectSchemeForm" should {
    "correctly unbind" in {
      // set up
      val model = SelectSchemeModel(true, true, "2015", 2015)
      val theForm = SelectSchemeForm.form.bind(Map("definedContribution" -> "false","definedContribution" -> "false","firstDCYear"->"2015","year"->"2015"))

      // test
      val map = theForm.mapping.unbind(model)

      // check
      map("definedContribution") shouldBe "true"
      map("definedContribution") shouldBe "true"
    }
  }
}
