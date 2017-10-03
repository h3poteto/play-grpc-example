package grpc.interceptors

// https://github.com/grpc/grpc-java/blob/166108a9438c22d06eb3b371b5ad34a75e14787c/examples/src/main/java/io/grpc/examples/header/HeaderServerInterceptor.java
// http://blog.soushi.me/entry/2017/08/18/234615

import io.grpc.{ServerInterceptor, ServerCall, ServerCallHandler, Metadata, Status}
import io.grpc.ForwardingServerCallListener.SimpleForwardingServerCallListener
import play.Logger

class Logging() extends ServerInterceptor {
  override def interceptCall[ReqT, RespT](
    call: ServerCall[ReqT, RespT],
    requestHeaders: Metadata,
    next: ServerCallHandler[ReqT, RespT]): ServerCall.Listener[ReqT] = {

    Logger.info(call.getAttributes().toString() + ", " + call.getMethodDescriptor().getFullMethodName())
    return next.startCall(call, requestHeaders)
  }
}

