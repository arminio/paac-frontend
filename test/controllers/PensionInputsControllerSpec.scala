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
import org.scalatest.mock._
import org.scalatest.concurrent.Eventually._
import org.scalatest.Matchers._ 

import scala.concurrent.{Future, Await}
import scala.concurrent.duration._

import java.util.UUID

import play.api.test.Helpers._
import play.api.test.{FakeRequest, FakeApplication}
import play.api.Play
import play.api.mvc.{AnyContent, Request, Result}
import play.api.test.Helpers._

import service._
import uk.gov.hmrc.play.test.UnitSpec
import uk.gov.hmrc.http.cache.client.SessionCache
import uk.gov.hmrc.play.http.{HeaderCarrier, SessionKeys}


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
                    SessionKeys.sessionId -> SESSION_ID)
    object PensionInputsControllerMockedKeystore extends PensionInputsController {
      override val keystore: KeystoreService = MockKeystore
    }
  }

  "PensionInputsController" should {
    "companion object has keystore" in {
      PensionInputsController.keystore shouldBe KeystoreService
    }

    "onPageLoad" can {
      "with keystore containing DB and DC flags as true values display both DC and DB blank fields" in new ControllerWithMockKeystore {
        // setup
        MockKeystore.map = MockKeystore.map - "definedBenefit_2014"
        MockKeystore.map = MockKeystore.map + ("definedBenefit" -> "true")
        MockKeystore.map = MockKeystore.map + ("definedContribution" -> "true")
        val request = FakeRequest(GET,"").withSession{(SessionKeys.sessionId,SESSION_ID)}

        // test
        val result : Future[Result] = PensionInputsControllerMockedKeystore.onPageLoad()(request)

        // check
        status(result) shouldBe 200
        val htmlPage = contentAsString(await(result))
        htmlPage should include ("""<input type="number" name="definedBenefits.amount_2014" id="definedBenefits.amount_2014" min="0" step="1" value='' max="5000000.00" size="10" style="width:9em;"/>""")
        htmlPage should include ("""<input type="number" name="definedContributions.amount_2014" id="definedContributions.amount_2014" min="0" step="1" value='' max="5000000.00" size="10" style="width:9em;"/>""")
      }

      "with keystore containing DB = true and DC = false values display only DB blank fields" in new ControllerWithMockKeystore {
        // setup
        MockKeystore.map = MockKeystore.map - "definedBenefit_2014"
        MockKeystore.map = MockKeystore.map + ("definedBenefit" -> "true")
        MockKeystore.map = MockKeystore.map + ("definedContribution" -> "false")
        val request = FakeRequest(GET,"").withSession{(SessionKeys.sessionId,SESSION_ID)}

        // test
        val result : Future[Result] = PensionInputsControllerMockedKeystore.onPageLoad()(request)

        // check
        status(result) shouldBe 200
        val htmlPage = contentAsString(await(result))
        htmlPage should include ("""<input type="number" name="definedBenefits.amount_2014" id="definedBenefits.amount_2014" min="0" step="1" value='' max="5000000.00" size="10" style="width:9em;"/>""")
      }

      "with keystore containing DB = false and DC = true values display only DC blank fields" in new ControllerWithMockKeystore {
        // setup
        MockKeystore.map = MockKeystore.map + ("definedBenefit" -> "false")
        MockKeystore.map = MockKeystore.map + ("definedContribution" -> "true")
        val request = FakeRequest(GET,"").withSession{(SessionKeys.sessionId,SESSION_ID)}

        // test
        val result : Future[Result] = PensionInputsControllerMockedKeystore.onPageLoad()(request)

        // check 
        status(result) shouldBe 200
        val htmlPage = contentAsString(await(result))
        htmlPage should include("""<input type="number" name="definedContributions.amount_2014" id="definedContributions.amount_2014" min="0" step="1" value='' max="5000000.00" size="10" style="width:9em;"/>""")
      }

      "with keystore containing DB = false and DC = false values, NOT display DB and DC fields, display only submit button" in new ControllerWithMockKeystore {
        // setup
        MockKeystore.map = MockKeystore.map + ("definedBenefit" -> "false")
        MockKeystore.map = MockKeystore.map + ("definedContribution" -> "false")
        val request = FakeRequest(GET,"").withSession{(SessionKeys.sessionId,SESSION_ID)}

        // test
        val result : Future[Result] = PensionInputsControllerMockedKeystore.onPageLoad()(request)

        // check
        status(result) shouldBe 200
        val htmlPage = contentAsString(await(result))
        htmlPage should include("""<button id="submit" type="submit" class="button" value="Continue">Continue</button>""")
      }

      "with current year 2015 redirect to review" in new ControllerWithMockKeystore {
        // setup
        MockKeystore.map = MockKeystore.map + (KeystoreService.CURRENT_INPUT_YEAR_KEY -> "2015")
        val request = FakeRequest(GET,"").withSession{(SessionKeys.sessionId,SESSION_ID)}

        // test
        val result : Future[Result] = PensionInputsControllerMockedKeystore.onPageLoad()(request)

        // check
        status(result) shouldBe 303
        redirectLocation(result) shouldBe Some("/paac/review")
      }

      "with current year -1 redirect to review" in new ControllerWithMockKeystore {
        // setup
        MockKeystore.map = MockKeystore.map + (KeystoreService.CURRENT_INPUT_YEAR_KEY -> "-1")
        val request = FakeRequest(GET,"").withSession{(SessionKeys.sessionId,SESSION_ID)}

        // test
        val result : Future[Result] = PensionInputsControllerMockedKeystore.onPageLoad()(request)

        // check
        status(result) shouldBe 303
        redirectLocation(result) shouldBe Some("/paac/review")
      }
    }
  }

  "onSubmit" can {
    "with blank value returns to page with errors" in new ControllerWithMockKeystore {
        // setup
        val request = FakeRequest(POST,"").withFormUrlEncodedBody(("definedBenefits.amount_2014"," ")).withSession{(SessionKeys.sessionId,SESSION_ID)}
        MockKeystore.map = MockKeystore.map + ("Current" -> "2014")
        MockKeystore.map = MockKeystore.map + ("definedBenefit" -> "true")
        MockKeystore.map = MockKeystore.map + ("definedContribution" -> "false")

        // test
        val result : Future[Result] = PensionInputsControllerMockedKeystore.onSubmit()(request)

        // check
        status(result) shouldBe 200
        val htmlPage = contentAsString(await(result))
        htmlPage should include ("""Use whole numbers only, not decimals or characters""")
    }

    "with empty db value returns to page with errors" in new ControllerWithMockKeystore {
        // setup
        val request = FakeRequest(POST,"").withFormUrlEncodedBody(("definedBenefits.amount_2014","")).withSession{(SessionKeys.sessionId,SESSION_ID)}
        MockKeystore.map = MockKeystore.map + ("Current" -> "2014")
        MockKeystore.map = MockKeystore.map + ("definedBenefit" -> "true")
        MockKeystore.map = MockKeystore.map + ("definedContribution" -> "false")

        // test
        val result : Future[Result] = PensionInputsControllerMockedKeystore.onSubmit()(request)

        // check
        status(result) shouldBe 200
        val htmlPage = contentAsString(await(result))
        htmlPage should include ("2014/15 defined benefit amount was incorrect or empty.")
    }

    "with empty dc value returns to page with errors" in new ControllerWithMockKeystore {
        // setup
        val request = FakeRequest(POST,"").withFormUrlEncodedBody(("definedContributions.amount_2014","")).withSession{(SessionKeys.sessionId,SESSION_ID)}
        MockKeystore.map = MockKeystore.map + ("Current" -> "2014")
        MockKeystore.map = MockKeystore.map + ("definedBenefit" -> "false")
        MockKeystore.map = MockKeystore.map + ("definedContribution" -> "true")

        // test
        val result : Future[Result] = PensionInputsControllerMockedKeystore.onSubmit()(request)

        // check
        status(result) shouldBe 200
        val htmlPage = contentAsString(await(result))
        htmlPage should include ("2014/15 defined contribution amount was incorrect or empty.")
    }

    "with valid input amount should save to keystore" in new ControllerWithMockKeystore {
      // set up
      MockKeystore.map = MockKeystore.map - "definedBenefit_2014"
      MockKeystore.map = MockKeystore.map + ("Current" -> "2014")
      MockKeystore.map = MockKeystore.map + ("SelectedYears" -> "2014")
      MockKeystore.map = MockKeystore.map + ("definedBenefit" -> "true")
      MockKeystore.map = MockKeystore.map + ("definedContribution" -> "false")
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
