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

package controllers.action

import java.util.UUID

import org.scalatest.BeforeAndAfterAll
import play.api.test.Helpers._
import play.api.test.{FakeRequest}
import play.api.Play
import play.api.test._
import play.api.mvc._
import play.api.mvc.Results._
import service._

import scala.concurrent.Future

class WriteKeystoreActionSpec extends test.BaseSpec {
  "WriteKeystore" should {
    "convert" must {
      "wrap request with extracted session data" in new MockKeystoreFixture {
        // set up
        val request = FakeRequest("GET","/").withSession("msg"->"some banal message")
        val writeKeystore = new WriteKeystore() with test.NullMetrics {
          def keystore: KeystoreService = MockKeystore
          def invokeBlock[A](r: Request[A], block: DataRequest[A] => Future[Result]): Future[Result] = Future.successful(Ok("hi"))
          def test[T](request:Request[T]): DataRequest[T] = super.convert(request)
        }

        // test
        val result = writeKeystore.test(request)

        // check
        result.data.get("msg") shouldBe Some("some banal message")
      }
    }
    "update keystore" must {
      "save session data to keystore" in new MockKeystoreFixture {
        // set up
        val request = new DataRequest(Map(), FakeRequest("GET","/").withSession("msg"->"some banal message"))
        val writeKeystore = new WriteKeystore() with test.NullMetrics {
          def keystore: KeystoreService = MockKeystore
          def invokeBlock[A](r: Request[A], block: DataRequest[A] => Future[Result]): Future[Result] = Future.successful(Ok("hi"))
          def test[T](): Future[Result] = super.updateKeystore(request, Future.successful(Ok("hi")))
        }

        // test
        val result = await(writeKeystore.test)

        // check
        MockKeystore.map.get("msg") shouldBe Some("some banal message")
      }
    }
  }

  "WriteKeystoreAction" should {
    "invokeBlock" must {
      "convert request and after block execution update keystore" in new MockKeystoreFixture {
        // set up
        implicit val request = FakeRequest("GET","/")
        val test = WriteKeystoreAction(MockKeystore)
        val block = (r: DataRequest[_]) => Future.successful(Ok("hi").withSession("msg"->"hi"))

        // test
        val result = await(test.invokeBlock(request, block))

        // check
        MockKeystore.map.get("msg") shouldBe Some("hi")
      }
    }
  }
}
