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

import java.util.Base64

import play.api.Play
import play.api.Play.{configuration, current}
import uk.gov.hmrc.play.config.ServicesConfig

trait AppConfig {
  val assetsPrefix: String
  val analyticsToken: Option[String]
  val analyticsHost: String
  val reportAProblemPartialUrl: String
  val reportAProblemNonJSUrl: String
  val useMinifiedAssets: Boolean
  val betaFeedbackUnauthenticatedUrl: String
  val betaFeedbackUrl: String
  val assetsUrl: String
}

object ApplicationConfig extends AppConfig with ServicesConfig {

  private def stringConfig(key: String) = Play.configuration.getString(key).getOrElse(throw new RuntimeException(s"Missing key: $key"))

  private def loadConfig(key: String) = configuration.getString(key).getOrElse(throw new Exception(s"Missing configuration key: $key"))
  private def loadInteger(key : String) = configuration.getInt(key).getOrElse(throw new Exception(s"Missing key: $key"))

  override lazy val assetsUrl = stringConfig(s"assets.url") + stringConfig(s"assets.version")

  private val contactHost = configuration.getString(s"contact-frontend.host").getOrElse("")
  private val contactFormServiceIdentifier = "PAAC"

  override lazy val assetsPrefix = loadConfig(s"assets.url") + loadConfig(s"assets.version")
  override lazy val analyticsToken: Option[String] = configuration.getString(s"govuk-tax.google-analytics.token")
  override lazy val analyticsHost = loadConfig(s"google-analytics.host")
  override lazy val reportAProblemPartialUrl = s"$contactHost/contact/problem_reports_ajax?service=$contactFormServiceIdentifier"
  override lazy val reportAProblemNonJSUrl = s"$contactHost/contact/problem_reports_nonjs?service=$contactFormServiceIdentifier"

  override lazy val betaFeedbackUrl = s"$contactHost/contact/beta-feedback"

  override lazy val useMinifiedAssets = configuration.getBoolean(s"govuk-tax.cc-frontend.assets.minified").getOrElse(true)
  override lazy val betaFeedbackUnauthenticatedUrl = s"$contactHost/contact/beta-feedback-unauthenticated"

  // Whitelist Configuration
  private def whitelistConfig(key: String):Seq[String] = Option(new String(Base64.getDecoder().decode(Play.configuration.getString(key).getOrElse("")), "UTF-8"))
                                                                            .map(_.split(",")).getOrElse(Array.empty).toSeq

  lazy val whitelist = whitelistConfig("whitelist")
  lazy val whitelistExcluded = whitelistConfig("whitelistExcludedCalls")
}
