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


class PensionInputsControllerSpec extends UnitSpec with BeforeAndAfterAll {
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

  trait MockKeystoreFixture {
    object MockKeystore extends KeystoreService {
      var map = Map("definedBenefit_2006" -> "500000",
                    "definedBenefit_2007" -> "600000",
                    "definedBenefit_2008" -> "700000",
                    "definedBenefit_2009" -> "800000",
                    "definedBenefit_2010" -> "900000",
                    "definedBenefit_2011" -> "1000000",
                    "definedBenefit_2012" -> "1100000",
                    "definedBenefit_2013" -> "1200000",
                    "definedBenefit_2014" -> "1300000",
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

  trait ControllerWithMockKeystore extends MockKeystoreFixture {
    object PensionInputsControllerMockedKeystore extends PensionInputsController {
      override val keystore: KeystoreService = MockKeystore
    }
  }

  "PensionInputsController" should {
    "companion object has keystore" in {
      PensionInputsController.keystore shouldBe KeystoreService
    }

    "onPageLoad" can {
      "with keystore containing no values display blank field" in new ControllerWithMockKeystore {
        // setup
        MockKeystore.map = MockKeystore.map - "definedBenefit_2014"
        val request = FakeRequest(GET,"").withSession{(SessionKeys.sessionId,SESSION_ID)}

        // test
        val result : Future[Result] = PensionInputsControllerMockedKeystore.onPageLoad()(request)

        // check 
        status(result) shouldBe 200
        val htmlPage = contentAsString(await(result))
        htmlPage should include ("""<input type="number" name="definedBenefit_2014" id="definedBenefit" min="0" step="1" value='' max="9999999.99" size="10" style="width:9em;"/>""")
      }

      "with keystore containing a 2014 value should display value" in new ControllerWithMockKeystore {
        // setup
        val request = FakeRequest(GET,"").withSession{(SessionKeys.sessionId,SESSION_ID)}

        // test
        val result : Future[Result] = PensionInputsControllerMockedKeystore.onPageLoad()(request)

        // check 
        status(result) shouldBe 200
        val htmlPage = contentAsString(await(result))
        htmlPage should include ("""<input type="number" name="definedBenefit_2014" id="definedBenefit" min="0" step="1" value='13000.00' max="9999999.99" size="10" style="width:9em;"/>""")
      }

      "with keystore containing 0 display field with 0.00" in new ControllerWithMockKeystore {
        // setup
        MockKeystore.map = MockKeystore.map + ("definedBenefit_2014" -> "0")
        val request = FakeRequest(GET,"").withSession{(SessionKeys.sessionId,SESSION_ID)}

        // test
        val result : Future[Result] = PensionInputsControllerMockedKeystore.onPageLoad()(request)

        // check 
        status(result) shouldBe 200
        val htmlPage = contentAsString(await(result))
        htmlPage should include ("""<input type="number" name="definedBenefit_2014" id="definedBenefit" min="0" step="1" value='0.00' max="9999999.99" size="10" style="width:9em;"/>""")
      }
    }
  }

  "onSubmit" can {
    "with blank value returns to page with errors" in new ControllerWithMockKeystore {
        // setup
        val request = FakeRequest(POST,"").withFormUrlEncodedBody(("definedBenefit_2014"," ")).withSession{(SessionKeys.sessionId,SESSION_ID)}

        // test
        val result : Future[Result] = PensionInputsControllerMockedKeystore.onSubmit()(request)

        // check 
        status(result) shouldBe 200
        val htmlPage = contentAsString(await(result))
        htmlPage should include ("""2014/15 amount must not be empty. (Amount must have no more than 10 numbers, including 2 decimal places, and should be £0.00 or larger and under £99999999.99.)""")
    }

    "with valid input amount should save to keystore" in new ControllerWithMockKeystore {
      // set up
      MockKeystore.map = MockKeystore.map - "definedBenefit_2014"
      implicit val hc = HeaderCarrier()
      implicit val request = FakeRequest(POST,"/paac/pensionInputs").withSession{(SessionKeys.sessionId,SESSION_ID)}.withFormUrlEncodedBody(("definedBenefit_2014"->"1234.56"))
      def readAmount[T]()(implicit hc: HeaderCarrier,
                       format: play.api.libs.json.Format[T],
                       request: Request[Any]) : Future[Option[String]] = MockKeystore.read[String]("definedBenefit_2014")

      // test
      val result : Future[Result] = PensionInputsControllerMockedKeystore.onSubmit()(request)

      // check
      status(result) shouldBe 303
      MockKeystore.map should contain key ("definedBenefit_2014")
      MockKeystore.map should contain value ("123456") // big decimal converted to pence value
    }
  }
}
