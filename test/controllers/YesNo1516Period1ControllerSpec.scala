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
import play.api.mvc.{Request, Result}
import play.api.test.Helpers._
import play.api.test.{FakeApplication, FakeRequest}
import service.KeystoreService
import uk.gov.hmrc.play.http.{HeaderCarrier, SessionKeys}
import uk.gov.hmrc.play.test.UnitSpec
import form._
import scala.concurrent.Future

class YesNo1516Period1ControllerSpec extends test.BaseSpec {
  val endPointURL = "/paac/yesno1516p1"

  trait ControllerWithMockKeystore extends MockKeystoreFixture{
    object MockYesNo1516Period1ControllerrWithMockKeystore extends YesNo1516Period1Controller {
      val yesNoKesystoreKey = "yesnoFor1516P1"
      val yesNoFormKey = "yesNo"
      override val keystore: KeystoreService = MockKeystore
    }
  }

  "YesNo1516Period1Controller" when {
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
      "have keystore with no values and display yes no for period 1 options" in new ControllerWithMockKeystore {
        // setup
        val request = FakeRequest(GET,"").withSession{(SessionKeys.sessionId,SESSION_ID)}

        // test
        val result : Future[Result] = MockYesNo1516Period1ControllerrWithMockKeystore.onPageLoad()(request)

        // check
        status(result) shouldBe 200
        val htmlPage = contentAsString(await(result))
        //htmlPage should include ("""<input id="scheme-type" type="radio" name="schemeType" value="dc" checked >""")
      }

      "have keystore with yesnoFor1516P1 value when we revisit the same page" in new ControllerWithMockKeystore {
        // setup
        val request = FakeRequest(GET,"").withSession{(SessionKeys.sessionId,SESSION_ID)}
        MockKeystore.map = MockKeystore.map + ("yesnoFor1516P1" -> "yes")

        // test
        val result : Future[Result] = MockYesNo1516Period1ControllerrWithMockKeystore.onPageLoad()(request)

        // check
        status(result) shouldBe 200
        MockKeystore.map should contain key ("yesnoFor1516P1")
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

      "with invalid yesno key name should show same form with errors and response code 200" in new ControllerWithMockKeystore{
        // set up
        implicit val hc = HeaderCarrier()
        implicit val request = FakeRequest(POST, endPointURL).withSession((SessionKeys.sessionId,SESSION_ID)).withFormUrlEncodedBody(("yesnoFor1516P1" -> "Yes"))

        // test
        val result: Future[Result] = MockYesNo1516Period1ControllerrWithMockKeystore.onSubmit()(request)

        // check
        status(result) shouldBe 200
      }

      "with valid yesNo key and value should save yesnoFor1516P1 key to keystore" in new ControllerWithMockKeystore{
        // set up
        implicit val hc = HeaderCarrier()
        implicit val request = FakeRequest(POST, endPointURL).withSession((SessionKeys.sessionId,SESSION_ID)).withFormUrlEncodedBody(("yesNo" -> "Yes"))

        // test
        val result: Future[Result] = MockYesNo1516Period1ControllerrWithMockKeystore.onSubmit()(request)

        // check
        status(result) shouldBe 303
        MockKeystore.map should contain key ("yesnoFor1516P1")
        MockKeystore.map should contain value ("Yes")
      }

      "with yesNo = Yes should forward to 2015/16 Period-1 InputPage" in new ControllerWithMockKeystore{
        // set up
        implicit val hc = HeaderCarrier()
        implicit val request = FakeRequest(POST, endPointURL).withSession((SessionKeys.sessionId,SESSION_ID)).withFormUrlEncodedBody(("yesNo" -> "Yes"))

        // test
        val result: Future[Result] = MockYesNo1516Period1ControllerrWithMockKeystore.onSubmit()(request)

        // check
        status(result) shouldBe 303
        MockKeystore.map should contain key ("yesnoFor1516P1")
        MockKeystore.map should contain value ("Yes")
        redirectLocation(result) shouldBe Some("/paac/pensionInputs1516p1")
      }

      "with yesNo = No should forward to 2015/16 Period-2 Yes/No Page" in new ControllerWithMockKeystore{
        // set up
        implicit val hc = HeaderCarrier()
        implicit val request = FakeRequest(POST, endPointURL).withSession((SessionKeys.sessionId,SESSION_ID)).withFormUrlEncodedBody(("yesNo" -> "No"))

        // test
        val result: Future[Result] = MockYesNo1516Period1ControllerrWithMockKeystore.onSubmit()(request)

        // check
        status(result) shouldBe 303
        MockKeystore.map should contain key ("yesnoFor1516P1")
        MockKeystore.map should contain value ("No")
        redirectLocation(result) shouldBe Some("/paac/yesno1516p2")
      }
    }
  }

  "YesNo1516Period1Form" should {
    "correctly unbind" in {
      // set up
      val model = "Yes"
      val theForm = YesNo1516Period1Form.form.bind(Map("yesNo" -> "No"))

      // test
      val map = theForm.mapping.unbind(model)

      // check
      map("yesNo") shouldBe "Yes"
    }
  }
}
