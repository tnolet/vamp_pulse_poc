name := "vamp_pulse"

version := "0.1"

name := """vamp pulse"""

version := "1.0-SNAPSHOT"

lazy val root = (project in file(".")).enablePlugins(PlayScala)

scalaVersion := "2.10.4"

resolvers += "Apache repo" at "https://repository.apache.org/content/repositories/releases"

resolvers += "Typesafe repository" at "http://repo. typesafe.com/typesafe/releases/"

scalacOptions ++= Seq("-feature")

libraryDependencies ++= {
  Seq(
    "com.sclasen" %% "akka-kafka" % "0.0.7",
    "org.slf4j" % "log4j-over-slf4j" % "1.6.6",
    "com.typesafe.play" %% "play-json" % "2.3.4",
    "com.typesafe.play" %% "play-ws" % "2.3.4"
  )
}

