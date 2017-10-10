package grpc.util

import io.grpc.{Metadata}
import java.util.UUID
import collection.JavaConverters._
import collection.mutable._

object MDCContext {
  object Tracking {
    val key: String = "tracking_id"

    def id(): String =  {
      UUID.randomUUID().toString
    }
  }

  object Request {
    val key: String = "request_id"

    def id(headers: Metadata): String =  {
      val k: Metadata.Key[String] = Metadata.Key.of(key, Metadata.ASCII_STRING_MARSHALLER)
      headers.get(k)
    }
  }

  def buildMdcContext(headers: Metadata): java.util.Map[String, String] = {
    HashMap(Tracking.key -> Tracking.id(), Request.key -> Request.id(headers)).asJava
  }
}
