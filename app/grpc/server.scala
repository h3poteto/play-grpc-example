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
import play.api.inject.ApplicationLifecycle
import akka.actor.ActorSystem
import scalaz._
import play.api._


trait Runner extends Runnable {
  run()

  final def run() = {
    val app = application()
    try {
      task(app)
    } finally {
      Play.stop(app)
    }
  }

  def task(app: Application)

  def environment(): Mode = {
    System.getProperty("play.mode") match {
      case "Prod" => Mode.Prod
      case _ => Mode.Dev
    }
  }

  def application(): Application = {
    val env = Environment(new java.io.File("."), this.getClass.getClassLoader, environment)
    val context = ApplicationLoader.createContext(env)
    val loader = ApplicationLoader(context)
    loader.load(context)
  }
}

object RunnerImpl extends App with Runner {
  def task(app: Application) {
    val exec = ExecutionContext.global
    val server = new GrpcServer(exec)
    server.start()
    server.blockUnitShutdown()
  }
}

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

    // JVM自体がshutdownされた際に呼ばれる
    sys.addShutdownHook {
      Logger.info("*** shutting down gPRC server since JVM is shutting down")
      self.stop()
    }
  }

  def stop(): Unit = {
    if (server != null) {
      Logger.info("*** gRPC server shutdown")
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
