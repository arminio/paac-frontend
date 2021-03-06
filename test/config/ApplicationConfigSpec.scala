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

package config

import org.scalatestplus.play.{OneAppPerTest, PlaySpec}
import play.api.mvc.Call
import java.util.Base64
import play.api.test.FakeApplication
import org.scalatest.TestData

class ApplicationConfigSpec extends PlaySpec
  with OneAppPerTest{

  implicit override def newAppForTest(td: TestData): FakeApplication =
    FakeApplication(additionalConfiguration = Map("whitelistExcludedCalls" -> Base64.getEncoder.encodeToString("/ping/pong,/healthcheck".getBytes),
                                                  "whitelist" -> Base64.getEncoder.encodeToString("11.22.33.44".getBytes)))

  "ApplicationConfig" must {
    "return a valid config item" when {
      "the whitelist exclusion paths are requested" in {
        ApplicationConfig.whitelistExcluded mustBe Seq("/ping/pong", "/healthcheck")
      }
      "the whitelist IPs are requested" in {
        ApplicationConfig.whitelist mustBe Seq("11.22.33.44")
      }

    }
  }
}