package com.example.app

import org.scalatra._
import io.circe.Encoder
import io.circe.syntax._
import io.circe.generic.auto._

sealed trait MyResult[T]
case class Success[T](v: T) extends MyResult[T] // 200
case class Failure[T](err: MyError) extends MyResult[T] // error

sealed abstract class MyError(
    val statusCode: Int,
    val message: String,
    val code: Int
)
case object UserNotExits extends MyError(404, "", 4040)
case class DBConnectionError(ex: Exception) extends MyError(500, "", 5000)

case class Foo(v: Int)

trait BaseServlet extends ScalatraServlet {
  def result[T: Encoder](data: MyResult[T]): ActionResult = data match {
    case Success(v) =>
      ActionResult(
        200,
        v.asJson,
        Map("content-type" -> "application/json")
      )
    case Failure(err) =>
      err match {
        case DBConnectionError(ex) =>
          ActionResult(
            err.statusCode,
            Map(
              "code" -> err.code.toString,
              "message" -> err.message
            ).asJson,
            Map("content-type" -> "application/json")
          )
        case _ =>
          ActionResult(
            err.statusCode,
            Map(
              "code" -> err.code.toString,
              "message" -> err.message
            ).asJson,
            Map("content-type" -> "application/json")
          )
      }
  }
}

class MyScalatraServlet extends BaseServlet {
  get("/") {
    result(Success(Foo(1)))
  }
}
