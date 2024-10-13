import scala.collection.Seq

// ARTIFACTS or DEPENDENCIES
lazy val artifacts = new {

  // artifacts versions
  val scalaV            = "2.12.15"
  val bankV             = "0.1.0-SNAPSHOT"
  val akkaActorV        = "2.5.23"
  val akkaHttpV         = "10.1.8"
  val akkaHttpTestkitV  = "10.1.8"
  val scalaLoggingV     = "3.9.2"
  val logbackV          = "1.2.3"
  val scalaTestV        = "3.0.8"
  val pureconfigV       = "0.11.1"

  // artifacts
  val akkaHttp = Seq(
    "com.typesafe.akka" %% "akka-actor" % akkaActorV,
    "com.typesafe.akka" %% "akka-stream" % akkaActorV,
    "com.typesafe.akka" %% "akka-http" % akkaHttpV,
    "com.typesafe.akka" %% "akka-http-spray-json" % akkaHttpV,
    "com.typesafe.akka" %% "akka-testkit" % akkaActorV % "test",
    "com.typesafe.akka" %% "akka-http-testkit" % akkaHttpTestkitV % "test",
    "com.typesafe.akka" %% "akka-persistence" % "2.6.20",
    "com.typesafe.akka" %% "akka-persistence-query" % "2.6.20"

  )

  val scalaLogging = Seq(
    "com.typesafe.scala-logging" %% "scala-logging" % scalaLoggingV,
    "ch.qos.logback" % "logback-classic" % logbackV
  )

  val scalatest = Seq("org.scalatest" %% "scalatest" % scalaTestV % "test")
  val pureconfig = Seq("com.github.pureconfig" %% "pureconfig" % pureconfigV)
}


// SETTINGS
lazy val commonSettings = Seq(
  organization          := "com.bank.event",
  scalaVersion          := artifacts.scalaV,
  version               := artifacts.bankV,
  logLevel              := Level.Info,
  scalacOptions ++= Seq(
    "-encoding", "UTF-8",
    "-Xfatal-warnings",
    "-deprecation",
    "-feature",
    "-unchecked",
    "-language:implicitConversions",
    "-language:higherKinds",
    "-language:existentials",
    "-language:postfixOps",
    "-Ywarn-dead-code"
  )
)

lazy val testSettings = Seq(
  fork in Test := false,
  parallelExecution in Test := false,
  libraryDependencies ++= artifacts.scalatest
)

// PROJECT or PROJECTS
lazy val donutStore = (project in file("."))
  .settings(name:= "simple-bank-system")
  .settings(commonSettings: _*)
  .settings(testSettings: _*)
  .settings(libraryDependencies ++= artifacts.scalaLogging ++ artifacts.akkaHttp ++ artifacts.pureconfig)
  .settings(resolvers += Resolver.sonatypeRepo("releases"))
  .settings(resolvers += Resolver.sonatypeRepo("snapshots"))