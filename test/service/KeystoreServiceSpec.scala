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

package service

import test.BaseSpec
import play.api.Play
import play.api.mvc._
import play.api.test.Helpers._
import play.api.test.{FakeRequest, FakeApplication}
import scala.concurrent.Future
import service._
import org.scalatest.mock._
import org.mockito.Mockito._
import org.scalatest.mock.MockitoSugar
import uk.gov.hmrc.http.cache.client.SessionCache
import uk.gov.hmrc.play.http.{HeaderCarrier, SessionKeys}
import play.api.libs.json._
import org.scalatest._
import uk.gov.hmrc.play.test.UnitSpec
import uk.gov.hmrc.http.cache.client._
import uk.gov.hmrc.play.http.HttpResponse

class KeystoreServiceSpec extends UnitSpec with MockitoSugar with BeforeAndAfterAll {
  val app = FakeApplication()
  override def beforeAll() {
    Play.start(app)
    super.beforeAll() // To be stackable, must call super.beforeEach
  }

  override def afterAll() {
    try {
      super.afterAll()
    } finally Play.stop()
  }

  trait SharedFixture {
    val sessionId = "session-test-id"
    val mockSessionCache = mock[SessionCache]
    implicit val hc: HeaderCarrier = HeaderCarrier()
    object MockedKeystoreService extends KeystoreService {
      override val sessionCache: SessionCache = mockSessionCache
    }
  }

  "readData" must {
    "return keystore data as map[string,string]" in new SharedFixture {
      // set up
      implicit val request = FakeRequest(GET, "/abc/def").withSession(SessionKeys.sessionId->sessionId)
      val json = Json.toJson(Map[String,String]("msg"->"hello world!"))
      stub(mockSessionCache.fetchAndGetEntry[JsValue]("paac-frontend", sessionId, "calculatorForm")).
           toReturn(Future.successful(Some(json)))

      // test
      val futureResult = MockedKeystoreService.readData()
      val result = await[Map[String,String]](futureResult)

      // check
      result("msg") shouldBe ("hello world!")
    }

    "return empty map when no session id is present in the session" in new SharedFixture {
      // set up
      implicit val request = FakeRequest(GET, "/abc/def")

      // test
      val futureResult = MockedKeystoreService.readData()
      val result = await[Map[String,String]](futureResult)

      // check
      result.size shouldBe 0
    }

    "return empty map when call failed" in new SharedFixture {
      // set up
      implicit val request = FakeRequest(GET, "/abc/def").withSession(SessionKeys.sessionId->sessionId)
      val json = Json.toJson(Map[String,String]("msg"->"hello world!"))
      stub(mockSessionCache.fetchAndGetEntry[JsValue]("paac-frontend", sessionId, "calculatorForm")).
           toReturn(Future.successful(None))

      // test
      val futureResult = MockedKeystoreService.readData()
      val result = await[Map[String,String]](futureResult)

      // check
      result.size shouldBe 0
    }
  }

  "saveData" must {
    "save to keystore cache supplied data" in new SharedFixture {
      // set up
      implicit val request = FakeRequest(GET, "/abc/def").withSession(SessionKeys.sessionId->sessionId)
      val data = Map[String,String]("msg"->"hello world!")
      val json = Json.toJson(data)
      val cacheMap = CacheMap(sessionId, Map[String,JsValue]("calculatorForm"->json))
      stub(mockSessionCache.cache[JsValue]("paac-frontend", sessionId, "calculatorForm", json)).
           toReturn(Future.successful(cacheMap))

      // test
      val futureResult = MockedKeystoreService.saveData(data)
      val result = await[Option[Map[String,String]]](futureResult)

      // check
      result.isDefined shouldBe true
      result.get.getOrElse("msg", "") shouldBe "hello world!"
    }

    "return none when no session id is present in the session" in new SharedFixture {
      // set up
      implicit val request = FakeRequest(GET, "/abc/def")

      // test
      val futureResult = MockedKeystoreService.saveData(Map[String,String]())
      val result = await[Option[Map[String,String]]](futureResult)

      // check
      result shouldBe None
    }

    "return none when no data previously saved" in new SharedFixture {
      // set up
      implicit val request = FakeRequest(GET, "/abc/def").withSession(SessionKeys.sessionId->sessionId)
      val data = Map[String,String]("msg"->"hello world!")
      val json = Json.toJson(data)
      val cacheMap = CacheMap(sessionId, Map[String,JsValue]("abc"->json))
      stub(mockSessionCache.cache[JsValue]("paac-frontend", sessionId, "calculatorForm", json)).
           toReturn(Future.successful(cacheMap))

      // test
      val futureResult = MockedKeystoreService.saveData(data)
      val result = await[Option[Map[String,String]]](futureResult)

      // check
      result shouldBe None
    }
  }

  "clear" must {
    "delete data when session id present in session" in new SharedFixture {
      // set up
      implicit val request = FakeRequest(GET, "/abc/def").withSession(SessionKeys.sessionId->sessionId)
      val uri = "http://non-existent-mocked-session-cache/the-domain/the-source-of-all-knowledge/session-test-id"
      stub(mockSessionCache.defaultSource).toReturn("the-source-of-all-knowledge")
      stub(mockSessionCache.baseUri).toReturn("http://non-existent-mocked-session-cache")
      stub(mockSessionCache.domain).toReturn("the-domain")
      stub(mockSessionCache.delete(uri)).toReturn(Future.successful(HttpResponse(200)))

      // test
      val futureResult = MockedKeystoreService.clear()
      val result = await[Option[Boolean]](futureResult)

      // check
      result shouldBe Some(true)
    }

    "delete data should return false when response status is not 2xx" in new SharedFixture {
      // set up
      implicit val request = FakeRequest(GET, "/abc/def").withSession(SessionKeys.sessionId->sessionId)
      val uri = "http://non-existent-mocked-session-cache/the-domain/the-source-of-all-knowledge/session-test-id"
      stub(mockSessionCache.defaultSource).toReturn("the-source-of-all-knowledge")
      stub(mockSessionCache.baseUri).toReturn("http://non-existent-mocked-session-cache")
      stub(mockSessionCache.domain).toReturn("the-domain")
      stub(mockSessionCache.delete(uri)).toReturn(Future.successful(HttpResponse(404, responseString=Some("forced failure"))))

      // test
      val futureResult = MockedKeystoreService.clear()
      val result = await[Option[Boolean]](futureResult)

      // check
      result shouldBe Some(false)
    }

    "return None when no session id present in session" in new SharedFixture {
      // set up
      implicit val request = FakeRequest(GET, "/abc/def")

      // test
      val futureResult = MockedKeystoreService.clear()
      val result = await[Option[Boolean]](futureResult)

      // check
      result shouldBe None
    }
  }
}
