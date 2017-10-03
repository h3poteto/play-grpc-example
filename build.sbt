import java.net.URLClassLoader
import com.trueaccord.scalapb.compiler.Version.{grpcJavaVersion, scalapbVersion, protobufVersion}

name := """play-grcp-example"""

version := "1.0-SNAPSHOT"

lazy val root = (project in file(".")).enablePlugins(PlayScala)

scalaVersion := "2.12.3"

libraryDependencies ++= Seq(
  guice,
  jdbc,
  evolutions,
  "com.typesafe.play" %% "play" % "2.6.3",
  "com.typesafe.play" %% "play-jdbc-api" % "2.6.3",
  "mysql" % "mysql-connector-java" % "5.1.43",
  "io.grpc" % "grpc-netty" % grpcJavaVersion,
  "com.trueaccord.scalapb" %% "scalapb-runtime" % scalapbVersion % "protobuf",
  "com.trueaccord.scalapb" %% "scalapb-runtime-grpc" % scalapbVersion,
  "io.grpc" % "grpc-all" % grpcJavaVersion
)

PB.targets in Compile := Seq(scalapb.gen() -> ((sourceManaged in Compile).value / "protobuf-scala"))
PB.protoSources in Compile += (baseDirectory in LocalRootProject).value / "protocol"
