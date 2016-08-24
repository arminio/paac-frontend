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

package metrics

import java.util.concurrent.TimeUnit
import com.codahale.metrics._
import com.kenshoo.play.metrics.MetricsRegistry
import scala.concurrent.{duration, Await}
import scala.concurrent.duration._
import play.api.Logger

trait Metrics {
  def successfulCalculation()
  def failedCalculation()
  def calculationTime(delta: Long, timeUnit: TimeUnit)
  def taxCalculated(taxYear: String, taxAmount: Long)
  def unusedAllowanceCalculated(taxYear: String, taxAmount: Long)
  def keystoreStatusCode(code: Int)
  def calculatorStatusCode(code: Int)
}

trait GraphiteMetrics extends Metrics {
  Logger.info("[Metrics] Registering metrics...")

  private val SUCCESSFUL_CALCULATION = "successful-calculation"
  private val FAILED_CALCULATION = "failed-calculation"
  private val CALCULATION_TIMER = "calculation-timer"
  private val TAX_CALCULATED = "tax-calculation"
  private val UNUSED_ALLOWANCE_CALCULATED = "unused-allowance-calculation"
  private val KEYSTORE_STATUS = "keystore-status-"
  private val PAAC_BACKEND_STATUS = "paac-backend-status-"

  def registry() = MetricsRegistry.defaultRegistry
  private val timer = (name: String) => registry.timer(name)
  private val counter = (name: String) => registry.counter(name)
  private val histogram = (name: String) => registry.histogram(name)
  private val increment = (name: String) => { Logger.debug(s"[Metrics][${name}] ${counter(name).getCount()}"); counter(name).inc() }
  private val log = (name: String, delta: Long, timeUnit: TimeUnit) => { Logger.debug(s"[Metrics][${name}] ${delta} ${timeUnit}"); timer(name).update(delta, timeUnit) }
  private val update = (name: String, value: Long) => { Logger.debug(f"[Metrics][${name}] ${(value / 100.00)}%2.2f "); histogram(name).update(value) }


  Seq((SUCCESSFUL_CALCULATION, counter),
      (FAILED_CALCULATION, counter),
      (CALCULATION_TIMER, timer),
      (TAX_CALCULATED, counter),
      (UNUSED_ALLOWANCE_CALCULATED, counter),
      (s"${PAAC_BACKEND_STATUS}200", counter),
      (s"${PAAC_BACKEND_STATUS}400", counter),
      (s"${KEYSTORE_STATUS}502", counter),
      (s"${KEYSTORE_STATUS}501", counter),
      (s"${KEYSTORE_STATUS}500", counter)
    ) foreach { t => t._2(t._1) }

  override def successfulCalculation(): Unit = increment(SUCCESSFUL_CALCULATION)

  override def failedCalculation(): Unit = increment(FAILED_CALCULATION)

  override def calculationTime(delta: Long, timeUnit: TimeUnit): Unit = log(CALCULATION_TIMER, delta, timeUnit)

  override def taxCalculated(taxYear: String, taxAmount: Long): Unit = increment(TAX_CALCULATED)

  override def unusedAllowanceCalculated(taxYear: String, taxAmount: Long): Unit = increment(UNUSED_ALLOWANCE_CALCULATED)

  override def keystoreStatusCode(code: Int): Unit = increment(s"${KEYSTORE_STATUS}${code}")

  override def calculatorStatusCode(code: Int): Unit = increment(s"${PAAC_BACKEND_STATUS}${code}")

  Logger.info("[Metrics] Completed metrics registration.")
}