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

import form._
import play.api.mvc.Result
import play.api.test.FakeRequest
import play.api.test.Helpers._
import service.KeystoreService
import uk.gov.hmrc.play.http.{HeaderCarrier, SessionKeys}

import scala.concurrent.Future

class YesNoThresholdIncomeControllerSpec extends test.BaseSpec {
  val endPointURL = "/paac/yesnothresholdincome"

  trait ControllerWithMockKeystore extends MockKeystoreFixture{
    object MockYesNoThresholdIncomeControllerWithMockKeystore extends YesNoThresholdIncomeController {
      val yesNoKeystoreKey = "yesnoForThresholdIncome"
      val yesNoFormKey = "yesNo"
      override val keystore: KeystoreService = MockKeystore
    }
  }

  "YesNoThresholdIncomeController" when {
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
      "have keystore with no values and display Yes/NO option for Threshold Income" in new ControllerWithMockKeystore {
        // setup
        val request = FakeRequest(GET,"").withSession{(SessionKeys.sessionId,SESSION_ID)}

        // test
        val result : Future[Result] = MockYesNoThresholdIncomeControllerWithMockKeystore.onPageLoad()(request)

        // check
        status(result) shouldBe 200
        val htmlPage = contentAsString(await(result))
      }

      "have keystore with Yes/NO option for Threshold Income  when we revisit the same page" in new ControllerWithMockKeystore {
        // setup
        val request = FakeRequest(GET,"").withSession{(SessionKeys.sessionId,SESSION_ID)}
        MockKeystore.map = MockKeystore.map + ("yesnoForThresholdIncome" -> "yes")

        // test
        val result : Future[Result] = MockYesNoThresholdIncomeControllerWithMockKeystore.onPageLoad()(request)

        // check
        status(result) shouldBe 200
        MockKeystore.map should contain key ("yesnoForThresholdIncome")
        MockKeystore.map should contain value ("yes")
      }

    }

    "onSubmit with POST request" should {
      "not return result NOT_FOUND" in {
        val result: Option[Future[Result]] = route(FakeRequest(POST, endPointURL))
        result.isDefined shouldBe true
        status(result.get) should not be NOT_FOUND
      }

      "return 303 for valid GET request" in {
        val result: Option[Future[Result]] = route(FakeRequest(POST, endPointURL))
        status(result.get) shouldBe 303
      }


      "with valid yesNo key & value should save yesnoForThresholdIncome key to keystore" in new ControllerWithMockKeystore{
        // set up
        implicit val hc = HeaderCarrier()
        implicit val request = FakeRequest(POST, endPointURL).withSession((SessionKeys.sessionId,SESSION_ID))
                                                             .withFormUrlEncodedBody("yesNo" -> "Yes")

        // test
        val result: Future[Result] = MockYesNoThresholdIncomeControllerWithMockKeystore.onSubmit()(request)

        // check
        status(result) shouldBe 303
        MockKeystore.map should contain key ("yesnoForThresholdIncome")
        MockKeystore.map should contain value ("Yes")
      }

     "with yesNo = Yes should forward to Adjusted income Input Page" in new ControllerWithMockKeystore{
       // set up
       implicit val hc = HeaderCarrier()
       implicit val request = FakeRequest(POST, endPointURL).withSession((SessionKeys.sessionId,SESSION_ID))
                                                            .withFormUrlEncodedBody(("yesNo" -> "Yes"))

       // test
       val result: Future[Result] = MockYesNoThresholdIncomeControllerWithMockKeystore.onSubmit()(request)

       // check
       status(result) shouldBe 303
       MockKeystore.map should contain key ("yesnoForThresholdIncome")
       MockKeystore.map should contain value ("Yes")
       redirectLocation(result) shouldBe Some("/paac/adjustedincome")
     }

     "with yesNo = No should forward to review page" in new ControllerWithMockKeystore{
       // set up
       MockKeystore.map = MockKeystore.map + ("isEdit" -> "false")
       implicit val hc = HeaderCarrier()
       implicit val request = FakeRequest(POST, endPointURL).withSession((SessionKeys.sessionId,SESSION_ID))
                                                            .withFormUrlEncodedBody(("yesNo" -> "No"))

       // test
       val result: Future[Result] = MockYesNoThresholdIncomeControllerWithMockKeystore.onSubmit()(request)

       // check
       status(result) shouldBe 303
       MockKeystore.map should contain key ("yesnoForThresholdIncome")
       MockKeystore.map should contain value ("No")
       redirectLocation(result) shouldBe Some("/paac/review")
     }

    "with yesNo = No should set AdjustedIncome value to empty string" in new ControllerWithMockKeystore{
       // set up
       MockKeystore.map = MockKeystore.map + ("isEdit" -> "false")
       implicit val hc = HeaderCarrier()
       implicit val request = FakeRequest(POST, endPointURL).withSession((SessionKeys.sessionId,SESSION_ID))
                                                            .withFormUrlEncodedBody(("yesNo" -> "No"))

       // test
       val result: Future[Result] = MockYesNoThresholdIncomeControllerWithMockKeystore.onSubmit()(request)

       // check
       MockKeystore.map(KeystoreService.YEAR_1617_AI_KEY) shouldBe ("")
     }
    }
  }

  "YesNoThresholdIncome Form" should {
    "correctly unbind" in {
      // set up
      val model = "Yes"
      val theForm = YesNoMPAATriggerEventForm.form.bind(Map("yesNo" -> "No"))

      // test
      val map = theForm.mapping.unbind(model)

      // check
      map("yesNo") shouldBe "Yes"
    }
  }
}
