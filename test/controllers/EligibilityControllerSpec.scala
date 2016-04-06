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

import java.util.UUID
import play.api.libs.concurrent.Execution.Implicits._
import org.scalatest.BeforeAndAfterAll
import org.scalatest.mock.MockitoSugar
import play.api.Play
import play.api.mvc.{Request, Result}
import play.api.test.Helpers._
import play.api.test.{FakeRequest, FakeApplication}
import service.KeystoreService
import uk.gov.hmrc.play.http.{HeaderCarrier, SessionKeys}
import uk.gov.hmrc.play.test.UnitSpec
import scala.concurrent.Future

class EligibilityControllerSpec extends UnitSpec with MockitoSugar with BeforeAndAfterAll {

  val app = FakeApplication()
  val SESSION_ID = s"session-${UUID.randomUUID}"

  override def beforeAll() {
    Play.start(app)
    super.beforeAll() // To be stackable, must call super.beforeEach
  }

  override def afterAll() {
    try {
      super.afterAll()
    } finally Play.stop()
  }

  implicit val request = FakeRequest()

  trait ControllerWithMockKeystore extends MockKeystoreFixture {
    object EligibilityControllerMockedKeystore extends EligibilityController {
      override val keystore: KeystoreService = MockKeystore
    }
  }

  trait MockKeystoreFixture {
    object MockKeystore extends KeystoreService {
      var map = Map("eligibility" -> "Yes",
        SessionKeys.sessionId -> SESSION_ID)
      override def store[T](data: T, key: String)
                           (implicit hc: HeaderCarrier,
                            format: play.api.libs.json.Format[T],
                            request: Request[Any])
      : Future[Option[T]] = {
       map = map + (key -> data.toString)
        Future.successful(Some(data))
      }
      override def read[T](key: String)
                          (implicit hc: HeaderCarrier,
                           format: play.api.libs.json.Format[T],
                           request: Request[Any])
      : Future[Option[T]] = {
        Future.successful((map get key).map(_.asInstanceOf[T]))
      }
    }
  }

  "EligibilityController" should {
    "not return result NOT_FOUND" in {
      val result : Option[Future[Result]] = route(FakeRequest(GET, "/paac/eligibility"))
      result.isDefined shouldBe true
      status(result.get) should not be NOT_FOUND
    }
    "return 303 for valid GET request" in {
      val result : Option[Future[Result]] = route(FakeRequest(GET, "/paac/eligibility"))
      status(result.get) shouldBe 303
    }

    "return Eligibility that was saved in keystore" in new MockKeystoreFixture {
      // set up
      object MockedEligibilityController extends EligibilityController {
        override val keystore: KeystoreService = MockKeystore
      }
      val request = FakeRequest(GET, "/paac/eligibility").withSession {(SessionKeys.sessionId,SESSION_ID)}

      // test
      val result: Future[Result] = MockedEligibilityController.onSubmit()(request)

      // check
      val htmlSummaryPage = contentAsString(await(result))
      htmlSummaryPage should include ("Were you a member of a registered pension scheme either in the UK or overseas?")
    }


  "with valid input amount should save to keystore" in new ControllerWithMockKeystore {
    // set up
    MockKeystore.map = MockKeystore.map - "eligibility"
    implicit val hc = HeaderCarrier()
    implicit val request = FakeRequest(POST,"/paac/eligibility").withSession{(SessionKeys.sessionId,SESSION_ID)}.withFormUrlEncodedBody(("eligibility"->"Yes"))


    // test
    val result : Future[Result] = EligibilityControllerMockedKeystore.onSubmit()(request)

    // check
    status(result) shouldBe 303
    MockKeystore.map should contain key ("eligibility")
    MockKeystore.map should contain value ("Yes")
    }
  }
}