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

import play.api.mvc.Results._
import play.api.mvc._
import play.api.test.FakeRequest
import play.api.test.Helpers._
import service.KeystoreService
import uk.gov.hmrc.play.http.{HeaderCarrier, SessionKeys}

import scala.concurrent.Future

class RedirectControllerSpec extends test.BaseSpec {

  trait ControllerWithMockKeystore extends MockKeystoreFixture {
    object RedirectController extends RedirectController {
      override val keystore: KeystoreService = MockKeystore
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

      "redirect to trigger date page if forward is false and edit is true" in new ControllerWithMockKeystore {
        // set up
        implicit val hc = HeaderCarrier()
        implicit val request = FakeRequest().withSession { (SessionKeys.sessionId, "session-test") }

        // test
        val result: Future[Result] = RedirectController.goTo(RedirectController.EDIT_TRIGGER_DATE, false, true, false, Redirect(routes.StartPageController.startPage()))

        // check
        status(result) shouldBe 303
        redirectLocation(result) shouldBe Some("/paac/dateofmpaate")
      }

      "redirect to trigger amount page if forward is false and edit is true" in new ControllerWithMockKeystore {
        // set up
        implicit val hc = HeaderCarrier()
        implicit val request = FakeRequest().withSession { (SessionKeys.sessionId, "session-test") }

        // test
        val result: Future[Result] = RedirectController.goTo(RedirectController.EDIT_TRIGGER_AMOUNT, false, true, false, Redirect(routes.StartPageController.startPage()))

        // check
        status(result) shouldBe 303
        redirectLocation(result) shouldBe Some("/paac/moneyPurchasePostTriggerValue")
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

      "redirect to PensionInputs201516Controller page load if forward is false and edit are true" in new ControllerWithMockKeystore {
        // set up
        implicit val hc = HeaderCarrier()
        implicit val request = FakeRequest().withSession { (SessionKeys.sessionId, "session-test") }

        // test
        val result: Future[Result] = RedirectController.goTo(2015, false, true, false, Redirect(routes.StartPageController.startPage()))

        // check
        status(result) shouldBe 303
        redirectLocation(result) shouldBe Some("/paac/pensionInputs201516")
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

      "redirect to PostTriggerPensionInputsController if forward is false and edit are false, te is true and year is 2014" in new ControllerWithMockKeystore {
        // set up
        implicit val hc = HeaderCarrier()
        implicit val request = FakeRequest().withSession { (SessionKeys.sessionId, "session-test") }
        MockKeystore.map = MockKeystore.map + (KeystoreService.TRIGGER_DATE_KEY -> "2014-06-01")

        // test
        val result: Future[Result] = RedirectController.goTo(2015, false, false, true, Redirect(routes.YesNoMPAATriggerEventAmountController.onPageLoad()))

        // check
        status(result) shouldBe 303
        redirectLocation(result) shouldBe Some("/paac/dateofmpaate")
      }

      "redirect to PostTriggerPensionInputsController if forward is false and edit are false, te is true and year is not defined" in new ControllerWithMockKeystore {
        // set up
        implicit val hc = HeaderCarrier()
        implicit val request = FakeRequest().withSession { (SessionKeys.sessionId, "session-test") }

        // test
        val result: Future[Result] = RedirectController.goTo(2015, false, false, true, Redirect(routes.YesNoMPAATriggerEventAmountController.onPageLoad()))

        // check
        status(result) shouldBe 303
        redirectLocation(result) shouldBe Some("/paac/dateofmpaate")
      }

      "redirect to PostTriggerPensionInputsController if forward is false and edit are false, te is true and year is 2015" in new ControllerWithMockKeystore {
        // set up
        implicit val hc = HeaderCarrier()
        implicit val request = FakeRequest().withSession { (SessionKeys.sessionId, "session-test") }
        MockKeystore.map = MockKeystore.map + (KeystoreService.TRIGGER_DATE_KEY -> "2015-06-01")

        // test
        val result: Future[Result] = RedirectController.goTo(2015, false, false, true, Redirect(routes.YesNoMPAATriggerEventAmountController.onPageLoad()))

        // check
        status(result) shouldBe 303
        redirectLocation(result) shouldBe Some("/paac/moneyPurchasePostTriggerValue")
      }

      "redirect to PostTriggerPensionInputsController if forward is false and edit is true, and year is 0" in new ControllerWithMockKeystore {
        // set up
        implicit val hc = HeaderCarrier()
        implicit val request = FakeRequest().withSession { (SessionKeys.sessionId, "session-test") }

        // test
        val result: Future[Result] = RedirectController.goTo(0, false, true, true, Redirect(routes.YesNoMPAATriggerEventAmountController.onPageLoad()))

        // check
        status(result) shouldBe 303
        redirectLocation(result) shouldBe Some("/paac/moneyPurchasePostTriggerValue")
      }

      "redirect to YesNoMPAATriggerEventAmountController if forward, edit, te are false and year is 2015" in new ControllerWithMockKeystore {
        // set up
        implicit val hc = HeaderCarrier()
        implicit val request = FakeRequest().withSession { (SessionKeys.sessionId, "session-test") }
        MockKeystore.map = MockKeystore.map + (KeystoreService.DC_FLAG -> "true")

        // test
        val result: Future[Result] = RedirectController.goTo(2015, false, false, false, Redirect(routes.YesNoMPAATriggerEventAmountController.onPageLoad()))

        // check
        status(result) shouldBe 303
        redirectLocation(result) shouldBe Some("/paac/yesnompaate")
      }

      "redirect to PensionInputs201516Controller on page load if forward, edit, dc, te are false and year is 2015" in new ControllerWithMockKeystore {
        // set up
        implicit val hc = HeaderCarrier()
        implicit val request = FakeRequest().withSession { (SessionKeys.sessionId, "session-test") }

        // test
        val result: Future[Result] = RedirectController.goTo(2015, false, false, false, Redirect(routes.YesNoMPAATriggerEventAmountController.onPageLoad()))

        // check
        status(result) shouldBe 303
        redirectLocation(result) shouldBe Some("/paac/pensionInputs201516")
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
      "not fail if values not in keystore" in new ControllerWithMockKeystore {
        // set up
        implicit val hc = HeaderCarrier()
        implicit val request = FakeRequest().withSession { (SessionKeys.sessionId, "session-test") }
        val route = Redirect(routes.YesNoMPAATriggerEventAmountController.onPageLoad())

        // test
        val result: Future[Result] = RedirectController.wheretoBack(route)

        // check
        status(result) shouldBe 303
        redirectLocation(result) shouldBe Some("/paac/yesnompaate")
        MockKeystore.map should contain key (KeystoreService.CURRENT_INPUT_YEAR_KEY)
        MockKeystore.map should contain value ("-1")
      }      

      "redirect to default route if empty current year and selected years" in new ControllerWithMockKeystore {
        // set up
        implicit val hc = HeaderCarrier()
        implicit val request = FakeRequest().withSession { (SessionKeys.sessionId, "session-test") }
        val route = Redirect(routes.YesNoMPAATriggerEventAmountController.onPageLoad())
        MockKeystore.map = MockKeystore.map + (KeystoreService.CURRENT_INPUT_YEAR_KEY -> "")
        MockKeystore.map = MockKeystore.map + (KeystoreService.SELECTED_INPUT_YEARS_KEY -> "")
        MockKeystore.map = MockKeystore.map + (KeystoreService.TE_YES_NO_KEY -> "No")

        // test
        val result: Future[Result] = RedirectController.wheretoBack(route)

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
        val result: Future[Result] = RedirectController.wheretoBack(route)
 
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
        val result: Future[Result] = RedirectController.wheretoBack(route)
 
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
        val result: Future[Result] = RedirectController.wheretoBack(route)
 
        // check
        status(result) shouldBe 303
        redirectLocation(result) shouldBe Some("/paac/pensionInputs")
        MockKeystore.map should contain key (KeystoreService.CURRENT_INPUT_YEAR_KEY)
        MockKeystore.map should contain value ("2014")
      }

      "redirect to review page if isEdit == true" in new ControllerWithMockKeystore {
        // set up
        implicit val hc = HeaderCarrier()
        implicit val request = FakeRequest().withSession { (SessionKeys.sessionId, "sesion-test") }
        val route = Redirect(routes.YesNoMPAATriggerEventAmountController.onPageLoad())
        MockKeystore.map = MockKeystore.map + (KeystoreService.IS_EDIT_KEY -> "true")
 
        // test
        val result: Future[Result] = RedirectController.wheretoBack(route)
 
        // check
        status(result) shouldBe 303
        redirectLocation(result) shouldBe Some("/paac/review")
      }

    }

    "wheretoNext" should {
      "not fail when values not in keystore" in new ControllerWithMockKeystore {
        // set up
        implicit val hc = HeaderCarrier()
        implicit val request = FakeRequest().withSession { (SessionKeys.sessionId, "session-test") }
        val route = Redirect(routes.YesNoMPAATriggerEventAmountController.onPageLoad())

        // test
        val result: Future[Result] = RedirectController.wheretoNext(route)

        // check
        status(result) shouldBe 303
        redirectLocation(result) shouldBe Some("/paac/yesnompaate")
        MockKeystore.map should contain key (KeystoreService.CURRENT_INPUT_YEAR_KEY)
        MockKeystore.map should contain value ("-1")
      }

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
            val result: Future[Result] = RedirectController.wheretoNext(route)
  
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
            val result: Future[Result] = RedirectController.wheretoNext(route)
  
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
            val result: Future[Result] = RedirectController.wheretoNext(route)
    
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
            val result: Future[Result] = RedirectController.wheretoNext(route)
    
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
            val result: Future[Result] = RedirectController.wheretoNext(route)
    
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
            val result: Future[Result] = RedirectController.wheretoNext(route)
  
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
            val result: Future[Result] = RedirectController.wheretoNext(route)
    
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
            val result: Future[Result] = RedirectController.wheretoNext(route)
    
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
            val result: Future[Result] = RedirectController.wheretoNext(route)
    
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
