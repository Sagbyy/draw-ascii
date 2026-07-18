scalaVersion := "3.8.4"

lazy val root = rootProject
  .settings(
    name := "draw-ascii",
    libraryDependencies ++= Seq(
      "org.scalameta" %% "munit" % "1.2.3" % Test
    )
  )
