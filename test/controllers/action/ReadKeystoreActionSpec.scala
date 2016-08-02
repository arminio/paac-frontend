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

class ReadKeystoreActionSpec extends test.BaseSpec {
  "ReadKeystore" should {
    "convert should read keystore" in new MockKeystoreFixture {
      // set up
      val request = FakeRequest("GET", "/abc")
      MockKeystore.map = Map[String,String]("msg"->"hello worlds")
      val readKeystore = new ReadKeystore {
        def keystore: KeystoreService = MockKeystore
        def test[T](r: Request[T]): Future[DataRequest[T]] = super.convert(r)
        def invokeBlock[A](r: Request[A], block: DataRequest[A] => Future[Result]): Future[Result] = Future.successful(Ok("hi"))
      }

      // test
      val result = await(readKeystore.test(request))

      // check
      result.data.get("msg") shouldBe Some("hello worlds")
    }

    "updateSession" should {
      "update session with request keystore data" in new MockKeystoreFixture {
        // set up
        implicit val request = FakeRequest("GET", "/abc").withSession("msg"->"abc")
        val dataRequest = new DataRequest(Map[String,String]("msg"->"hello worlds"),request)
        val readKeystore = new ReadKeystore {
          def keystore: KeystoreService = MockKeystore
          def test[T](): Future[Result] = super.updateSession(dataRequest, Future.successful(Ok("hi")))
          def invokeBlock[A](r: Request[A], block: DataRequest[A] => Future[Result]): Future[Result] = Future.successful(Ok("hi"))
        }

        // test
        val result = await(readKeystore.test())

        // check
        result.session.get("msg") shouldBe Some("hello worlds")
      }
      "does not update session when no keystore data" in new MockKeystoreFixture {
        // set up
        implicit val request = FakeRequest("GET", "/abc").withSession("msg"->"abc")
        val dataRequest = new DataRequest(Map[String,String](),request)
        val readKeystore = new ReadKeystore {
          def keystore: KeystoreService = MockKeystore
          def test[T](): Future[Result] = super.updateSession(dataRequest, Future.successful(Ok("hi")))
          def invokeBlock[A](r: Request[A], block: DataRequest[A] => Future[Result]): Future[Result] = Future.successful(Ok("hi"))
        }

        // test
        val result = await(readKeystore.test())

        // check
        result.session.get("msg") shouldBe Some("abc")
      }
    }

    "ReadKeystoreAction" should {
      "read keystore and update result sesssion" in new MockKeystoreFixture {
        // set up
        implicit val request = FakeRequest("GET", "/abc").withSession("msg"->"abc")
        MockKeystore.map = Map[String,String]("msg"->"hello worlds")
        val readKeystore = ReadKeystoreAction(MockKeystore)

        // test
        val futureResult = readKeystore.invokeBlock(request, {
          (request: DataRequest[_]) =>
          Future.successful(Ok("hi"))
        })
        val result = await(futureResult)

        // check
        result.session.get("msg") shouldBe Some("hello worlds")
      }
    }
  }
}
