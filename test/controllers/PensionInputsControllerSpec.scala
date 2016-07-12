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

import scala.concurrent.Future


class PensionInputsControllerSpec extends test.BaseSpec {

  trait ControllerWithMockKeystore extends MockKeystoreFixture {
    MockKeystore.map = Map("definedBenefit_2008" -> "700000",
                    "definedBenefit_2009" -> "800000",
                    "definedBenefit_2010" -> "900000",
                    "definedBenefit_2011" -> "1000000",
                    "definedBenefit_2012" -> "1100000",
                    "definedBenefit_2013" -> "1200000",
                    "definedBenefit_2014" -> "1300000",
                    "definedBenefit_2014" -> "1300000",
                    SessionKeys.sessionId -> SESSION_ID,
                    KeystoreService.CURRENT_INPUT_YEAR_KEY -> "2014",
                    KeystoreService.SELECTED_INPUT_YEARS_KEY -> "2014"
                    )

    object PensionInputsControllerMockedKeystore extends PensionInputsController {
      override val keystore: KeystoreService = MockKeystore
    }
  }

  "PensionInputsController" should {
    "companion object has keystore" in {
      PensionInputsController.keystore shouldBe KeystoreService
    }

    "onPageLoad" can {
      "with keystore containing DB flag as true values display DB blank field" in new ControllerWithMockKeystore {
        // setup
        MockKeystore.map = MockKeystore.map - "definedBenefit_2014"
        MockKeystore.map = MockKeystore.map + ("definedBenefit" -> "true")
        val request = FakeRequest(GET,"").withSession{(SessionKeys.sessionId,SESSION_ID)}

        // test
        val result : Future[Result] = PensionInputsControllerMockedKeystore.onPageLoad()(request)

        // check
        status(result) shouldBe 200
        val htmlPage = contentAsString(await(result))
        htmlPage should include ("""<input type="number" name="definedBenefits.amount_2014" id="definedBenefits.amount_2014" min="0" step="1" value='' max="5000000.00" size="10" style="width:9em;"/>""")
      }

      "with keystore containing DB = true value display only DB blank fields" in new ControllerWithMockKeystore {
        // setup
        MockKeystore.map = MockKeystore.map - "definedBenefit_2014"
        MockKeystore.map = MockKeystore.map + ("definedBenefit" -> "true")
        val request = FakeRequest(GET,"").withSession{(SessionKeys.sessionId,SESSION_ID)}

        // test
        val result : Future[Result] = PensionInputsControllerMockedKeystore.onPageLoad()(request)

        // check
        status(result) shouldBe 200
        val htmlPage = contentAsString(await(result))
        htmlPage should include ("""<input type="number" name="definedBenefits.amount_2014" id="definedBenefits.amount_2014" min="0" step="1" value='' max="5000000.00" size="10" style="width:9em;"/>""")
      }

      "with keystore containing DB = false value, NOT display DB field, display only submit button" in new ControllerWithMockKeystore {
        // setup
        MockKeystore.map = MockKeystore.map + ("definedBenefit" -> "false")
        val request = FakeRequest(GET,"").withSession{(SessionKeys.sessionId,SESSION_ID)}

        // test
        val result : Future[Result] = PensionInputsControllerMockedKeystore.onPageLoad()(request)

        // check
        status(result) shouldBe 200
        val htmlPage = contentAsString(await(result))
        htmlPage should include("""<button id="submit" type="submit" class="button" value="Continue">Continue</button>""")
      }

      "with current year = -1 redirect to start" in new ControllerWithMockKeystore {
        // setup
        val request = FakeRequest(GET,"").withSession{(SessionKeys.sessionId,SESSION_ID)}
        MockKeystore.map = MockKeystore.map - KeystoreService.CURRENT_INPUT_YEAR_KEY - KeystoreService.SELECTED_INPUT_YEARS_KEY
        MockKeystore.map = MockKeystore.map + (KeystoreService.SELECTED_INPUT_YEARS_KEY -> "2014")
        MockKeystore.map = MockKeystore.map + (KeystoreService.CURRENT_INPUT_YEAR_KEY -> "-1")

        // test
        val result : Future[Result] = PensionInputsControllerMockedKeystore.onPageLoad()(request)

        // check
        status(result) shouldBe 303
        redirectLocation(result) shouldBe Some("/paac")
      }
    }
  }

  "onSubmit" can {
    "with blank value returns to page with errors" in new ControllerWithMockKeystore {
        // setup
        val request = FakeRequest(POST,"").withFormUrlEncodedBody(("definedBenefits.amount_2014"," ")).withSession{(SessionKeys.sessionId,SESSION_ID)}
        MockKeystore.map = MockKeystore.map + ("Current" -> "2014")
        MockKeystore.map = MockKeystore.map + ("definedBenefit" -> "true")

        // test
        val result : Future[Result] = PensionInputsControllerMockedKeystore.onSubmit()(request)

        // check
        status(result) shouldBe 200
        val htmlPage = contentAsString(await(result))
        htmlPage should include ("""There was a problem with the amounts you reported to us""")
    }

    "with empty db value returns to page with errors" in new ControllerWithMockKeystore {
        // setup
        val request = FakeRequest(POST,"").withFormUrlEncodedBody(("definedBenefits.amount_2014","")).withSession{(SessionKeys.sessionId,SESSION_ID)}
        MockKeystore.map = MockKeystore.map + ("Current" -> "2014")
        MockKeystore.map = MockKeystore.map + ("definedBenefit" -> "true")

        // test
        val result : Future[Result] = PensionInputsControllerMockedKeystore.onSubmit()(request)

        // check
        status(result) shouldBe 200
        val htmlPage = contentAsString(await(result))
        htmlPage should include ("There was a problem with the amounts you reported to us")
    }

    "with valid input amount should save to keystore" in new ControllerWithMockKeystore {
      // set up
      MockKeystore.map = MockKeystore.map - "definedBenefit_2014"
      MockKeystore.map = MockKeystore.map + ("Current" -> "2014")
      MockKeystore.map = MockKeystore.map + ("SelectedYears" -> "2014")
      MockKeystore.map = MockKeystore.map + ("definedBenefit" -> "true")
      MockKeystore.map = MockKeystore.map + ("isEdit" -> "false")
      implicit val hc = HeaderCarrier()
      implicit val request = FakeRequest(POST,"/paac/pensionInputs").withSession{(SessionKeys.sessionId,SESSION_ID)}.withFormUrlEncodedBody(("definedBenefits.amount_2014"->"1234"))

      // test
      val result : Future[Result] = PensionInputsControllerMockedKeystore.onSubmit()(request)

      // check
      status(result) shouldBe 303
      MockKeystore.map should contain key ("definedBenefit_2014")
      MockKeystore.map should contain value ("123400") // big decimal converted to pence value
    }
  }

  "onBack" should {
    "redirect to tax year selection" in new ControllerWithMockKeystore {
      // set up
      implicit val hc = HeaderCarrier()
      implicit val request = FakeRequest(GET,"/paac/backpensionInputs").withSession{(SessionKeys.sessionId,SESSION_ID)}

      // test
      val result : Future[Result] = PensionInputsControllerMockedKeystore.onBack()(request)

      // check
      status(result) shouldBe 303
      redirectLocation(result) shouldBe Some("/paac/taxyearselection")
    }
  }
}