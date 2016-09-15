name := "mnist"

organization := "co.quine"

version := "0.1.0"

scalaVersion := "2.11.8"

isSnapshot := true

publishTo := Some("Quine snapshots" at "s3://snapshots.repo.quine.co")

resolvers ++= Seq[Resolver](
  "Quine Releases"                    at "s3://releases.repo.quine.co",
  "Quine Snapshots"                   at "s3://snapshots.repo.quine.co",
  "Local Maven"                       at Path.userHome.asFile.toURI.toURL + ".m2/repository",
  "Typesafe repository snapshots"     at "http://repo.typesafe.com/typesafe/snapshots/",
  "Typesafe repository releases"      at "http://repo.typesafe.com/typesafe/releases/",
  "Sonatype repo"                     at "https://oss.sonatype.org/content/groups/scala-tools/",
  "Sonatype releases"                 at "https://oss.sonatype.org/content/repositories/releases",
  "Sonatype snapshots"                at "https://oss.sonatype.org/content/repositories/snapshots",
  "Sonatype staging"                  at "http://oss.sonatype.org/content/repositories/staging",
  "Sonatype release Repository"       at "http://oss.sonatype.org/service/local/staging/deploy/maven2/",
  "Java.net Maven2 Repository"        at "http://download.java.net/maven/2/",
  "Twitter Repository"                at "http://maven.twttr.com"
)

resolvers += Resolver.jcenterRepo

lazy val commonScalacOptions = Seq(
  "-encoding", "UTF-8",
  "-language:postfixOps",
  "-language:higherKinds",
  "-language:existentials",
  "-language:implicitConversions",
  "-language:experimental.macros"
)

scalacOptions ++= commonScalacOptions

classpathTypes += "maven-plugin"

addCompilerPlugin("com.milessabin" % "si2712fix-plugin_2.11.8" % "1.2.0")

lazy val versions = new {
  val cats = "0.7.2"
  val fs2 = "0.9.0-RC2"
  val fs2Cats = "0.1.0-RC2"
}

lazy val functionalibs = Seq(
  "org.typelevel" %% "cats"      % versions.cats
)

lazy val streamlibs = Seq(
  "co.fs2"  %% "fs2-core" % versions.fs2,
  "co.fs2"  %% "fs2-io"   % versions.fs2,
  "co.fs2"  %% "fs2-cats" % versions.fs2Cats
)

libraryDependencies ++= (functionalibs ++ streamlibs)

evictionWarningOptions in update := EvictionWarningOptions.default
  .withWarnTransitiveEvictions(false)
  .withWarnDirectEvictions(false)
  .withWarnScalaVersionEviction(false)