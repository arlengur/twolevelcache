import sbt._

object Dependencies {
  lazy val scalaTest      = "org.scalatest"         %% "scalatest"      % "3.0.8"
  lazy val logbackclassic = "ch.qos.logback"        % "logback-classic" % "1.2.3"
  lazy val log4s          = "org.log4s"             %% "log4s"          % "1.8.2"
  lazy val pureConfig     = "com.github.pureconfig" %% "pureconfig"     % "0.11.1"

}
