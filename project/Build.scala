import sbt._
import Keys._

object AsyncCrawlerBuild extends Build {

  lazy val core = Project (
     id = "core"
    ,base = file("core")
  )

  lazy val jsoup = Project (
     id = "jsoup"
    ,base = file("jsoup")
  ) dependsOn core

  lazy val filedl = Project (
     id = "filedl"
    ,base = file("filedl")
  ) dependsOn core

  lazy val json4s = Project (
     id = "json4s"
    ,base = file("json4s")
  ) dependsOn core
}
