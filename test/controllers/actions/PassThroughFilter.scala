package controllers.actions

import models.requests.DataRequest
import play.api.mvc.{ActionFilter, Result}

import scala.concurrent.{ExecutionContext, Future}

final class PassThroughFilter[R[_]](using val executionContext: ExecutionContext) extends ActionFilter[R] {
  def filter[A](request: R[A]): Future[Option[Result]] = Future.successful(None)
}
