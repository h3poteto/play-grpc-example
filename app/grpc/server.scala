package grpc

import com.google.inject.{ Guice, Injector }
import io.grpc.{Server, ServerBuilder, Status, StatusRuntimeException, ServerInterceptors}
import io.grpc.stub.StreamObserver
import io.grpc.util._

import proto.users.{RequestType, User, UsersGrpc}
import grpc.interceptors.{ErrorHandler, Logging}
import grpc.util.Logger

import scala.concurrent.{ExecutionContext, Future}
import scala.concurrent.duration._
import javax.inject.{Inject, Named}
import akka.actor.ActorSystem
import scalaz._

trait Runner {
  def start(): Unit
}

class RunnerImpl @Inject() (actorSystem: ActorSystem)(implicit exec: ExecutionContext) extends Runner {
  def start(): Unit = {
    val server = new GrpcServer(exec)
    server.start()
    server.blockUnitShutdown()
  }
  actorSystem.scheduler.scheduleOnce(1.seconds) {
    start()
  }
}

// object GrpcServer {
//   private val logger = Logger.getLogger(classOf[GrpcServer].getName)

//   def main(args: Array[String]): Unit = {
//     val server = new GrpcServer(ExecutionContext.global)
//     server.start()
//     server.blockUnitShutdown()
//   }

//   private val port = sys.env.getOrElse("SERVER_PORT", "50051").asInstanceOf[String].toInt
// }

class GrpcServer(executionContext: ExecutionContext) { self =>
  private val port = sys.env.getOrElse("SERVER_PORT", "50051").asInstanceOf[String].toInt
  private[this] var server: Server = null
  private var users: Array[User] = Array.empty

  def start(): Unit = {
    server = ServerBuilder.forPort(port).addService(
      ServerInterceptors.intercept(
        UsersGrpc.bindService(new UsersImpl, executionContext),
        new Logging,
        new ErrorHandler,
        TransmitStatusRuntimeExceptionInterceptor.instance()
      )
    ).build.start
    Logger.info("gRPC server started, listening on " + port)
    sys.addShutdownHook {
      Logger.info("*** shutting down gPRC server since JVM is shutting down")
      self.stop()
      Logger.info("*** gRPC server shutdown")
    }
  }

  def stop(): Unit = {
    if (server != null) {
      server.shutdown()
    }
  }

  def blockUnitShutdown(): Unit = {
    if (server != null) {
      server.awaitTermination()
    }
  }

  private class UsersImpl extends UsersGrpc.Users {
    override def create(request: User): scala.concurrent.Future[User] = {
      Logger.info("writing")
      Validation.user(request) match {
        case Success(_) => true
        case Failure(e) => throw e.head
      }
      users = users :+ request
      Future.successful(request)
    }

    override def list(request: RequestType, stream: StreamObserver[User]) = {
      for (u <- users) {
        stream.onNext(u)
      }
      stream.onCompleted()
    }
  }
}
