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

import play.api.i18n.Lang
import play.api.mvc.{Cookie, Result}
import play.api.test.FakeRequest
import play.api.test.Helpers._
import service.KeystoreService
import uk.gov.hmrc.play.http.SessionKeys

import scala.concurrent.Future

class LanguageSupportSpec extends test.BaseSpec {
    "StartPageController" should {
      "Check lang=en language swich" in new MockKeystoreFixture {
        // set up
        val request = FakeRequest(GET, "/paac/setLanguage").withSession {(SessionKeys.sessionId,SESSION_ID)}

        object MockedStartPageController extends StartPageController with AppTestSettings {
          def keystore: KeystoreService = MockKeystore
        }

        // test
        val result : Future[Result] = MockedStartPageController.setLanguage()(request)

        // check
        status(result) shouldBe 303
        val StartPage = contentAsString(await(result))
        StartPage should include ("")
        redirectLocation(result) shouldBe Some("/paac")
      }
      "Check lang=cy language swich" in new MockKeystoreFixture {
        // set up
        val request = FakeRequest(GET, "/paac/setLanguage?lang=cy").withSession {(SessionKeys.sessionId,SESSION_ID)}

        object MockedStartPageController extends StartPageController with AppTestSettings {
          def keystore: KeystoreService = MockKeystore
        }

        // test
        val result : Future[Result] = MockedStartPageController.setLanguage()(request)

        // check
        status(result) shouldBe 303
        val StartPage = contentAsString(await(result))
        StartPage should include ("")
        redirectLocation(result) shouldBe Some("/paac")
      }
    }
}
