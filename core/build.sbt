name := "async-crawler-core"

organization := "org.nisshiee"

version := "1.0.0-SNAPSHOT"

scalaVersion := "2.10.2"

libraryDependencies ++= Seq(
   "org.scalaz" %% "scalaz-core" % "7.0.3"
  ,"org.typelevel" %% "scalaz-contrib-210" % "0.1.5"
  ,"com.typesafe.akka" %% "akka-actor" % "2.2.0"
  ,"net.databinder.dispatch" %% "dispatch-core" % "0.11.0"
  ,"org.specs2" %% "specs2" % "1.14" % "test"
  ,"org.typelevel" %% "scalaz-specs2" % "0.1.3" % "test"
  ,"junit" % "junit" % "4.11" % "test"
  ,"org.pegdown" % "pegdown" % "1.2.1" % "test"
)

scalacOptions <++= scalaVersion.map { sv =>
  if (sv.startsWith("2.10")) {
    Seq(
      "-deprecation",
      "-language:dynamics",
      "-language:postfixOps",
      "-language:reflectiveCalls",
      "-language:implicitConversions",
      "-language:higherKinds",
      "-language:existentials",
      "-language:reflectiveCalls",
      "-language:experimental.macros",
      "-Xfatal-warnings"
    )
  } else {
    Seq("-deprecation")
  }
}

testOptions in (Test, test) += Tests.Argument("console", "html", "junitxml")

initialCommands := """
import scalaz._, Scalaz._
import java.nio.charset.Charset
import scala.concurrent.duration._
import org.nisshiee.crawler._
"""

cleanupCommands := """
Crawler.shutdown
"""

// ========== for sonatype oss publish ==========

publishMavenStyle := true

publishTo <<= version { (v: String) =>
  val nexus = "https://oss.sonatype.org/"
  if (v.trim.endsWith("SNAPSHOT"))
    Some("snapshots" at nexus + "content/repositories/snapshots")
  else
    Some("releases"  at nexus + "service/local/staging/deploy/maven2")
}

publishArtifact in Test := false

pomIncludeRepository := { _ => false }

pomExtra := (
  <url>https://github.com/nisshiee/async-crawler</url>
  <licenses>
    <license>
      <name>The MIT License (MIT)</name>
      <url>http://opensource.org/licenses/mit-license.php</url>
      <distribution>repo</distribution>
    </license>
  </licenses>
  <scm>
    <url>git@github.com:nisshiee/async-crawler.git</url>
    <connection>scm:git:git@github.com:nisshiee/async-crawler.git</connection>
  </scm>
  <developers>
    <developer>
      <id>nisshiee</id>
      <name>Hirokazu Nishioka</name>
      <url>http://nisshiee.github.com/</url>
    </developer>
  </developers>)


// ========== for scaladoc ==========

scalacOptions in (Compile, doc) <++= (baseDirectory in LocalProject("core")).map {
  bd => Seq("-sourcepath", bd.getAbsolutePath,
            "-doc-source-url", "https://github.com/nisshiee/async-crawler/blob/master/core€{FILE_PATH}.scala",
            "-implicits", "-diagrams")
}
