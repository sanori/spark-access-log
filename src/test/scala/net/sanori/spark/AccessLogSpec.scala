package net.sanori.spark

import java.sql.Timestamp
import java.time._

import org.apache.spark.sql.functions._

class AccessLogSpec extends SparkSqlSpec {
  describe("toCombinedLog") {
    it("parses a line with combined log format") {
      val line = "172.19.0.1 - - [22/Oct/2018:17:34:46 +0000]" +
        " \"GET / HTTP/1.1\" 200 820 \"-\"" +
        " \"Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Ubuntu Chromium/69.0.3497.81 Chrome/69.0.3497.81 Safari/537.36\""
      val actual = accessLog.toCombinedLog(line)
      assert(actual.remoteAddr === "172.19.0.1")
      assert(actual.remoteUser === "")
      assert(actual.time === Timestamp.from(
        Instant.parse("2018-10-22T17:34:46Z")
      ))
      assert(actual.request === "GET / HTTP/1.1")
      assert(actual.status === "200")
      assert(actual.bytesSent === Some(820))
      assert(actual.httpReferer === "")
      assert(actual.httpUserAgent ===
        "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Ubuntu Chromium/69.0.3497.81 Chrome/69.0.3497.81 Safari/537.36"
      )
    }

    it("parses a line with common log format") {
      val line = "127.0.0.1 - - [24/Oct/2018:02:15:36 +0900] \"GET /favicon.ico HTTP/1.1\" 404 -"
      val actual = accessLog.toCombinedLog(line)
      assert(actual.remoteAddr === "127.0.0.1")
      assert(actual.remoteUser === "")
      assert(actual.time === Timestamp.from(
        Instant.parse("2018-10-23T17:15:36Z")
      ))
      assert(actual.request === "GET /favicon.ico HTTP/1.1")
      assert(actual.status === "404")
      assert(actual.bytesSent === None)
    }

    it("parses request path with double quote") {
      val line = "141.102.80.130 - - [13/Jul/1995:19:25:35 -0400] \"GET /history/apollo/apollo.html\" HTTP/1.0\" 404 -"
      val actual = accessLog.toCombinedLog(line)
      assert(actual.remoteAddr === "141.102.80.130")
      assert(actual.remoteUser === "")
      assert(actual.time === Timestamp.from(
        Instant.parse("1995-07-13T23:25:35Z")
      ))
      assert(actual.request === "GET /history/apollo/apollo.html\" HTTP/1.0")
      assert(actual.status === "404")
      assert(actual.bytesSent === None)
    }

    it("works with Spark RDD") {
      val lines = sc.textFile(fsPath("combined.log"))
      val rdd = lines.map(accessLog.toCombinedLog)
      assert(rdd.count === 13)

      val actual = rdd.first
      assert(actual.remoteAddr === "172.19.0.1")
      assert(actual.remoteUser === "")
      assert(actual.time === Timestamp.from(
        Instant.parse("2018-10-22T17:34:46Z")
      ))
      assert(actual.request === "GET / HTTP/1.1")
      assert(actual.status === "200")
      assert(actual.bytesSent === Some(820))
      assert(actual.httpReferer === "")
      assert(actual.httpUserAgent ===
        "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Ubuntu Chromium/69.0.3497.81 Chrome/69.0.3497.81 Safari/537.36"
      )    }
  }

  describe("to_combined") {
    it("works with Spark SQL as UDF") {
      val txtDf = spark.read.textFile(fsPath("combined.log"))
      val logDf = txtDf
        .select(accessLog.to_combined(col("value")).as("log"))
        .select(col("log.*"))
      assert(logDf.count === 13)

      val spark2 = spark
      import spark2.implicits._
      val actual = logDf.as[accessLog.CombinedLog].head
      assert(actual.remoteAddr === "172.19.0.1")
      assert(actual.remoteUser === "")
      assert(actual.time === Timestamp.from(
        Instant.parse("2018-10-22T17:34:46Z")
      ))
      assert(actual.request === "GET / HTTP/1.1")
      assert(actual.status === "200")
      assert(actual.bytesSent === Some(820))
      assert(actual.httpReferer === "")
      assert(actual.httpUserAgent ===
        "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Ubuntu Chromium/69.0.3497.81 Chrome/69.0.3497.81 Safari/537.36"
      )
    }
  }
}
