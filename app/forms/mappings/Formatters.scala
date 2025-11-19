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

package forms.mappings

import forms.Validation
import forms.Validation.{clientNameInputMaxLength, clientReferenceInputMaxLength, employerReferenceInputMaxLength}
import play.api.data.FormError
import play.api.data.format.Formatter
import models.Enumerable
import models.agent.ClientListFormData
import viewmodels.agent.SearchBy
import viewmodels.agent.SearchBy.*

import scala.util.control.Exception.nonFatalCatch

trait Formatters {

  private[mappings] def stringFormatter(errorKey: String, args: Seq[String] = Seq.empty): Formatter[String] =
    new Formatter[String] {

      override def bind(key: String, data: Map[String, String]): Either[Seq[FormError], String] =
        data.get(key) match {
          case None                      => Left(Seq(FormError(key, errorKey, args)))
          case Some(s) if s.trim.isEmpty => Left(Seq(FormError(key, errorKey, args)))
          case Some(s)                   => Right(s)
        }

      override def unbind(key: String, value: String): Map[String, String] =
        Map(key -> value)
    }

  private[mappings] def booleanFormatter(
    requiredKey: String,
    invalidKey: String,
    args: Seq[String] = Seq.empty
  ): Formatter[Boolean] =
    new Formatter[Boolean] {

      private val baseFormatter = stringFormatter(requiredKey, args)

      override def bind(key: String, data: Map[String, String]) =
        baseFormatter
          .bind(key, data)
          .flatMap {
            case "true"  => Right(true)
            case "false" => Right(false)
            case _       => Left(Seq(FormError(key, invalidKey, args)))
          }

      def unbind(key: String, value: Boolean) = Map(key -> value.toString)
    }

  private[mappings] def intFormatter(
    requiredKey: String,
    wholeNumberKey: String,
    nonNumericKey: String,
    args: Seq[String] = Seq.empty
  ): Formatter[Int] =
    new Formatter[Int] {

      val decimalRegexp = """^-?(\d*\.\d*)$"""

      private val baseFormatter = stringFormatter(requiredKey, args)

      override def bind(key: String, data: Map[String, String]) =
        baseFormatter
          .bind(key, data)
          .map(_.replace(",", ""))
          .flatMap {
            case s if s.matches(decimalRegexp) =>
              Left(Seq(FormError(key, wholeNumberKey, args)))
            case s                             =>
              nonFatalCatch
                .either(s.toInt)
                .left
                .map(_ => Seq(FormError(key, nonNumericKey, args)))
          }

      override def unbind(key: String, value: Int) =
        baseFormatter.unbind(key, value.toString)
    }

  private[mappings] def enumerableFormatter[A](requiredKey: String, invalidKey: String, args: Seq[String] = Seq.empty)(
    implicit ev: Enumerable[A]
  ): Formatter[A] =
    new Formatter[A] {

      private val baseFormatter = stringFormatter(requiredKey, args)

      override def bind(key: String, data: Map[String, String]): Either[Seq[FormError], A] =
        baseFormatter.bind(key, data).flatMap { str =>
          ev.withName(str)
            .map(Right.apply)
            .getOrElse(Left(Seq(FormError(key, invalidKey, args))))
        }

      override def unbind(key: String, value: A): Map[String, String] =
        baseFormatter.unbind(key, value.toString)
    }

  private[mappings] def currencyFormatter(
    requiredKey: String,
    invalidNumericKey: String,
    nonNumericKey: String,
    args: Seq[String] = Seq.empty
  ): Formatter[BigDecimal] =
    new Formatter[BigDecimal] {
      val isNumeric    = """(^£?\d*$)|(^£?\d*\.\d*$)"""
      val validDecimal = """(^£?\d*$)|(^£?\d*\.\d{1,2}$)"""

      private val baseFormatter = stringFormatter(requiredKey, args)

      override def bind(key: String, data: Map[String, String]): Either[Seq[FormError], BigDecimal] =
        baseFormatter
          .bind(key, data)
          .map(_.replace(",", "").replace(" ", ""))
          .flatMap {
            case s if !s.matches(isNumeric)    =>
              Left(Seq(FormError(key, nonNumericKey, args)))
            case s if !s.matches(validDecimal) =>
              Left(Seq(FormError(key, invalidNumericKey, args)))
            case s                             =>
              nonFatalCatch
                .either(BigDecimal(s.replace("£", "")))
                .left
                .map(_ => Seq(FormError(key, nonNumericKey, args)))
          }

      override def unbind(key: String, value: BigDecimal): Map[String, String] =
        baseFormatter.unbind(key, value.toString)
    }

