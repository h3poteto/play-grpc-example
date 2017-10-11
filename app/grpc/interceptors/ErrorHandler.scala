package grpc.interceptors

// https://github.com/grpc/grpc-java/blob/166108a9438c22d06eb3b371b5ad34a75e14787c/examples/src/main/java/io/grpc/examples/header/HeaderServerInterceptor.java
// http://blog.soushi.me/entry/2017/08/18/234615
// https://github.com/grpc/grpc-java/blob/2b1eee90e5bd7f5ad905e34f73f2040d6c9a3568/core/src/main/java/io/grpc/util/TransmitStatusRuntimeExceptionInterceptor.java
// https://github.com/saturnism/grpc-java-demos/blob/master/error-handling-example/error-server/src/main/java/com/example/grpc/server/UnknownStatusDescriptionInterceptor.java
// https://github.com/grpc/grpc-java/blob/d387bfe72fff7b89f2cf7ce09bca418feb322085/stub/src/main/java/io/grpc/stub/ServerCalls.java#L345

import io.grpc.{ForwardingServerCall, ForwardingServerCallListener, Metadata, ServerCall, ServerCallHandler, ServerInterceptor, Status, StatusRuntimeException}
import grpc.util.Logger
import grpc.ValidationError

class ErrorHandler extends ServerInterceptor {
  override def interceptCall[ReqT, RespT](
    serverCall: ServerCall[ReqT, RespT],
    headers: Metadata,
    next: ServerCallHandler[ReqT, RespT]): ServerCall.Listener[ReqT] = {
    val listener = next.startCall(serverCall, headers)
    return new ForwardingServerCallListener.SimpleForwardingServerCallListener[ReqT](listener) {
      override def onMessage(message: ReqT) {
        try {
          super.onMessage(message)
        } catch {
          case e: Exception =>
            closeWithException(e, headers)
        }
      }

      override def onHalfClose() {
        try {
          super.onHalfClose()
        } catch {
          case e: Exception =>
            closeWithException(e, headers)
        }
      }
      override def onCancel() {
        try {
          super.onCancel()
        } catch {
          case e: Exception =>
            closeWithException(e, headers)
        }
      }

      override def onComplete() {
        try {
          super.onComplete()
        } catch {
          case e: Exception =>
            closeWithException(e, headers)
        }
      }

      override def onReady() {
        try {
          super.onReady()
        } catch {
          case e: Exception =>
            closeWithException(e, headers)
        }
      }

      private def closeWithException(t: Exception, requestHeader: Metadata) {
        // この段階で持っているserverCallをcloseしてしまうと，streamがその時点で削除される
        // その結果client側では，GRPC::Unknownが出てしまう
        // そのため，ここでExceptionをgrpcのStatusにマッピングしてStatusRuntimeExceptionを投げ直す
        // 投げられたStatusRutnimeExceptionはTransmitStatusRuntimeExceptionInterceptorで拾われる
        var status: Status = null

        t match {
          case e: ValidationError => status = Status.Code.INVALID_ARGUMENT.toStatus().withDescription(t.getMessage()).withCause(t)
          case _ => status = Status.Code.INTERNAL.toStatus().withDescription(t.getMessage()).withCause(t)
        }
        Logger.error(status.toString())
        throw new StatusRuntimeException(status)
      }
    }
  }
}
