import java.net.URLClassLoader

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
  "mysql" % "mysql-connector-java" % "5.1.43"
)
