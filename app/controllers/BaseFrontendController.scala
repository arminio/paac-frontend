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
import scala.concurrent.Future
import play.api.mvc._

import service.KeystoreService
import service.KeystoreService._
import uk.gov.hmrc.play.frontend.controller.FrontendController
import uk.gov.hmrc.play.http.{HeaderCarrier, SessionKeys}


trait SessionProvider {
  val NOSESSION = "NOSESSION"
  def createSessionId()(implicit request: Request[AnyContent]) : (String, String) = SessionKeys.sessionId -> s"session-${UUID.randomUUID}"

  def createKeystoreSession()(implicit request: Request[AnyContent]) : Session = {
    Session(request.session.data + createSessionId())
  }
}

trait RedirectController extends BaseFrontendController {
  def keystore: KeystoreService

  def goTo(year: Int, isForward: Boolean, isEdit: Boolean, isTE: Boolean, defaultRoute: Result)
          (implicit hc: HeaderCarrier, format: play.api.libs.json.Format[String], request: Request[Any]): Future[Result] = {
    keystore.store(year.toString(), KeystoreService.CURRENT_INPUT_YEAR_KEY).flatMap {
      (values) =>
      //redirect to nextYear Controller
      if (isForward && isEdit) {
        Future.successful(Results.Redirect(routes.ReviewTotalAmountsController.onPageLoad()))
      } else if (isEdit) {
        if (year == 20151) {
          Future.successful(Results.Redirect(routes.PensionInputs1516Period1Controller.onPageLoad()))
        } else if (year == 20152) {
          Future.successful(Results.Redirect(routes.PensionInputs1516Period2Controller.onPageLoad()))
        } else {
          Future.successful(Redirect(routes.PensionInputsController.onPageLoad()))
        }
      } else if (year < 0) {
        Future.successful(defaultRoute)
      } else if (year > 2015) {
        Future.successful(Redirect(routes.StartPageController.startPage()))//2016
      } else if (year == 2015) {
        if (isForward) {
          Future.successful(Redirect(routes.StaticPageController.onPipPageLoad()))
        } else {
          if (isTE) {
            keystore.read[String](KeystoreService.TRIGGER_DATE_KEY).flatMap {
              (dateAsStr)=>
              if (dateAsStr.isDefined) {
                val parts = dateAsStr.getOrElse("2000-01-01").split("-").map(_.toInt)
                if (parts(0) > 2016 || (parts(0) == 2016 && parts(1) > 4) || (parts(0) == 2016 && parts(1) == 4 && parts(2) > 5) ||
                    parts(0) < 2015 || (parts(0) == 2015 && parts(1) < 4) || (parts(0) == 2015 && parts(1) == 4 && parts(2) < 5)) {
                  Future.successful(Redirect(routes.DateOfMPAATriggerEventController.onPageLoad()))
                } else {
                  Future.successful(Redirect(routes.PostTriggerPensionInputsController.onPageLoad()))
                }
              } else {
                Future.successful(Redirect(routes.DateOfMPAATriggerEventController.onPageLoad()))
              }
            }
          } else {
            Future.successful(Redirect(routes.YesNoMPAATriggerEventAmountController.onPageLoad()))
          }
        }
      } else {
        Future.successful(Redirect(routes.PensionInputsController.onPageLoad()))
      }
    }
  }

  def wheretoBack(defaultRoute: Result)(implicit hc: HeaderCarrier, format: play.api.libs.json.Format[String], request: Request[Any]) : Future[Result] = {
    implicit val marshall = KeystoreService.toStringPair _

    def previous(currentYear: String, selectedYears: String): Int = {
      if (currentYear == "" || selectedYears == "") {
        -1
      } else {
        val syears = selectedYears.split(",")
        val cy = currentYear.toInt
        if (cy == -1) {
          syears.reverse(0).toInt
        } else {
          val i = syears.indexOf(currentYear) - 1
          if (i < 0) {
            -2
          } else {
            syears(i).toInt
          }
        }
      }
    }

    keystore.read(List(CURRENT_INPUT_YEAR_KEY, SELECTED_INPUT_YEARS_KEY, TE_YES_NO_KEY)).flatMap {
      (fieldsMap) =>
      val currentYear = fieldsMap(CURRENT_INPUT_YEAR_KEY)
      val selectedYears = fieldsMap(SELECTED_INPUT_YEARS_KEY)
      val previousYear = previous(currentYear, selectedYears)
      goTo(previousYear, false, false, fieldsMap(TE_YES_NO_KEY) == "Yes", defaultRoute)
    }
  }

