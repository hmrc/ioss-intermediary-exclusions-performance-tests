/*
 * Copyright 2025 HM Revenue & Customs
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

package uk.gov.hmrc.perftests.exclusions

import io.gatling.core.Predef._
import io.gatling.core.session.Expression
import io.gatling.http.Predef._
import io.gatling.http.request.builder.HttpRequestBuilder
import uk.gov.hmrc.performance.conf.ServicesConfiguration

import java.time.LocalDate

object ExclusionsRequests extends ServicesConfiguration {

  val baseUrl: String = baseUrlFor("ioss-intermediary-exclusions-frontend")
  val route: String   = "/pay-clients-vat-on-eu-sales/leave-import-one-stop-shop-intermediary"

  val loginUrl = baseUrlFor("auth-login-stub")

  def inputSelectorByName(name: String): Expression[String] = s"input[name='$name']"

  def getAuthorityWizard =
    http("Get Authority Wizard page")
      .get(loginUrl + s"/auth-login-stub/gg-sign-in")
      .check(css(inputSelectorByName("csrfToken"), "value").saveAs("csrfToken"))
      .check(status.in(200, 303))

  def postAuthorityWizard =
    http("Enter Auth login credentials ")
      .post(loginUrl + s"/auth-login-stub/gg-sign-in")
      .formParam("csrfToken", "#{csrfToken}")
      .formParam("authorityId", "")
      .formParam("gatewayToken", "")
      .formParam("credentialStrength", "strong")
      .formParam("confidenceLevel", "50")
      .formParam("affinityGroup", "Organisation")
      .formParam("email", "user@test.com")
      .formParam("credentialRole", "User")
      .formParam("redirectionUrl", baseUrl + route)
      .formParam("enrolment[0].name", "HMRC-MTD-VAT")
      .formParam("enrolment[0].taxIdentifier[0].name", "VRN")
      .formParam("enrolment[0].taxIdentifier[0].value", "100000001")
      .formParam("enrolment[0].state", "Activated")
      .formParam("enrolment[1].name", "HMRC-IOSS-INT")
      .formParam("enrolment[1].taxIdentifier[0].name", "IntNumber")
      .formParam("enrolment[1].taxIdentifier[0].value", "IN9001234567")
      .formParam("enrolment[1].state", "Activated")
      .check(status.in(200, 303))
      .check(headerRegex("Set-Cookie", """mdtp=(.*)""").saveAs("mdtpCookie"))

  def getExclusionsMovedToADifferentCountry =
    http("Get Exclusions Moved To A Different Country page")
      .get(s"$baseUrl$route/exclusions-moved-to-a-different-country")
      .header("Cookie", "mdtp=#{mdtpCookie}")
      .check(css(inputSelectorByName("csrfToken"), "value").saveAs("csrfToken"))
      .check(status.in(200))

  def testExclusionsMovedToADifferentCountry(answer: Boolean) =
    http("Post Exclusions Moved To A Different Country page")
      .post(s"$baseUrl$route/exclusions-moved-to-a-different-country")
      .formParam("csrfToken", "#{csrfToken}")
      .formParam("value", answer)
      .check(status.in(200, 303))

  def postExclusionsMovedToADifferentCountry(answer: Boolean) =
    if (answer) {
      testExclusionsMovedToADifferentCountry(answer)
        .check(header("Location").is(s"$route/exclusions-which-eu-country"))
    } else {
      testExclusionsMovedToADifferentCountry(answer)
        .check(header("Location").is(s"$route/exclusions-leave-scheme"))
    }

  def getExclusionsWhichEuCountry =
    http("Get Exclusions Which Eu Country page")
      .get(s"$baseUrl$route/exclusions-which-eu-country")
      .header("Cookie", "mdtp=${mdtpCookie}")
      .check(css(inputSelectorByName("csrfToken"), "value").saveAs("csrfToken"))
      .check(status.in(200))

  def postExclusionsWhichEuCountry =
    http("Post Exclusions Which Eu Country")
      .post(s"$baseUrl$route/exclusions-which-eu-country")
      .formParam("csrfToken", "#{csrfToken}")
      .formParam("value", "HR")
      .check(status.in(200, 303))
      .check(header("Location").is(s"$route/exclusions-move-date"))

  def getExclusionsMoveDate =
    http("Get Exclusions Move Date page")
      .get(s"$baseUrl$route/exclusions-move-date")
      .header("Cookie", "mdtp=#{mdtpCookie}")
      .check(css(inputSelectorByName("csrfToken"), "value").saveAs("csrfToken"))
      .check(status.in(200))

  def postExclusionsMoveDate =
    http("Post Exclusions Move Date")
      .post(s"$baseUrl$route/exclusions-move-date")
      .formParam("csrfToken", "#{csrfToken}")
      .formParam("value.day", s"${LocalDate.now().getDayOfMonth}")
      .formParam("value.month", s"${LocalDate.now().getMonthValue}")
      .formParam("value.year", s"${LocalDate.now().getYear}")
      .check(status.in(200, 303))
      .check(header("Location").is(s"$route/exclusions-tax-number"))

  def getExclusionsTaxNumber =
    http("Get Exclusions Tax Number page")
      .get(s"$baseUrl$route/exclusions-tax-number")
      .header("Cookie", "mdtp=#{mdtpCookie}")
      .check(css(inputSelectorByName("csrfToken"), "value").saveAs("csrfToken"))
      .check(status.in(200))

  def postExclusionsTaxNumber =
    http("Post Exclusions Tax Number")
      .post(s"$baseUrl$route/exclusions-tax-number")
      .formParam("csrfToken", "#{csrfToken}")
      .formParam("value", "HR01234567888")
      .check(status.in(200, 303))
      .check(header("Location").is(s"$route/check-your-answers"))

  def getCheckYourAnswers =
    http("Get Check Your Answers page")
      .get(s"$baseUrl$route/check-your-answers")
      .header("Cookie", "mdtp=#{mdtpCookie}")
      .check(css(inputSelectorByName("csrfToken"), "value").saveAs("csrfToken"))
      .check(status.in(200))

  def postCheckYourAnswers =
    http("Post Check Your Answers")
      .post(s"$baseUrl$route/check-your-answers/false")
      .formParam("csrfToken", "#{csrfToken}")
      .check(status.in(200, 303))
      .check(header("Location").is(s"$route/exclusions-request-received"))

  def getExclusionsRequestReceived =
    http("Get Exclusions Request Received page")
      .get(s"$baseUrl$route/exclusions-request-received")
      .header("Cookie", "mdtp=#{mdtpCookie}")
      .check(status.in(200))

  def getExclusionsLeaveScheme =
    http("Get Exclusions Leave Scheme page")
      .get(s"$baseUrl$route/exclusions-leave-scheme")
      .header("Cookie", "mdtp=#{mdtpCookie}")
      .check(css(inputSelectorByName("csrfToken"), "value").saveAs("csrfToken"))
      .check(status.in(200))

  def postExclusionsLeaveScheme =
    http("Post Exclusions Leave Scheme")
      .post(s"$baseUrl$route/exclusions-leave-scheme")
      .formParam("csrfToken", "#{csrfToken}")
      .formParam("value", true)
      .check(status.in(200, 303))
      .check(header("Location").is(s"$route/exclusions-stopped-using-service-date"))

  def getExclusionsStoppedUsingServiceDate =
    http("Get Exclusions Stopped Using Service Date page")
      .get(s"$baseUrl$route/exclusions-stopped-using-service-date")
      .header("Cookie", "mdtp=#{mdtpCookie}")
      .check(css(inputSelectorByName("csrfToken"), "value").saveAs("csrfToken"))
      .check(status.in(200))

  def postExclusionsStoppedUsingServiceDate =
    http("Post Exclusions Stopped Using Service Date")
      .post(s"$baseUrl$route/exclusions-stopped-using-service-date")
      .formParam("csrfToken", "#{csrfToken}")
      .formParam("value.day", s"${LocalDate.now().getDayOfMonth}")
      .formParam("value.month", s"${LocalDate.now().getMonthValue}")
      .formParam("value.year", s"${LocalDate.now().getYear}")
      .check(status.in(200, 303))
      .check(header("Location").is(s"$route/check-your-answers"))

}
