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
import service._
import uk.gov.hmrc.play.http.{HeaderCarrier, SessionKeys}
import service.KeystoreService._
import scala.concurrent.Future

class PensionInputsControllerSpec extends test.BaseSpec {
  val DB_2014_KEY = s"${DB_PREFIX}2014"

  trait ControllerWithMockKeystore extends MockKeystoreFixture {
    MockKeystore.map = Map("definedBenefit_2008" -> "700000",
                    "definedBenefit_2009" -> "800000",
                    "definedBenefit_2010" -> "900000",
                    "definedBenefit_2011" -> "1000000",
                    "definedBenefit_2012" -> "1100000",
                    "definedBenefit_2013" -> "1200000",
                    DB_2014_KEY -> "1300000",
                    DB_2014_KEY -> "1300000",
                    SessionKeys.sessionId -> SESSION_ID,
                    CURRENT_INPUT_YEAR_KEY -> "2014",
                    SELECTED_INPUT_YEARS_KEY -> "2014"
                    )

    object PensionInputsControllerMockedKeystore extends PensionInputsController with AppTestSettings {
      def keystore: KeystoreService = MockKeystore
    }
  }

  "PensionInputsController" should {
    "onPageLoad" can {
      "with keystore containing DB flag as true values display DB blank field" in new ControllerWithMockKeystore {
        // setup
        MockKeystore.map = MockKeystore.map - DB_2014_KEY
        MockKeystore.map = MockKeystore.map + ("definedBenefit" -> "true")
        val request = FakeRequest(GET,"").withSession{(SessionKeys.sessionId,SESSION_ID)}

        // test
        val result : Future[Result] = PensionInputsControllerMockedKeystore.onPageLoad(2014)(request)

        // check
        status(result) shouldBe 200
        val htmlPage = contentAsString(await(result))
        htmlPage should include ("""<input type="number" name="definedBenefit_2014" id="definedBenefit_2014" """)
        dumpHtml("empty_pensionInputs", htmlPage)
      }

      "with keystore containing DB = true value display only DB blank fields" in new ControllerWithMockKeystore {
        // setup
        MockKeystore.map = MockKeystore.map - DB_2014_KEY
        MockKeystore.map = MockKeystore.map + ("definedBenefit" -> "true")
        val request = FakeRequest(GET,"").withSession{(SessionKeys.sessionId,SESSION_ID)}

        // test
        val result : Future[Result] = PensionInputsControllerMockedKeystore.onPageLoad(2014)(request)

        // check
        status(result) shouldBe 200
        val htmlPage = contentAsString(await(result))
        htmlPage should include ("""<input type="number" name="definedBenefit_2014" id="definedBenefit_2014" """)
      }

      "with keystore containing DB = false value, NOT display DB field, display only submit button" in new ControllerWithMockKeystore {
        // setup
        MockKeystore.map = MockKeystore.map + ("definedBenefit" -> "false")
        val request = FakeRequest(GET,"").withSession{(SessionKeys.sessionId,SESSION_ID)}

        // test
        val result : Future[Result] = PensionInputsControllerMockedKeystore.onPageLoad(2014)(request)

        // check
        status(result) shouldBe 200
        val htmlPage = contentAsString(await(result))
        htmlPage should include("""<button id="submit" type="submit" class="button" value="Continue">Continue</button>""")
      }

      "with current year = -1 redirect to start" in new ControllerWithMockKeystore {
        // setup
        val request = FakeRequest(GET,"").withSession{(SessionKeys.sessionId,SESSION_ID)}
        MockKeystore.map = MockKeystore.map - CURRENT_INPUT_YEAR_KEY - SELECTED_INPUT_YEARS_KEY
        MockKeystore.map = MockKeystore.map + (SELECTED_INPUT_YEARS_KEY -> "2014")
        MockKeystore.map = MockKeystore.map + (CURRENT_INPUT_YEAR_KEY -> "-1")

        // test
        val result : Future[Result] = PensionInputsControllerMockedKeystore.onPageLoad(-1)(request)

        // check
        status(result) shouldBe 303
        redirectLocation(result) shouldBe Some("/paac")
      }
    }
  }

  "onSubmit" can {
    "with blank value returns to page with errors" in new ControllerWithMockKeystore {
        // setup
        val request = FakeRequest(POST,"").withFormUrlEncodedBody((DB_2014_KEY," "),
                                                                  ("isDefinedBenefit","true"),
                                                                  ("isDefinedContribution","false"),
                                                                  ("year","2014"),
                                                                  ("isEdit","false")).withSession((SessionKeys.sessionId,SESSION_ID),
                                                                                                  ("Current" -> "2014"),
                                                                                                  ("definedBenefit" -> "true"))

        // test
        val result : Future[Result] = PensionInputsControllerMockedKeystore.onSubmit()(request)

        // check
        status(result) shouldBe 200
        val htmlPage = contentAsString(await(result))
        htmlPage should include ("""There is a problem""")
        dumpHtml("error_pensionInputs", htmlPage)
    }

    "with empty db value returns to page with errors" in new ControllerWithMockKeystore {
        // setup
        val request = FakeRequest(POST,"").withFormUrlEncodedBody((DB_2014_KEY,""),
                                                                  ("isDefinedBenefit","true"),
                                                                  ("isDefinedContribution","false"),
                                                                  ("year","2014"),
                                                                  ("isEdit","false")).withSession((SessionKeys.sessionId,SESSION_ID),
                                                                                                  ("Current" -> "2014"),
                                                                                                  ("definedBenefit" -> "true"))
        // test
        val result : Future[Result] = PensionInputsControllerMockedKeystore.onSubmit()(request)

        // check
        status(result) shouldBe 200
        val htmlPage = contentAsString(await(result))
        htmlPage should include ("There is a problem")
    }

    "with valid input amount should save to keystore" in new ControllerWithMockKeystore {
      // set up
      val formData = List((DB_2014_KEY->"1234"),
                          ("isDefinedBenefit"->"true"),
                          ("isDefinedContribution"->"false"),
                          ("year"->"2014"),
                          ("isEdit"->"false"))
      val sessionData = List(("Current" -> "2014"),
                             ("SelectedYears" -> "2014"),
                             ("definedBenefit" -> "true"),
                             ("isEdit" -> "false"),
                             (SessionKeys.sessionId,SESSION_ID))
      implicit val hc = HeaderCarrier()
      implicit val request = FakeRequest(POST,"/paac/pensionInputs").withSession(sessionData: _*).withFormUrlEncodedBody(formData: _*)

      // test
      val result : Future[Result] = PensionInputsControllerMockedKeystore.onSubmit()(request)

      // check
      status(result) shouldBe 303
      MockKeystore.map(DB_2014_KEY) shouldBe ("123400") // big decimal converted to pence value
    }
  }

  "onBack" should {
    "redirect to tax year selection" in new ControllerWithMockKeystore {
      // set up
      implicit val hc = HeaderCarrier()
      val sessionData = List(("Current" -> "2014"),
                             ("SelectedYears" -> "2014"),
                             ("isEdit" -> "false"),
                             (SessionKeys.sessionId,SESSION_ID))
      implicit val request = FakeRequest(GET,"/paac/backpensionInputs").withSession(sessionData: _*)

      // test
      val result : Future[Result] = PensionInputsControllerMockedKeystore.onBack(2016)(request)

      // check
      status(result) shouldBe 303
      redirectLocation(result) shouldBe Some("/paac/taxyearselection")
    }
  }
}