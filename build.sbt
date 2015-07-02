name := """site-perso"""

version := "1.0-SNAPSHOT"

lazy val root = (project in file(".")).enablePlugins(PlayScala, net.litola.SassPlugin).settings(
  sassOptions := Seq("--compass")
)

scalaVersion := "2.11.1"

libraryDependencies ++= Seq(
  jdbc,
  anorm,
  cache,
  ws,
  "com.sendgrid" % "sendgrid-java" % "2.2.2",
  "com.logentries" % "logentries-appender" % "1.1.30"
)