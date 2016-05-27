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

import scala.concurrent.Future
import form._

class DateOfMPAATriggerEventControllerSpec extends test.BaseSpec {

  trait ControllerWithMockKeystore extends MockKeystoreFixture {
    object ControllerWithMockKeystore extends DateOfMPAATriggerEventController {
      override val keystore: KeystoreService = MockKeystore
    }
  }

  "DateOfMPAATriggerEventControllerSpec" when {
    "onPageLoad" should {
      "display form page with no date if no keystore value" in new ControllerWithMockKeystore {
        // setup
        val request = FakeRequest(GET,"").withSession{(SessionKeys.sessionId,SESSION_ID)}
        MockKeystore.map = MockKeystore.map + (KeystoreService.TRIGGER_DATE_KEY -> "")

        // do it
        val result : Future[Result] = ControllerWithMockKeystore.onPageLoad()(request)

        // check
        status(result) shouldBe 200
        val htmlPage = contentAsString(await(result))
        htmlPage should include ("""value=""""")
      }

      "display form page with date if keystore value exists" in new ControllerWithMockKeystore {
        // setup
        val request = FakeRequest(GET,"").withSession{(SessionKeys.sessionId,SESSION_ID)}
        MockKeystore.map = MockKeystore.map + (KeystoreService.TRIGGER_DATE_KEY -> "2015-11-1")

        // do it
        val result : Future[Result] = ControllerWithMockKeystore.onPageLoad()(request)

        // check
        status(result) shouldBe 200
        val htmlPage = contentAsString(await(result))
        htmlPage should include ("""value="2015"""")
        htmlPage should include ("""value="11"""")
        htmlPage should include ("""value="1"""")
      }
    }

    "onSubmit" should {
      "with valid form save date into keystore" in new ControllerWithMockKeystore {
        // set up
        implicit val hc = HeaderCarrier()
        implicit val request = FakeRequest(POST, "/paac/d").withSession((SessionKeys.sessionId,SESSION_ID)).withFormUrlEncodedBody(("dateOfMPAATriggerEvent.day" -> "4"),
                                    ("dateOfMPAATriggerEvent.month" -> "7"),
                                    ("dateOfMPAATriggerEvent.year" -> "2015"))

        // test
        val result : Future[Result] = ControllerWithMockKeystore.onSubmit()(request)

        // check
        status(result) shouldBe 303
        MockKeystore.map should contain key (KeystoreService.TRIGGER_DATE_KEY)
        MockKeystore.map should contain value ("2015-07-04")
      }

      "with invalid form display page again" in new ControllerWithMockKeystore {
        // set up
        implicit val hc = HeaderCarrier()
        implicit val request = FakeRequest(POST, "/paac/d").withSession((SessionKeys.sessionId,SESSION_ID)).withFormUrlEncodedBody(("dateOfMPAATriggerEvent.day" -> "4"),
                                    ("dateOfMPAATriggerEvent.month" -> "7"),
                                    ("dateOfMPAATriggerEvent.year" -> ""))

        // test
        val result : Future[Result] = ControllerWithMockKeystore.onSubmit()(request)

        // check
        status(result) shouldBe 200
        val htmlPage = contentAsString(await(result))
        // page should contain errors TODO
      }
    }
  }

  "DateOfMPAATriggerEventForm" when {
    "on validate" should {
      "return false when year is negative" in new ControllerWithMockKeystore {
        DateOfMPAATriggerEventForm.form.bind(Map(
          "dateOfMPAATriggerEvent.day" -> "-1",
          "dateOfMPAATriggerEvent.month" -> "-1",
          "dateOfMPAATriggerEvent.year" -> "-1111"
        )).fold(
          formWithErrors => {
            formWithErrors.errors.find(_.key == "dateOfMPAATriggerEvent") should not be None
          },
          success =>
            success should not be Some(-1)
        )
      }
      "return false when year is short" in new ControllerWithMockKeystore {
        DateOfMPAATriggerEventForm.form.bind(Map(
          "dateOfMPAATriggerEvent.day" -> "-1",
          "dateOfMPAATriggerEvent.month" -> "-1",
          "dateOfMPAATriggerEvent.year" -> "9"
        )).fold(
          formWithErrors => {
            formWithErrors.errors.find(_.key == "dateOfMPAATriggerEvent") should not be None
          },
          success =>
            success should not be Some(-1)
        )
      }
    }
  }
}
