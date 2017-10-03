package grpc

import com.google.inject.{ Guice, Injector }
import play.Logger
import io.grpc.{Server, ServerBuilder, Status, StatusException}
import io.grpc.stub.StreamObserver
import grpc.errorhandler.ErrorHandler
import users.users.{RequestType, CwUserId, UsersGrpc}

import scala.concurrent.{ExecutionContext, Future}
import scala.concurrent.duration._
import javax.inject.{Inject, Named}
import akka.actor.ActorSystem

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
  private var users: Array[CwUserId] = Array.empty

  def start(): Unit = {
    server = ServerBuilder.forPort(port).addService(UsersGrpc.bindService(new UsersImpl, executionContext)).intercept(new ErrorHandler()).build.start
    Logger.info("Server started, listening on " + port)
    sys.addShutdownHook {
      Logger.info("*** shutting down gPRC server since JVM is shutting down")
      self.stop()
      Logger.info("*** server shutdown")
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
    override def create(request: CwUserId): scala.concurrent.Future[CwUserId] = {
      users = users :+ request
      Future.successful(request)
    }

    override def list(request: RequestType, stream: StreamObserver[CwUserId]) = {
      for (u <- users) {
        stream.onNext(u)
      }
      stream.onCompleted()
    }
  }
}
