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

class YesNo1516Period2ControllerSpec extends UnitSpec with BeforeAndAfterAll {
  val app = FakeApplication()
  val SESSION_ID = s"session-${UUID.randomUUID}"
  val endPointURL = "/paac/yesno1516p2"

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

  trait ControllerWithMockKeystore extends MockKeystoreFixture{
    object MockYesNo1516Period2ControllerrWithMockKeystore extends YesNo1516Period2Controller {
      val yesNoKesystoreKey = "yesnoFor1516P2"
      val yesNoFormKey = "yesNo"
      override val keystore: KeystoreService = MockKeystore
    }
  }

  trait MockKeystoreFixture {
    object MockKeystore extends KeystoreService {
      var map = Map(SessionKeys.sessionId -> SESSION_ID)
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

  "YesNo1516Period2Controller" when {
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
      "have keystore with no values and display select scheme options" in new ControllerWithMockKeystore {
        // setup
        val request = FakeRequest(GET,"").withSession{(SessionKeys.sessionId,SESSION_ID)}

        // test
        val result : Future[Result] = MockYesNo1516Period2ControllerrWithMockKeystore.onPageLoad()(request)

        // check
        status(result) shouldBe 200
        val htmlPage = contentAsString(await(result))
        //htmlPage should include ("""<input id="scheme-type" type="radio" name="schemeType" value="dc" checked >""")
      }

      "have keystore with yesnoFor1516P2 value when we revisit the same page" in new ControllerWithMockKeystore {
        // setup
        val request = FakeRequest(GET,"").withSession{(SessionKeys.sessionId,SESSION_ID)}
        MockKeystore.map = MockKeystore.map + ("yesnoFor1516P2" -> "yes")

        // test
        val result : Future[Result] = MockYesNo1516Period2ControllerrWithMockKeystore.onPageLoad()(request)

        // check
        status(result) shouldBe 200
        MockKeystore.map should contain key ("yesnoFor1516P2")
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
        implicit val request = FakeRequest(POST, endPointURL).withSession((SessionKeys.sessionId,SESSION_ID)).withFormUrlEncodedBody(("yesnoFor1516P2" -> "Yes"))

        // test
        val result: Future[Result] = MockYesNo1516Period2ControllerrWithMockKeystore.onSubmit()(request)

        // check
        status(result) shouldBe 200
      }

      "with valid yesNo key and value should save yesnoFor1516P2 key to keystore" in new ControllerWithMockKeystore{
        // set up
        implicit val hc = HeaderCarrier()
        implicit val request = FakeRequest(POST, endPointURL).withSession((SessionKeys.sessionId,SESSION_ID)).withFormUrlEncodedBody(("yesNo" -> "Yes"))

        // test
        val result: Future[Result] = MockYesNo1516Period2ControllerrWithMockKeystore.onSubmit()(request)

        // check
        status(result) shouldBe 303
        MockKeystore.map should contain key ("yesnoFor1516P2")
        MockKeystore.map should contain value ("Yes")
      }

      "with yesNo = Yes should forward to 2015/16 Period-1 InputPage" in new ControllerWithMockKeystore{
        // set up
        implicit val hc = HeaderCarrier()
        implicit val request = FakeRequest(POST, endPointURL).withSession((SessionKeys.sessionId,SESSION_ID)).withFormUrlEncodedBody(("yesNo" -> "Yes"))

        // test
        val result: Future[Result] = MockYesNo1516Period2ControllerrWithMockKeystore.onSubmit()(request)

        // check
        status(result) shouldBe 303
        MockKeystore.map should contain key ("yesnoFor1516P2")
        MockKeystore.map should contain value ("Yes")
        redirectLocation(result) shouldBe Some("/paac/pensionInputs1516p2")
      }

      /*"with yesNo = No should forward to 2015/16 Period-2 InputPage" in new ControllerWithMockKeystore{
        // set up
        implicit val hc = HeaderCarrier()
        implicit val request = FakeRequest(POST, endPointURL).withSession((SessionKeys.sessionId,SESSION_ID)).withFormUrlEncodedBody(("yesNo" -> "No"))

        // test
        val result: Future[Result] = MockYesNo1516Period2ControllerrWithMockKeystore.onSubmit()(request)

        // check
        status(result) shouldBe 303
        MockKeystore.map should contain key ("yesnoFor1516P2")
        MockKeystore.map should contain value ("No")
        redirectLocation(result) shouldBe Some("/paac/pensionInputs")
      }*/
    }
  }

  "YesNo1516Period2Form" should {
    "correctly unbind" in {
      // set up
      val model = "Yes"
      val theForm = YesNo1516Period2Form.form.bind(Map("yesNo" -> "No"))

      // test
      val map = theForm.mapping.unbind(model)

      // check
      map("yesNo") shouldBe "Yes"
    }
  }
}
