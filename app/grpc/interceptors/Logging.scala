package grpc.interceptors

// https://github.com/grpc/grpc-java/blob/166108a9438c22d06eb3b371b5ad34a75e14787c/examples/src/main/java/io/grpc/examples/header/HeaderServerInterceptor.java
// http://blog.soushi.me/entry/2017/08/18/234615
// https://github.com/saturnism/grpc-java-demos/blob/master/error-handling-example/error-server/src/main/java/com/example/grpc/server/UnknownStatusDescriptionInterceptor.java
// https://github.com/grpc/grpc-java/issues/2280#issuecomment-247745278
// https://github.com/grpc/grpc-java/issues/1949

import org.slf4j.MDC
import io.grpc.{ServerInterceptor, ServerCall, ServerCallHandler, Metadata, Status}
import io.grpc.{ForwardingServerCallListener, ForwardingServerCall}

import grpc.util.{Logger, MDCContext}

// gRPCサーバ内でのユニークIDを付与し，リクエスト・レスポンスのログを吐く
class Logging() extends ServerInterceptor {

  override def interceptCall[ReqT, RespT](
    serverCall: ServerCall[ReqT, RespT],
    headers: Metadata,
    next: ServerCallHandler[ReqT, RespT]): ServerCall.Listener[ReqT] = {

    // リクエストごとに固有のrequestIdを作る
    val mdcContext: java.util.Map[String, String] = MDCContext.buildMdcContext(headers)
    MDC.setContextMap(mdcContext)

    val wrapperCall: ServerCall[ReqT, RespT] = new ForwardingServerCall.SimpleForwardingServerCall[ReqT, RespT](serverCall) {
      override def request(numMessages: Int) {
        MDC.setContextMap(mdcContext)
        Logger.info("Request: " + headers.toString() + ", " + serverCall.getAttributes().toString() + ", " + serverCall.getMethodDescriptor().getFullMethodName())
        super.request(numMessages)
      }

      override def sendMessage(message: RespT) {
        MDC.setContextMap(mdcContext)
        Logger.info("Response: " + message.toString())
        super.sendMessage(message)
      }

      override def close(status: Status, trailers: Metadata) {
        MDC.setContextMap(mdcContext)
        Logger.info("Response: " + status.toString())
        super.close(status, trailers)
        MDC.clear()
      }
    }

    val listener = next.startCall(wrapperCall, headers)
    return new ForwardingServerCallListener.SimpleForwardingServerCallListener[ReqT](listener) {

      override def onMessage(message: ReqT) {
        MDC.setContextMap(mdcContext)
        super.onMessage(message)
      }

      override def onHalfClose() {
        MDC.setContextMap(mdcContext)
        super.onHalfClose()
      }

      override def onCancel() {
        MDC.setContextMap(mdcContext)
        super.onCancel()
        MDC.clear()
      }

      override def onComplete() {
        MDC.setContextMap(mdcContext)
        super.onComplete()
        MDC.clear()
      }

      override def onReady() {
        MDC.setContextMap(mdcContext)
        super.onReady()
      }
    }
  }
}
