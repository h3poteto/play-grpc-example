package grpc.errorhandler

// https://github.com/grpc/grpc-java/blob/166108a9438c22d06eb3b371b5ad34a75e14787c/examples/src/main/java/io/grpc/examples/header/HeaderServerInterceptor.java
// http://blog.soushi.me/entry/2017/08/18/234615

import io.grpc.{ServerInterceptor, ServerCall, ServerCallHandler, Metadata, Status}
import io.grpc.ForwardingServerCallListener.SimpleForwardingServerCallListener
import play.Logger

class ErrorHandler() extends ServerInterceptor {
  override def interceptCall[ReqT, RespT](
    call: ServerCall[ReqT, RespT],
    requestHeaders: Metadata,
    next: ServerCallHandler[ReqT, RespT]): ServerCall.Listener[ReqT] = {

    return (new SimpleForwardingServerCallListener[ReqT](next.startCall(call, requestHeaders)) {
      override def onHalfClose() {
        try {
          super.onHalfClose()
        } catch {
          case e: Exception  =>
            handleException(call, requestHeaders, e)
            throw e
        }
      }

      override def onReady() {
        try {
          super.onReady()
        } catch {
          case e: Exception =>
            handleException(call, requestHeaders, e)
            throw e
        }
      }
    })
  }

  private def handleException[ReqT, RespT](
    call: ServerCall[ReqT, RespT],
    requestHeaders: Metadata,
    ex: Exception) = {
    Logger.error(ex.getMessage)
    call.close(Status.Code.CANCELLED.toStatus(), requestHeaders)
  }
}

