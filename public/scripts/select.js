/*
 * Copyright 2015 HM Revenue and Customs
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

$(document).ready(function() {
    // remove buggy handlers added dynamically by application.min.js
    $('input[type=checkbox]').unbind('focus click').unbind('click').unbind('change')

    // set classes for all checked checkboxes
    $('input:checked').parent('.block-label').addClass('selected');

    $('input[type=checkbox]').each(function() {
        var $label = $(this).parent();

        // add click handler to toggle selected class based on click
        $(this).unbind('click').on('click', function(event) {
            $label.toggleClass('selected');
            event.stopPropagation();
        });

        // add focus in handler
        $label.on('focus', "input", function(event) {
            $label.toggleClass('add-focus')
        })

        // add focus out handler
        $label.on('focusout', "input", function(event) {
            $label.toggleClass('add-focus')
        })
    });
});