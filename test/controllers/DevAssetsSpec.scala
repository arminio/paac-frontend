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
import connector._
import models._
import play.api.libs.json._
import play.api.Play
import play.api.mvc.Result
import play.api.test.Helpers._
import play.api.test.{FakeRequest, FakeApplication}
import uk.gov.hmrc.play.test.UnitSpec
import scala.concurrent.Future


class DevAssetsSpec extends test.BaseSpec {

  "DevAssets" should {
    "at should not serve file asset if not present with absolute path" in {
      // set up
      val action = DevAssets.at(System.getProperty("user.dir"),"app.js")

      // test
      val result: Future[Result] = action()(FakeRequest(GET, "/some/path"))

      //info(await(result))
    }
    
    "at should not serve file asset if not present with non absolute path" in {
      // set up
      val action = DevAssets.at("file://"+System.getProperty("user.dir"),"app.js")

      // test
      val result: Future[Result] = action()(FakeRequest(GET, "/some/path"))

      //info(await(result))
    }
  }
}
