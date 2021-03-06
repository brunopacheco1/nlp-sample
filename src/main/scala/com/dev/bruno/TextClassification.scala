package com.dev.bruno

import com.johnsnowlabs.nlp.{DocumentAssembler, Finisher}
import com.johnsnowlabs.nlp.annotators.Tokenizer
import com.johnsnowlabs.nlp.annotators.sbd.pragmatic.SentenceDetector
import org.apache.spark.SparkConf
import org.apache.spark.ml.Pipeline
import org.apache.spark.ml.classification.{LogisticRegression, OneVsRest}
import org.apache.spark.ml.feature.{HashingTF, IDF, StopWordsRemover, StringIndexer}
import org.apache.spark.sql.SparkSession

object TextClassification {
  def main(args: Array[String]): Unit = {
    val conf = new SparkConf()
      .setAppName("LinearRegression")
      .setMaster("local[*]")

    val spark = SparkSession.builder.config(conf).getOrCreate

    var texts = spark.read
      .option("header", "true") // The CSV file has header and use them as column names
      .option("inferSchema", "true").csv("./texts.csv")

    texts = texts.filter("text is not null and topic is not null")

    texts.show()

    val Array(train, test) = texts.randomSplit(Array(0.8, 0.2), 123)

    val vocabSize = 500

    val documentAssembler = new DocumentAssembler()
      .setInputCol("text")
      .setOutputCol("document")

    val sentenceDetector = new SentenceDetector()
      .setInputCols(Array("document"))
      .setOutputCol("sentence")

    val regexTokenizer = new Tokenizer()
      .setInputCols(Array("sentence"))
      .setOutputCol("token")

    val finisher = new Finisher()
      .setInputCols(Array("token"))
      .setOutputCols(Array("tokens"))
      .setOutputAsArray(true)
      .setIncludeKeys(true)

    val stopWordsRemover = new StopWordsRemover()
      .setInputCol("tokens")
      .setOutputCol("cleantokens")
      .setStopWords(StopWordsRemover.loadDefaultStopWords("english"))

    val hashingtf = new HashingTF()
      .setInputCol("cleantokens")
      .setOutputCol("tf")
      .setNumFeatures(vocabSize)

    val idf = new IDF()
      .setInputCol("tf")
      .setOutputCol("features")

    val labelIndexer = new StringIndexer()
      .setInputCol("topic")
      .setOutputCol("label")

    val lr = new LogisticRegression().setMaxIter(100).setRegParam(0.001)

    val ovr = new OneVsRest().setClassifier(lr)

    val pipeline = new Pipeline()
      .setStages(Array(
        documentAssembler,
        sentenceDetector,
        regexTokenizer,
        finisher,
        stopWordsRemover,
        hashingtf,
        idf,
        labelIndexer,
        ovr
      ))

    val model = pipeline.fit(train)

    val txTrain = model.transform(train)

    val txTest = model.transform(test)

    val trainDF = txTrain.select("title", "label", "prediction")

    trainDF.show(1000)

    val testDF = txTest.select("title", "label", "prediction")

    testDF.show(1000)

    spark.close()
  }
}