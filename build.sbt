import sbt._
import sbt.Keys._
import sbtrelease.ReleasePlugin.autoImport._
import sbtcrossproject.CrossProject
import sbtcrossproject.CrossType

val Org = "org.scoverage"
val ScalatestVersion = "3.1.1"

val bin212 = Seq("2.12.13", "2.12.12", "2.12.11", "2.12.10")
val bin213 = Seq("2.13.5", "2.13.4", "2.13.3", "2.13.2", "2.13.1", "2.13.0")

val appSettings = Seq(
    organization := Org,
    scalaVersion := "2.12.13",
    crossScalaVersions := bin212 ++ bin213,
    crossVersion := CrossVersion.full,
    crossTarget := target.value / s"scala-${scalaVersion.value}",
    Test / fork := false,
    publishMavenStyle := true,
    Test / publishArtifact := false,
    Test / parallelExecution := false,
    scalacOptions := Seq("-unchecked", "-deprecation", "-feature", "-encoding", "utf8"),
    Global / concurrentRestrictions += Tags.limit(Tags.Test, 1),
    publishTo := {
      if (isSnapshot.value)
        Some("snapshots" at "https://oss.sonatype.org/content/repositories/snapshots")
      else
        Some("releases" at "https://oss.sonatype.org/service/local/staging/deploy/maven2")
    },
    libraryDependencies ++= Seq(
      "org.scala-lang" % "scala-compiler" % scalaVersion.value % Compile
    ),
    pomExtra := {
      <url>https://github.com/scoverage/scalac-scoverage-plugin</url>
        <licenses>
          <license>
            <name>Apache 2</name>
            <url>http://www.apache.org/licenses/LICENSE-2.0</url>
            <distribution>repo</distribution>
          </license>
        </licenses>
        <scm>
          <url>git@github.com:scoverage/scalac-scoverage-plugin.git</url>
          <connection>scm:git@github.com:scoverage/scalac-scoverage-plugin.git</connection>
        </scm>
        <developers>
          <developer>
            <id>sksamuel</id>
            <name>Stephen Samuel</name>
            <url>http://github.com/sksamuel</url>
          </developer>
          <developer>
            <id>gslowikowski</id>
            <name>Grzegorz Slowikowski</name>
            <url>http://github.com/gslowikowski</url>
          </developer>
        </developers>
    },
    pomIncludeRepository := {
      _ => false
    }
  ) ++ Seq(
    releaseCrossBuild := true,
    releasePublishArtifactsAction := PgpKeys.publishSigned.value
  )

lazy val root = Project("scalac-scoverage", file("."))
    .settings(name := "scalac-scoverage")
    .settings(appSettings: _*)
    .settings(publishArtifact := false)
    .settings(publishLocal := {})
    .aggregate(plugin, runtime.jvm, runtime.js)

lazy val runtime = CrossProject("scalac-scoverage-runtime", file("scalac-scoverage-runtime"))(JVMPlatform, JSPlatform)
    .crossType(CrossType.Full)
    .withoutSuffixFor(JVMPlatform)
    .settings(name := "scalac-scoverage-runtime")
    .settings(appSettings: _*)
    .settings(
      libraryDependencies += "org.scalatest" %%% "scalatest" % ScalatestVersion % Test
    )
    .jvmSettings(
      Test / fork := true
    )
    .jsSettings(
      crossVersion := CrossVersion.fullWith("sjs" + scalaJSVersion.take(1) + "_", ""),
      scalaJSStage := FastOptStage
    )

lazy val `scalac-scoverage-runtimeJVM` = runtime.jvm
lazy val `scalac-scoverage-runtimeJS` = runtime.js

lazy val plugin = Project("scalac-scoverage-plugin", file("scalac-scoverage-plugin"))
    .dependsOn(`scalac-scoverage-runtimeJVM` % Test)
    .settings(name := "scalac-scoverage-plugin")
    .settings(appSettings: _*)
    .settings(
      libraryDependencies ++= Seq(
        "org.scala-lang.modules" %% "scala-xml" % "1.2.0",
        "org.scalatest" %% "scalatest" % ScalatestVersion % Test
      )
    )
  .settings(
    (Test/ unmanagedSourceDirectories) += (Test / sourceDirectory).value / "scala-2.12+"
  )

