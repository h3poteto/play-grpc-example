package grpc.util

import java.util.UUID
import collection.JavaConverters._
import collection.mutable._

object Tracking {
  val key: String = "trackingId"

  def id(): String =  {
    UUID.randomUUID().toString
  }

  def buildMdcContext(): java.util.Map[String, String] = {
    HashMap(key -> id()).asJava
  }
}
