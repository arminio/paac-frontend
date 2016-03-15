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

package connector

import config.WSHttp
import models._
import play.api.libs.json.{JsValue, Json}
import uk.gov.hmrc.play.config.ServicesConfig
import uk.gov.hmrc.play.http.{HeaderCarrier, HttpPost, HttpResponse}
import scala.concurrent.Future
import play.api.libs.concurrent.Execution.Implicits._

object CalculatorConnector extends CalculatorConnector with ServicesConfig {
  override def httpPostRequest = WSHttp
  override val serviceUrl = baseUrl("paac")
}

trait CalculatorConnector {
  def httpPostRequest: HttpPost
  def serviceUrl: String

  def connectToGetPersonDetails()(implicit hc: HeaderCarrier): Future[String] = {
    val jsonRequest = Json.toJson(Seq(Contribution(TaxPeriod(2015, 2, 30), TaxPeriod(2015, 10,20), InputAmounts(10000L,10000L))))
    httpPostRequest.POST[JsValue, HttpResponse](s"$serviceUrl/paac/calculate", jsonRequest).map {
      _.body
    }
  }
}
