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

import test._
import play.api.mvc.Results._
import play.api.mvc._
import play.api.test.FakeRequest
import play.api.test.Helpers._
import service._
import uk.gov.hmrc.play.http.{HeaderCarrier, SessionKeys}
import play.api.libs.json._

import scala.concurrent.Future

class RedirectControllerSpec extends BaseSpec {

  trait Fixture extends MockKeystoreFixture {
    val mockKeystore = MockKeystore
    val controller = new RedirectController() {
      def keystore: KeystoreService = mockKeystore
    }
  }

  "RichPageLocation" should {
    "updateYear" must {
      "return false when location is Start" in new Fixture {
        // set up
        val location = new controller.RichPageLocation(Start()) {
          def testUpdateYear() = super.updateYear()
          def testIsCheckYourAnswersPage(location:PageLocation) = isCheckYourAnswersPage(location)
          def testMove(e:Event, newSessionData: Map[String,String])(implicit request: Request[Any]) = move(e, newSessionData)(request)
        }

        // test
        val result = location.testUpdateYear()

        // check
        result shouldBe false
      }
    }

    "isCheckYourAnswersPage" must {
      "return true when location is CheckYourAnswers" in new Fixture {
        // set up
        val location = new controller.RichPageLocation(CheckYourAnswers()) {
          def testUpdateYear() = super.updateYear()
          def testIsCheckYourAnswersPage(location:PageLocation) = isCheckYourAnswersPage(location)
          def testMove(e:Event, newSessionData: Map[String,String])(implicit request: Request[Any]) = move(e, newSessionData)(request)
        }

        // test
        val result = location.testIsCheckYourAnswersPage(CheckYourAnswers())

        // check
        result shouldBe true
      }
    }

    "move" must {
      "if no current year use first selected year" in new Fixture {
        // set up
        val location = new controller.RichPageLocation(CheckYourAnswers(PageState(year=2014))) {
          def testMove(e:Event, newSessionData: Map[String,String])(implicit request: Request[Any]) = move(e, newSessionData)(request)
        }

        // test
        val result = await(location.testMove(Forward, Map[String,String]((KeystoreService.SELECTED_INPUT_YEARS_KEY->"2014"),
                                                                         (KeystoreService.CURRENT_INPUT_YEAR_KEY->""))))

        // check
        redirectLocation(result) shouldBe Some("/paac/review")
      }
    }
  }
}
