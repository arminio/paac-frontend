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

import play.api.test.Helpers._
import play.api.test.{FakeRequest}
import uk.gov.hmrc.play.test.UnitSpec
import uk.gov.hmrc.play.http.SessionKeys
import controllers.action._

class SessionProviderSpec extends UnitSpec {
  trait SessionProviderFixture {
    object SessionProvider extends SessionProvider {
    }
    val sessionProvider = SessionProvider
  }

  "SessionProvider" should {
    "create unique session id" in new SessionProviderFixture {
      // set up
      val request = FakeRequest()
      val id1 = sessionProvider.createSessionId()(request)
      val id2 = sessionProvider.createSessionId()(request)

      // check
      id1 should not be id2
    }

    "create keystore session with unique session id" in new SessionProviderFixture {
      // set up
      val request = FakeRequest()
      val id1 = sessionProvider.createSessionId()(request)

      // test
      val session = sessionProvider.createKeystoreSession()(request)

      // check
      (session get SessionKeys.sessionId) should not be id1
    }
  }
}
