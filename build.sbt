lazy val commonSettings = Seq(
  version := "0.0.1",
  scalaVersion := "2.12.2"
)

lazy val root = (project in file(".")).
  settings(commonSettings: _*).
  settings(
    name := "OpenSSLWrapper Root Project",
    scalacOptions += "-deprecation"
  ).aggregate(sample, opensslwrapper)

lazy val sample = (project in file("sample")).
  dependsOn(opensslwrapper).
  settings(commonSettings: _*).
  settings(
    name := "Sample",
    scalacOptions += "-deprecation",
    resolvers ++= Seq(
      "jitpack" at "https://jitpack.io"
    ),
    libraryDependencies ++= Seq(
      "com.github.mickey305" % "Jclic" % "0.1.5_12"
    )
  )

lazy val opensslwrapper = (project in file("opensslwrapper")).
  settings(commonSettings: _*).
  settings(
    name := "OpenSSLWrapper",
    scalacOptions += "-deprecation",
    resolvers ++= Seq(
      "Typesafe Repository" at "http://repo.typesafe.com/typesafe/releases/",
      "jitpack" at "https://jitpack.io"
    ),
    libraryDependencies ++= Seq(
      "com.typesafe" % "config" % "1.3.1",
      "com.github.mickey305" % "Jclic" % "0.1.5_12",
      "com.github.mickey305" % "FileCompressFramework" % "0.0.2-SNAPSHOT",
      // https://mvnrepository.com/artifact/commons-io/commons-io
      "commons-io" % "commons-io" % "2.5"
    )
  )
