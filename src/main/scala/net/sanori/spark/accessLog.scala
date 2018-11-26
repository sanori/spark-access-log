package net.sanori.spark

import java.sql.Timestamp
import java.text.ParseException
import java.time._
import java.time.format._

import org.apache.log4j.Logger
import org.apache.spark.sql.expressions.UserDefinedFunction
import org.apache.spark.sql.functions._

import scala.util.Try
import scala.util.matching.Regex

/** Collections of functions and a case class for access.log parser.
 *
 * One can parse access.log text lines using `map` of RDD as follows:
 * {{{
 *   import net.sanori.spark.accessLog.toCombinedLog
 *
 *   val logRdd = sc.textFile("access.log").map(toCombinedLog)
 * }}}
 *
 * or `map` of Dataset:
 * {{{
 *   import net.sanori.spark.accessLog.toCombinedLog
 *
 *   val logDs = spark.read.textFile("access.log").map(toCombinedLog)
 * }}}
 *
 * One can use `to_combined` which is Spark UDF of `toCombinedLog` as follows:
 * {{{
 *   import net.sanori.spark.accessLog.to_combined
 *   import org.apache.spark.sql.functions._
 *
 *   val logDf = spark.read.text("access.log")
 *     .select(to_combined(col("value")).as("log"))
 *     .select(col("log.*"))
 * }}}
 */

object accessLog {

  /** Parsed output row of access.log
   *
   * Combined log format.
   * If the input log is Common log format,
   * httpReferer and httpUserAgent is provided as null string.
   *
   * @param remoteAddr    The IP or hostname of remote host
   * @param remoteUser    User's ID if provided
   * @param time          The time of the event
   * @param request       HTTP request string
   * @param status        Status code of HTTP response
   * @param bytesSent     Number of bytes of HTTP response body
   * @param httpReferer   Referer header value in HTTP request
   * @param httpUserAgent User-Agent header value in HTTP request
   */
  case class CombinedLog(remoteAddr: String = "",
                         remoteUser: String = "",
                         time: Timestamp = new Timestamp(0L),
                         request: String = "",
                         status: String = "",
                         bytesSent: Option[Long] = None,
                         httpReferer: String = "",
                         httpUserAgent: String = "")

  protected val log: Logger = Logger.getLogger(this.getClass.getName)

  protected val accessLogPattern: Regex = ("^([^ ]+) [^ ]+ ([^ ]+)" +
    " \\[([^]]+)\\] \"(.+)\" ([0-9]+) ([0-9-]+)" +
    "(?: \"([^\"]+)\" \"([^\"]+)\")?").r

  /** DateTimeFormatter for strftime,
   * which is the default time format of access.log.
   *
   */
  protected val strftimeFormat: DateTimeFormatter =
    DateTimeFormatter.ofPattern("dd/MMM/yyyy:HH:mm:ss ZZ")

  /** Returns a CombinedLog struct which is the result of parsing text `line`.
   *
   * @param line A line of access.log
   * @return parsed CombinedLog
   */
  def toCombinedLog(line: String): CombinedLog = {
    line match {
      case accessLogPattern(host, uid, dateTimeStr, request,
      status, bytesSent, referrer, userAgent) =>
        CombinedLog(host, if (uid == "-") "" else uid,
          Timestamp.from(
            OffsetDateTime.parse(dateTimeStr, strftimeFormat).toInstant
          ),
          request, status, Try(bytesSent.toLong).toOption,
          if (referrer == "-") "" else referrer, userAgent)
      case _ => {
        log.error("Regex match failed: '" + line + "'")
        throw new ParseException("Not an access.log: '" + line + "'", 0)
      }
    }
  }

  /** UDF format of toCombinedLog
   *
   * {{{
   *   to_combined(text: Column): Column
   * }}}
   */
  val to_combined: UserDefinedFunction = udf[CombinedLog, String](toCombinedLog)
}
