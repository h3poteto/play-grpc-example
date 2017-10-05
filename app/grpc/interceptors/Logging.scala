package grpc.interceptors

// https://github.com/grpc/grpc-java/blob/166108a9438c22d06eb3b371b5ad34a75e14787c/examples/src/main/java/io/grpc/examples/header/HeaderServerInterceptor.java
// http://blog.soushi.me/entry/2017/08/18/234615
// https://github.com/grpc/grpc-java/issues/2280
// https://github.com/grpc/grpc-java/issues/1949
// https://github.com/saturnism/grpc-java-demos/blob/master/error-handling-example/error-server/src/main/java/com/example/grpc/server/UnknownStatusDescriptionInterceptor.java

import io.grpc.{ServerInterceptor, ServerCall, ServerCallHandler, Metadata, Status}
import io.grpc.ForwardingServerCall.SimpleForwardingServerCall
import play.Logger

class Logging() extends ServerInterceptor {
  override def interceptCall[ReqT, RespT](
    call: ServerCall[ReqT, RespT],
    requestHeaders: Metadata,
    next: ServerCallHandler[ReqT, RespT]): ServerCall.Listener[ReqT] = {

    Logger.info("Request: " + requestHeaders.toString() + ", " + call.getAttributes().toString() + ", " + call.getMethodDescriptor().getFullMethodName())

    val wrapperCall: ServerCall[ReqT, RespT] = new SimpleForwardingServerCall[ReqT, RespT](call) {
      override def sendMessage(message: RespT) {
        Logger.info("Response: " + message.toString())
        super.sendMessage(message)
      }
      override def close(status: Status, trailers: Metadata) {
        Logger.info("Response: " + status.toString())
        super.close(status, trailers)
      }
    }
    return next.startCall(wrapperCall, requestHeaders)
  }
}

