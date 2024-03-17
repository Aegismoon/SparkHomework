ThisBuild / version := "1.0"

ThisBuild / scalaVersion := "2.12.18"

lazy val sparkVersion = "3.5.0"
val postgresVersion = "42.7.1"

lazy val root = (project in file("."))
  .settings(
    name := "SparkDataHomeWork"
  )

libraryDependencies ++= Seq(
  "org.apache.spark" %% "spark-sql"% sparkVersion % "provided",
  "org.postgresql" % "postgresql"% postgresVersion,
  "org.scala-lang" % "scala-reflect" % "2.12.8" % "provided"
)
