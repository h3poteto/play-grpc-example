package grpc

import models.User
import scalaz._
import scalaz.syntax.validation._
import scalaz.syntax.apply._


object Validation {
  def user(u: proto.users.User):Validation[NonEmptyList[ValidationError], User] = {
    // 両方共successだった場合に，Userのコンストラクタに値を渡してインスタンスを生成し，Successを返す
    (validateName(u.name) |@| validateAge(u.age.toInt))(User)
  }

  def validateName(name: String): ValidationNel[ValidationError, String] = {
    // https://github.com/scalaz/scalaz/blob/c0e4b531847348e1fd533c7d3605fe69320dde91/core/src/main/scala/scalaz/syntax/ValidationOps.scala#L7
    // なのでValidationNel[ValidationError, String]生成するためにsuccessNel[ValidationError]という型指定にする必要がある
    if (name.length > 1 && name.length < 20) name.successNel[ValidationError]
    else (new ValidationError("name length must be between 1 and 20 characters")).failureNel[String]
  }

  def validateAge(age: Int): ValidationNel[ValidationError, Int] = {
    if (age >= 0) age.successNel[ValidationError]
    else (new ValidationError("age must 0 or more")).failureNel[Int]
  }
}


class ValidationError(message: String = null, cause: Throwable = null) extends RuntimeException(message, cause)
