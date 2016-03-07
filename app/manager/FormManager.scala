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

package manager

import play.api.Logger
import play.api.data.{Form, FormError}

/**
 * Created by adamconder on 12/02/2016.
 */

trait FormManager {

  val formService = new FormService

  class FormService {

    def overrideFormError[A](form: Form[A], key: String, value: String) : Form[A] = {
      Logger.debug(s"form to modify $form")
      val modified = form.errors.foldLeft(Seq[FormError]())((acc, error) => if (error.message == key) acc :+ error.copy(messages = Seq(value)) else acc :+ error)
      form.copy[A](form.mapping, form.data, modified, form.value)
    }

    def removeLeadingZero(input : String) : String = {
      if (input.startsWith("0")) {
        val count = input.count(x => x == '0')
        if (count > 1) {
          input
        } else {
          input.replace("0", "")
        }
      } else {
        input
      }
    }

  }

}
