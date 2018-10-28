package net.sanori.spark

import java.nio.file.Paths

import org.apache.log4j.{Level, Logger}
import org.apache.spark.SparkContext
import org.apache.spark.sql._
import org.scalatest.{BeforeAndAfterAll, FunSpec}

// Base class to test Spark SQL related functions

abstract class SparkSqlSpec extends FunSpec with BeforeAndAfterAll {
  protected var spark: SparkSession = _
  protected var sc: SparkContext = _

  def fsPath(resource: String): String =
    Paths.get(this.getClass.getResource(resource).toURI).toString

  override def beforeAll() = {
    super.beforeAll
    Logger.getLogger("org.apache").setLevel(Level.WARN)

    spark = SparkSession.builder
      .master("local[*]")
      .getOrCreate
    sc = spark.sparkContext
  }

  override def afterAll() = {
    if (spark != null) spark.stop()
    super.afterAll
  }
}