  private[mappings] def searchFilterFormatter(
    searchBy: SearchBy,
    args: Seq[String] = Seq.empty
  ): Formatter[String] =
    new Formatter[String] {

      val errorKey                        = s"agent.clientListSearch.searchFilter.${searchBy.toString.toLowerCase()}.error.required"
      val clientNameFormatErrorKey        = "agent.clientListSearch.searchFilter.cn.error.format"
      val clientNameLengthErrorKey        = "agent.clientListSearch.searchFilter.cn.error.length"
      val clientReferenceFormatErrorKey   = "agent.clientListSearch.searchFilter.cr.error.format"
      val clientReferenceLengthErrorKey   = "agent.clientListSearch.searchFilter.cr.error.length"
      val employerReferenceFormatErrorKey = "agent.clientListSearch.searchFilter.er.error.format"
      val employerReferenceLengthErrorKey = "agent.clientListSearch.searchFilter.er.error.length"

      override def bind(key: String, data: Map[String, String]): Either[Seq[FormError], String] =
        data.get(key) match {
          case None                                                                                                  => Left(Seq(FormError(key, errorKey, args)))
          case Some(s) if s.trim.isEmpty                                                                             => Left(Seq(FormError(key, errorKey, args)))
          case Some(s) if (searchBy == SearchBy.CN) && !s.matches(Validation.clientNameInputPattern.toString())      =>
            Left(Seq(FormError(key, clientNameFormatErrorKey, args)))
          case Some(s) if (searchBy == SearchBy.CN) && (s.length > clientNameInputMaxLength)                         =>
            Left(Seq(FormError(key, clientNameLengthErrorKey, args)))
          case Some(s) if (searchBy == SearchBy.CR) && !s.matches(Validation.clientReferenceInputPattern.toString()) =>
            Left(Seq(FormError(key, clientReferenceFormatErrorKey, args)))
          case Some(s) if (searchBy == SearchBy.CR) && (s.length > clientReferenceInputMaxLength)                    =>
            Left(Seq(FormError(key, clientReferenceLengthErrorKey, args)))
          case Some(s)
              if (searchBy == SearchBy.ER) && !s.matches(Validation.employerReferenceInputPattern.toString()) =>
            Left(Seq(FormError(key, employerReferenceFormatErrorKey, args)))
          case Some(s) if (searchBy == SearchBy.ER) && (s.length > employerReferenceInputMaxLength)                  =>
            Left(Seq(FormError(key, employerReferenceLengthErrorKey, args)))
          case Some(s)                                                                                               => Right(s)
        }

      override def unbind(key: String, value: String): Map[String, String] =
        Map(key -> value)
    }

  private[mappings] def clientListSearchFormatter(
    requiredKey: String => String,
    args: Seq[String]
  ): Formatter[ClientListFormData] =
    new Formatter[ClientListFormData] {

      override def bind(key: String, data: Map[String, String]): Either[Seq[FormError], ClientListFormData] = {
        def bindSearchBy(key: String): Either[Seq[FormError], String]                         =
          stringFormatter(requiredKey(key), args).bind(key, data)
        def bindSearchFilter(key: String, searchBy: SearchBy): Either[Seq[FormError], String] =
          searchFilterFormatter(searchBy, args).bind(key, data)
        bindSearchBy(Constants.SearchBy) match {
          case Right(searchBy)     =>
            def validateSearchFilter(searchByVal: SearchBy) =
              bindSearchFilter(Constants.SearchFilter, searchByVal) match {
                case Right(searchFilter)     => Right(ClientListFormData(searchBy, searchFilter))
                case Left(searchFilterError) => Left(searchFilterError)
              }
            searchBy match {
              case CN.toString => validateSearchFilter(CN)
              case CR.toString => validateSearchFilter(CR)
              case ER.toString => validateSearchFilter(ER)
              case _           => Left(Seq(FormError(Constants.SearchBy, "agent.clientListSearch.searchBy.error.required", args)))
            }
          case Left(searchByError) =>
            Left(searchByError)
        }

      }

      override def unbind(key: String, value: ClientListFormData): Map[String, String] =
        Map(
          Constants.SearchBy     -> value.searchBy,
          Constants.SearchFilter -> value.searchFilter
        )
    }

}
