name := "nlp-sample"

version := "1.0"

scalaVersion := "2.11.11"

libraryDependencies += "org.apache.spark" %% "spark-mllib" % "2.2.1"

libraryDependencies += "org.apache.spark" %% "spark-sql" % "2.2.1"

libraryDependencies += "com.johnsnowlabs.nlp" %% "spark-nlp" % "1.4.0"

libraryDependencies += "junit" % "junit" % "4.4" % "test->default"