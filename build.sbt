lazy val commonSettings = Seq(
  version := "0.0.1",
  scalaVersion := "2.12.2"
)

lazy val root = (project in file(".")).
  settings(commonSettings: _*).
  settings(
    name := "OpenSSLWrapper Project",
    scalacOptions += "-deprecation"
  ).aggregate(sample, opensslwrapper, opensslwrapperGUI)

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
      "com.github.mickey305"      % "Jclic"                 % "0.1.5_15",
      "com.typesafe.akka"         %% "akka-actor"           % "2.4.12"
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
      "com.typesafe"              % "config"                % "1.3.1",
      "com.github.mickey305"      % "Jclic"                 % "0.1.5_15",
      "com.github.mickey305"      % "FileCompressFramework" % "0.0.2-SNAPSHOT",
      // https://mvnrepository.com/artifact/commons-io/commons-io
      "commons-io"                % "commons-io"            % "2.5",
      // https://mvnrepository.com/artifact/org.apache.commons/commons-lang3
      "org.apache.commons"        % "commons-lang3"         % "3.5"
    )
  )

lazy val opensslwrapperGUI = (project in file("opensslwrapperGUI")).
  dependsOn(opensslwrapper).
  settings(commonSettings: _*).
  settings(
    name := "OpenSSLWrapperGUI",
    scalacOptions += "-deprecation",
    resolvers ++= Seq(
      "Typesafe Repository" at "http://repo.typesafe.com/typesafe/releases/",
      "jitpack" at "https://jitpack.io",
      Resolver.sonatypeRepo("releases")
    ),
    libraryDependencies ++= Seq(
      "org.scalafx"               %% "scalafx"                   % "8.0.102-R11",
      "org.scalafx"               % "scalafxml-core-sfx8_2.12"   % "0.4",
      "org.scala-lang.modules"    % "scala-java8-compat_2.12"    % "0.8.0",
      "com.typesafe"              % "config"                     % "1.3.1",
      "com.github.mickey305"      % "Jclic"                      % "0.1.5_15",
      "com.github.mickey305"      % "FileCompressFramework"      % "0.0.2-SNAPSHOT",
      // https://mvnrepository.com/artifact/commons-io/commons-io
      "commons-io"                % "commons-io"                 % "2.5",
      // https://mvnrepository.com/artifact/org.apache.commons/commons-lang3
      "org.apache.commons"        % "commons-lang3"              % "3.5",
      // https://mvnrepository.com/artifact/org.apache.poi/poi
      "org.apache.poi"            % "poi"                        % "3.16",
      // https://mvnrepository.com/artifact/org.apache.poi/poi-ooxml-schemas
      "org.apache.poi"            % "poi-ooxml-schemas"          % "3.16",
      // https://mvnrepository.com/artifact/org.apache.poi/poi-ooxml
      "org.apache.poi"            % "poi-ooxml"                  % "3.16",
      // https://mvnrepository.com/artifact/org.apache.xmlbeans/xmlbeans
      "org.apache.xmlbeans"       % "xmlbeans"                   % "2.6.0"
    ),
    addCompilerPlugin(
      "org.scalamacros"           % "paradise"                   % "2.1.0" cross CrossVersion.full
    )
  )