  def wheretoNext(defaultRoute: Result)(implicit hc: HeaderCarrier, format: play.api.libs.json.Format[String], request: Request[Any]) : Future[Result] = {
    implicit val marshall = KeystoreService.toStringPair _

    def next(currentYear: String, selectedYears: String): Int = {
      if (selectedYears == "" && currentYear == "") {
        -1
      } else {
        val syears = selectedYears.split(",")
        if (currentYear == "") {
          syears(0).toInt
        } else {
          val i = syears.indexOf(currentYear) + 1
          if (i < syears.length) {
            syears(i).toInt
          } else {
            -1
          }
        }
      }
    }

    keystore.read(List(CURRENT_INPUT_YEAR_KEY, SELECTED_INPUT_YEARS_KEY,IS_EDIT_KEY,TE_YES_NO_KEY)).flatMap {
      (fieldsMap) =>
      val currentYear = fieldsMap(KeystoreService.CURRENT_INPUT_YEAR_KEY)
      val selectedYears = fieldsMap(KeystoreService.SELECTED_INPUT_YEARS_KEY)
      val nextYear = next(currentYear, selectedYears)
      goTo(nextYear, true, fieldsMap(IS_EDIT_KEY).toBoolean, fieldsMap(TE_YES_NO_KEY) == "Yes", defaultRoute)
    }
  }
}

trait BaseFrontendController extends SessionProvider with FrontendController {
  this: SessionProvider =>

  val keys = List(KeystoreService.SCHEME_TYPE_KEY, 
                  KeystoreService.DB_FLAG, 
                  KeystoreService.DC_FLAG, 
                  KeystoreService.TRIGGER_DATE_KEY,
                  KeystoreService.CURRENT_INPUT_YEAR_KEY,
                  KeystoreService.SELECTED_INPUT_YEARS_KEY,
                  KeystoreService.P1_YES_NO_KEY,
                  KeystoreService.P2_YES_NO_KEY,
                  KeystoreService.TE_YES_NO_KEY,
                  KeystoreService.IS_EDIT_KEY
                  )
  implicit val marshall = {
    (key: String, value: Option[String]) =>
      if (keys.contains(key)) {
        value match {
          case None => (key, "")
          case Some(v) => (key, v)
        }
      } else {
        value match {
          case None => (key, "")
          case Some("0") => (key, "0.00")
          case Some(v) => (key, f"${(v.toInt / 100.00)}%2.2f")
        }
      }
  }
  
  def getSessionId()(implicit request : Request[AnyContent]) : Option[String] = request.session.get(SessionKeys.sessionId)

  /**
   * every session should have an ID: required by key-store
   * If no session Id is found or session was deleted (NOSESSION), a new session id will be issued
    *
    * @return redirect to start page if no session else action
   * 
   * Example usage:
   * def onPageLoad(...) = withSession { implicit request => etc. }
   */
  def withSession(f: => Request[AnyContent]=> Future[Result]) : Action[AnyContent] = Action.async {
    implicit request : Request[AnyContent] =>
      getSessionId match {
        case Some(NOSESSION) => Future.successful(Redirect(routes.StartPageController.startPage()).withNewSession.withSession(createSessionId()))
        case None => Future.successful(Redirect(routes.StartPageController.startPage()).withNewSession.withSession(createSessionId()))
        case _ => f(request)
      }
  }
}
