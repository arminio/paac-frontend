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
import play.api.test.Helpers._
import play.api.test.{FakeRequest}
import play.api.Play
import play.api.test._
import play.api.mvc._
import play.api.mvc.Results._
import service.KeystoreService

import uk.gov.hmrc.play.http.{HeaderCarrier, HttpPost, HttpResponse}
import uk.gov.hmrc.play.http.SessionKeys
import uk.gov.hmrc.play.test.UnitSpec
import scala.concurrent.Future

class RedirectControllerSpec extends UnitSpec with BeforeAndAfterAll {
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
    object RedirectController extends RedirectController {
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


  "RedirectController" should {
    "on goTo" should {
      "save year redirected to as current input year" in new ControllerWithMockKeystore {
        // set up
        implicit val hc = HeaderCarrier()
        implicit val request = FakeRequest().withSession { (SessionKeys.sessionId, "session-test") }

        // test
        val result: Future[Result] = RedirectController.goTo(99, true, true, false, Redirect(routes.StartPageController.startPage()))

        // check
        MockKeystore.map should contain key (KeystoreService.CURRENT_INPUT_YEAR_KEY)
        MockKeystore.map should contain value ("99")
      }

      "redirect to review page load if forward and edit are true" in new ControllerWithMockKeystore {
        // set up
        implicit val hc = HeaderCarrier()
        implicit val request = FakeRequest().withSession { (SessionKeys.sessionId, "session-test") }

        // test
        val result: Future[Result] = RedirectController.goTo(99, true, true, false, Redirect(routes.StartPageController.startPage()))

        // check
        status(result) shouldBe 303
        redirectLocation(result) shouldBe Some("/paac/review")
      }

      "redirect to PensionInputs1516Period1Controller page load if forward is false and edit are true" in new ControllerWithMockKeystore {
        // set up
        implicit val hc = HeaderCarrier()
        implicit val request = FakeRequest().withSession { (SessionKeys.sessionId, "session-test") }

        // test
        val result: Future[Result] = RedirectController.goTo(20151, false, true, false, Redirect(routes.StartPageController.startPage()))

        // check
        status(result) shouldBe 303
        redirectLocation(result) shouldBe Some("/paac/pensionInputs1516p1")
      }

      "redirect to PensionInputs1516Period2Controller page load if forward is false and edit are true" in new ControllerWithMockKeystore {
        // set up
        implicit val hc = HeaderCarrier()
        implicit val request = FakeRequest().withSession { (SessionKeys.sessionId, "session-test") }

        // test
        val result: Future[Result] = RedirectController.goTo(20152, false, true, false, Redirect(routes.StartPageController.startPage()))

        // check
        status(result) shouldBe 303
        redirectLocation(result) shouldBe Some("/paac/pensionInputs1516p2")
      }

      "redirect to PensionInputsController page load if forward is false and edit are true" in new ControllerWithMockKeystore {
        // set up
        implicit val hc = HeaderCarrier()
        implicit val request = FakeRequest().withSession { (SessionKeys.sessionId, "session-test") }

        // test
        val result: Future[Result] = RedirectController.goTo(2014, false, true, false, Redirect(routes.StartPageController.startPage()))

        // check
        status(result) shouldBe 303
        redirectLocation(result) shouldBe Some("/paac/pensionInputs")
      }

      "redirect to given route if forward is false and edit are false and year is -1" in new ControllerWithMockKeystore {
        // set up
        implicit val hc = HeaderCarrier()
        implicit val request = FakeRequest().withSession { (SessionKeys.sessionId, "session-test") }

        // test
        val result: Future[Result] = RedirectController.goTo(-1, false, false, false, Redirect(routes.YesNoMPAATriggerEventAmountController.onPageLoad()))

        // check
        status(result) shouldBe 303
        redirectLocation(result) shouldBe Some("/paac/yesnompaate")
      }

      "redirect to start if forward is false and edit are false and year is 2016" in new ControllerWithMockKeystore {
        // set up
        implicit val hc = HeaderCarrier()
        implicit val request = FakeRequest().withSession { (SessionKeys.sessionId, "session-test") }

        // test
        val result: Future[Result] = RedirectController.goTo(2016, false, false, false, Redirect(routes.YesNoMPAATriggerEventAmountController.onPageLoad()))

        // check
        status(result) shouldBe 303
        redirectLocation(result) shouldBe Some("/paac")
      }

      "redirect to StaticPageController changes-to-pip if forward is true and edit are false and year is 2015" in new ControllerWithMockKeystore {
        // set up
        implicit val hc = HeaderCarrier()
        implicit val request = FakeRequest().withSession { (SessionKeys.sessionId, "session-test") }

        // test
        val result: Future[Result] = RedirectController.goTo(2015, true, false, false, Redirect(routes.YesNoMPAATriggerEventAmountController.onPageLoad()))

        // check
        status(result) shouldBe 303
        redirectLocation(result) shouldBe Some("/paac/changes-to-pip")
      }

      "redirect to PostTriggerPensionInputsController if forward is false and edit are false, te is true and year is 2015" in new ControllerWithMockKeystore {
        // set up
        implicit val hc = HeaderCarrier()
        implicit val request = FakeRequest().withSession { (SessionKeys.sessionId, "session-test") }

        // test
        val result: Future[Result] = RedirectController.goTo(2015, false, false, true, Redirect(routes.YesNoMPAATriggerEventAmountController.onPageLoad()))

        // check
        status(result) shouldBe 303
        redirectLocation(result) shouldBe Some("/paac/moneyPurchasePostTriggerValue")
      }

      "redirect to YesNoMPAATriggerEventAmountController if forward, edit, te are false and year is 2015" in new ControllerWithMockKeystore {
        // set up
        implicit val hc = HeaderCarrier()
        implicit val request = FakeRequest().withSession { (SessionKeys.sessionId, "session-test") }

        // test
        val result: Future[Result] = RedirectController.goTo(2015, false, false, false, Redirect(routes.YesNoMPAATriggerEventAmountController.onPageLoad()))

        // check
        status(result) shouldBe 303
        redirectLocation(result) shouldBe Some("/paac/yesnompaate")
      }

      "redirect to PensionInputsController if year is 2013" in new ControllerWithMockKeystore {
        // set up
        implicit val hc = HeaderCarrier()
        implicit val request = FakeRequest().withSession { (SessionKeys.sessionId, "session-test") }

        // test
        val result: Future[Result] = RedirectController.goTo(2013, false, false, false, Redirect(routes.YesNoMPAATriggerEventAmountController.onPageLoad()))

        // check
        status(result) shouldBe 303
        redirectLocation(result) shouldBe Some("/paac/pensionInputs")
      }
    }

    "wheretoBack" should {
      "redirect to default route if empty current year and selected years" in new ControllerWithMockKeystore {
        // set up
        implicit val hc = HeaderCarrier()
        implicit val request = FakeRequest().withSession { (SessionKeys.sessionId, "session-test") }
        val route = Redirect(routes.YesNoMPAATriggerEventAmountController.onPageLoad())
        MockKeystore.map = MockKeystore.map + (KeystoreService.CURRENT_INPUT_YEAR_KEY -> "")
        MockKeystore.map = MockKeystore.map + (KeystoreService.SELECTED_INPUT_YEARS_KEY -> "")
        MockKeystore.map = MockKeystore.map + (KeystoreService.TE_YES_NO_KEY -> "No")

        // test
        val result: Future[Result] = RedirectController.wheretoBack[String](route)

        // check
        status(result) shouldBe 303
        redirectLocation(result) shouldBe Some("/paac/yesnompaate")
        MockKeystore.map should contain key (KeystoreService.CURRENT_INPUT_YEAR_KEY)
        MockKeystore.map should contain value ("-1")
      }

      "redirect to previous year if current year is in list of selected years" in new ControllerWithMockKeystore {
        // set up
        implicit val hc = HeaderCarrier()
        implicit val request = FakeRequest().withSession { (SessionKeys.sessionId, "sesion-test") }
        val route = Redirect(routes.YesNoMPAATriggerEventAmountController.onPageLoad())
        MockKeystore.map = MockKeystore.map + (KeystoreService.CURRENT_INPUT_YEAR_KEY -> "2014")
        MockKeystore.map = MockKeystore.map + (KeystoreService.SELECTED_INPUT_YEARS_KEY -> "2012,2013,2014")
        MockKeystore.map = MockKeystore.map + (KeystoreService.TE_YES_NO_KEY -> "No")
 
        // test
        val result: Future[Result] = RedirectController.wheretoBack[String](route)
 
        // check
        status(result) shouldBe 303
        redirectLocation(result) shouldBe Some("/paac/pensionInputs")
        MockKeystore.map should contain key (KeystoreService.CURRENT_INPUT_YEAR_KEY)
        MockKeystore.map should contain value ("2013")
      }

      "redirect to default route if current year is in list of selected years" in new ControllerWithMockKeystore {
        // set up
        implicit val hc = HeaderCarrier()
        implicit val request = FakeRequest().withSession { (SessionKeys.sessionId, "sesion-test") }
        val route = Redirect(routes.YesNoMPAATriggerEventAmountController.onPageLoad())
        MockKeystore.map = MockKeystore.map + (KeystoreService.CURRENT_INPUT_YEAR_KEY -> "2012")
        MockKeystore.map = MockKeystore.map + (KeystoreService.SELECTED_INPUT_YEARS_KEY -> "2012,2013,2014")
        MockKeystore.map = MockKeystore.map + (KeystoreService.TE_YES_NO_KEY -> "No")
 
        // test
        val result: Future[Result] = RedirectController.wheretoBack[String](route)
 
        // check
        status(result) shouldBe 303
        redirectLocation(result) shouldBe Some("/paac/yesnompaate")
        MockKeystore.map should contain key (KeystoreService.CURRENT_INPUT_YEAR_KEY)
        MockKeystore.map should contain value ("-2")
      }

      "redirect to last of selected years if current year is -1" in new ControllerWithMockKeystore {
        // set up
        implicit val hc = HeaderCarrier()
        implicit val request = FakeRequest().withSession { (SessionKeys.sessionId, "sesion-test") }
        val route = Redirect(routes.YesNoMPAATriggerEventAmountController.onPageLoad())
        MockKeystore.map = MockKeystore.map + (KeystoreService.CURRENT_INPUT_YEAR_KEY -> "-1")
        MockKeystore.map = MockKeystore.map + (KeystoreService.SELECTED_INPUT_YEARS_KEY -> "2012,2013,2014")
        MockKeystore.map = MockKeystore.map + (KeystoreService.TE_YES_NO_KEY -> "No")
 
        // test
        val result: Future[Result] = RedirectController.wheretoBack[String](route)
 
        // check
        status(result) shouldBe 303
        redirectLocation(result) shouldBe Some("/paac/pensionInputs")
        MockKeystore.map should contain key (KeystoreService.CURRENT_INPUT_YEAR_KEY)
        MockKeystore.map should contain value ("2014")
      }

    }

    "wheretoNext" should {
      "when isEdit is false" should {
          "redirect to default route if empty current year and selected years" in new ControllerWithMockKeystore {
            // set up
            implicit val hc = HeaderCarrier()
            implicit val request = FakeRequest().withSession { (SessionKeys.sessionId, "session-test") }
            val route = Redirect(routes.YesNoMPAATriggerEventAmountController.onPageLoad())
            MockKeystore.map = MockKeystore.map + (KeystoreService.CURRENT_INPUT_YEAR_KEY -> "")
            MockKeystore.map = MockKeystore.map + (KeystoreService.SELECTED_INPUT_YEARS_KEY -> "")
            MockKeystore.map = MockKeystore.map + (KeystoreService.TE_YES_NO_KEY -> "No")
            MockKeystore.map = MockKeystore.map + (KeystoreService.IS_EDIT_KEY -> "false")
  
            // test
            val result: Future[Result] = RedirectController.wheretoNext[String](route)
  
            // check
            status(result) shouldBe 303
            redirectLocation(result) shouldBe Some("/paac/yesnompaate")
            MockKeystore.map should contain key (KeystoreService.CURRENT_INPUT_YEAR_KEY)
            MockKeystore.map should contain value ("-1")
          }

          "redirect to pension inputs if empty current year and non-empty selected years" in new ControllerWithMockKeystore {
            // set up
            implicit val hc = HeaderCarrier()
            implicit val request = FakeRequest().withSession { (SessionKeys.sessionId, "session-test") }
            val route = Redirect(routes.YesNoMPAATriggerEventAmountController.onPageLoad())
            MockKeystore.map = MockKeystore.map + (KeystoreService.CURRENT_INPUT_YEAR_KEY -> "")
            MockKeystore.map = MockKeystore.map + (KeystoreService.SELECTED_INPUT_YEARS_KEY -> "2012,2013")
            MockKeystore.map = MockKeystore.map + (KeystoreService.TE_YES_NO_KEY -> "No")
            MockKeystore.map = MockKeystore.map + (KeystoreService.IS_EDIT_KEY -> "false")
  
            // test
            val result: Future[Result] = RedirectController.wheretoNext[String](route)
  
            // check
            status(result) shouldBe 303
            redirectLocation(result) shouldBe Some("/paac/pensionInputs")
            MockKeystore.map should contain key (KeystoreService.CURRENT_INPUT_YEAR_KEY)
            MockKeystore.map should contain value ("2012")
          }
  
          "redirect to default route if current year is last in list of selected years" in new ControllerWithMockKeystore {
            // set up
            implicit val hc = HeaderCarrier()
            implicit val request = FakeRequest().withSession { (SessionKeys.sessionId, "sesion-test") }
            val route = Redirect(routes.YesNoMPAATriggerEventAmountController.onPageLoad())
            MockKeystore.map = MockKeystore.map + (KeystoreService.CURRENT_INPUT_YEAR_KEY -> "2014")
            MockKeystore.map = MockKeystore.map + (KeystoreService.SELECTED_INPUT_YEARS_KEY -> "2012,2013,2014")
            MockKeystore.map = MockKeystore.map + (KeystoreService.TE_YES_NO_KEY -> "No")
            MockKeystore.map = MockKeystore.map + (KeystoreService.IS_EDIT_KEY -> "false")
  
            // test
            val result: Future[Result] = RedirectController.wheretoNext[String](route)
    
             // check
             status(result) shouldBe 303
             redirectLocation(result) shouldBe Some("/paac/yesnompaate")
             MockKeystore.map should contain key (KeystoreService.CURRENT_INPUT_YEAR_KEY)
             MockKeystore.map should contain value ("-1")
           }
  
          "redirect to next year if current year is in list of selected years and not last" in new ControllerWithMockKeystore {
            // set up
            implicit val hc = HeaderCarrier()
            implicit val request = FakeRequest().withSession { (SessionKeys.sessionId, "sesion-test") }
            val route = Redirect(routes.YesNoMPAATriggerEventAmountController.onPageLoad())
            MockKeystore.map = MockKeystore.map + (KeystoreService.CURRENT_INPUT_YEAR_KEY -> "2012")
            MockKeystore.map = MockKeystore.map + (KeystoreService.SELECTED_INPUT_YEARS_KEY -> "2012,2013,2014")
            MockKeystore.map = MockKeystore.map + (KeystoreService.TE_YES_NO_KEY -> "No")
            MockKeystore.map = MockKeystore.map + (KeystoreService.IS_EDIT_KEY -> "false")
  
            // test
            val result: Future[Result] = RedirectController.wheretoNext[String](route)
    
             // check
             status(result) shouldBe 303
             redirectLocation(result) shouldBe Some("/paac/pensionInputs")
             MockKeystore.map should contain key (KeystoreService.CURRENT_INPUT_YEAR_KEY)
             MockKeystore.map should contain value ("2013")
           }
  
          "redirect to first of selected years if current year is -1" in new ControllerWithMockKeystore {
            // set up
            implicit val hc = HeaderCarrier()
            implicit val request = FakeRequest().withSession { (SessionKeys.sessionId, "sesion-test") }
            val route = Redirect(routes.YesNoMPAATriggerEventAmountController.onPageLoad())
            MockKeystore.map = MockKeystore.map + (KeystoreService.CURRENT_INPUT_YEAR_KEY -> "-1")
            MockKeystore.map = MockKeystore.map + (KeystoreService.SELECTED_INPUT_YEARS_KEY -> "2012,2013,2014")
            MockKeystore.map = MockKeystore.map + (KeystoreService.TE_YES_NO_KEY -> "No")
            MockKeystore.map = MockKeystore.map + (KeystoreService.IS_EDIT_KEY -> "false")
  
            // test
            val result: Future[Result] = RedirectController.wheretoNext[String](route)
    
             // check
             status(result) shouldBe 303
             redirectLocation(result) shouldBe Some("/paac/pensionInputs")
             MockKeystore.map should contain key (KeystoreService.CURRENT_INPUT_YEAR_KEY)
             MockKeystore.map should contain value ("2012")
        }
      }

      "when isEdit is true" should {
        "redirect to review if empty current year and selected years" in new ControllerWithMockKeystore {
          // set up
          implicit val hc = HeaderCarrier()
          implicit val request = FakeRequest().withSession { (SessionKeys.sessionId, "session-test") }
          val route = Redirect(routes.YesNoMPAATriggerEventAmountController.onPageLoad())
          MockKeystore.map = MockKeystore.map + (KeystoreService.CURRENT_INPUT_YEAR_KEY -> "")
          MockKeystore.map = MockKeystore.map + (KeystoreService.SELECTED_INPUT_YEARS_KEY -> "")
          MockKeystore.map = MockKeystore.map + (KeystoreService.TE_YES_NO_KEY -> "No")
          MockKeystore.map = MockKeystore.map + (KeystoreService.IS_EDIT_KEY -> "true")
  
            // test
            val result: Future[Result] = RedirectController.wheretoNext[String](route)
  
            // check
            status(result) shouldBe 303
            redirectLocation(result) shouldBe Some("/paac/review")
            MockKeystore.map should contain key (KeystoreService.CURRENT_INPUT_YEAR_KEY)
            MockKeystore.map should contain value ("-1")
          }
  
          "redirect to review if current year is last in list of selected years" in new ControllerWithMockKeystore {
            // set up
            implicit val hc = HeaderCarrier()
            implicit val request = FakeRequest().withSession { (SessionKeys.sessionId, "sesion-test") }
            val route = Redirect(routes.YesNoMPAATriggerEventAmountController.onPageLoad())
            MockKeystore.map = MockKeystore.map + (KeystoreService.CURRENT_INPUT_YEAR_KEY -> "2014")
            MockKeystore.map = MockKeystore.map + (KeystoreService.SELECTED_INPUT_YEARS_KEY -> "2012,2013,2014")
            MockKeystore.map = MockKeystore.map + (KeystoreService.TE_YES_NO_KEY -> "No")
            MockKeystore.map = MockKeystore.map + (KeystoreService.IS_EDIT_KEY -> "true")
  
            // test
            val result: Future[Result] = RedirectController.wheretoNext[String](route)
    
             // check
             status(result) shouldBe 303
             redirectLocation(result) shouldBe Some("/paac/review")
             MockKeystore.map should contain key (KeystoreService.CURRENT_INPUT_YEAR_KEY)
             MockKeystore.map should contain value ("-1")
           }
  
          "redirect to review if current year is in list of selected years and not last" in new ControllerWithMockKeystore {
            // set up
            implicit val hc = HeaderCarrier()
            implicit val request = FakeRequest().withSession { (SessionKeys.sessionId, "sesion-test") }
            val route = Redirect(routes.YesNoMPAATriggerEventAmountController.onPageLoad())
            MockKeystore.map = MockKeystore.map + (KeystoreService.CURRENT_INPUT_YEAR_KEY -> "2012")
            MockKeystore.map = MockKeystore.map + (KeystoreService.SELECTED_INPUT_YEARS_KEY -> "2012,2013,2014")
            MockKeystore.map = MockKeystore.map + (KeystoreService.TE_YES_NO_KEY -> "No")
            MockKeystore.map = MockKeystore.map + (KeystoreService.IS_EDIT_KEY -> "true")
  
            // test
            val result: Future[Result] = RedirectController.wheretoNext[String](route)
    
             // check
             status(result) shouldBe 303
             redirectLocation(result) shouldBe Some("/paac/review")
             MockKeystore.map should contain key (KeystoreService.CURRENT_INPUT_YEAR_KEY)
             MockKeystore.map should contain value ("2013")
           }
  
          "redirect to review if current year is -1" in new ControllerWithMockKeystore {
            // set up
            implicit val hc = HeaderCarrier()
            implicit val request = FakeRequest().withSession { (SessionKeys.sessionId, "sesion-test") }
            val route = Redirect(routes.YesNoMPAATriggerEventAmountController.onPageLoad())
            MockKeystore.map = MockKeystore.map + (KeystoreService.CURRENT_INPUT_YEAR_KEY -> "-1")
            MockKeystore.map = MockKeystore.map + (KeystoreService.SELECTED_INPUT_YEARS_KEY -> "2012,2013,2014")
            MockKeystore.map = MockKeystore.map + (KeystoreService.TE_YES_NO_KEY -> "No")
            MockKeystore.map = MockKeystore.map + (KeystoreService.IS_EDIT_KEY -> "true")
  
            // test
            val result: Future[Result] = RedirectController.wheretoNext[String](route)
    
             // check
             status(result) shouldBe 303
             redirectLocation(result) shouldBe Some("/paac/review")
             MockKeystore.map should contain key (KeystoreService.CURRENT_INPUT_YEAR_KEY)
             MockKeystore.map should contain value ("2012")
        }
      }
    }
  }
}
