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
import play.api.mvc.Result
import play.api.test.Helpers._
import play.api.test.{FakeRequest, FakeApplication}
import uk.gov.hmrc.play.http.SessionKeys
import uk.gov.hmrc.play.test.UnitSpec
import scala.concurrent.Future
import play.api.libs.concurrent.Execution.Implicits._

class StaticPageControllerSpec extends UnitSpec with BeforeAndAfterAll {
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
  
  "StaticPageController" should {
    "pipOnPageLoad" should {
      "return 200" in {
        // set up
        val request = FakeRequest(GET, "/paac/changes-to-pip").withSession {(SessionKeys.sessionId,SESSION_ID)}

        // test
        val result: Future[Result] = StaticPageController.onPipPageLoad()(request)

        // check
        status(result) shouldBe 200
      }

      "return expected page" in {
        // set up
        val request = FakeRequest(GET, "/paac/changes-to-pip").withSession {(SessionKeys.sessionId,SESSION_ID)}

        // test
        val result: Future[Result] = StaticPageController.onPipPageLoad()(request)

        // check
        val StartPage = contentAsString(await(result))
        StartPage should include ("Period 1 is when your normal pension input period started to 8th July 2015 and period 2 is from 9th July 2015 to 5th April 2016.")
      }
    }

    "PipTaxYearPageLoad" should {
      "return 200" in {
        // set up
        val request = FakeRequest(GET, "/paac/pip-tax-years").withSession {(SessionKeys.sessionId,SESSION_ID)}

        // test
        val result: Future[Result] = StaticPageController.onPipTaxYearPageLoad()(request)

        // check
        status(result) shouldBe 200
      }

      "return expected page" in {
        // set up
        val request = FakeRequest(GET, "/paac/pip-tax-years").withSession {(SessionKeys.sessionId,SESSION_ID)}

        // test
        val result: Future[Result] = StaticPageController.onPipTaxYearPageLoad()(request)

        // check
        val StartPage = contentAsString(await(result))
        StartPage should include ("A tax year is 6 April to 5 April. The tax year that your " +
          "pension input period is for is based on the date your pension input period ends.")
      }
    }

    "onPipTaxYearSubmit" should {
      "return 303" in {
        // set up
        val request = FakeRequest(GET, "/paac").withSession {(SessionKeys.sessionId,SESSION_ID)}

        // test
        val result: Future[Result] = StaticPageController.onPipTaxYearSubmit()(request)

        // check
        status(result) shouldBe 303
      }
    }
  }
}
