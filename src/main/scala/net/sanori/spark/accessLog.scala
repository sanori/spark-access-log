package net.sanori.spark

import java.sql.Timestamp
import java.text.ParseException
import java.time._
import java.time.format._

import org.apache.spark.sql.expressions.UserDefinedFunction
import org.apache.spark.sql.functions._

import scala.util.Try
import scala.util.matching.Regex

object accessLog {

  case class CombinedLog(remoteAddr: String = "",
                         remoteUser: String = "",
                         time: Timestamp = new Timestamp(0L),
                         request: String = "",
                         status: String = "",
                         bytesSent: Option[Long] = None,
                         httpReferer: String = "",
                         httpUserAgent: String = "")

  val accessLogPattern: Regex = ("^([^ ]+) [^ ]+ ([^ ]+)" +
    " \\[([^]]+)\\] \"([^\"]+)\" ([^ ]+) ([^ ]+)" +
    "(?: \"([^\"]+)\" \"([^\"]+)\")?").r

  val strftimeFormat: DateTimeFormatter =
    DateTimeFormatter.ofPattern("dd/MMM/yyyy:HH:mm:ss ZZ")

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
      case _ => throw new ParseException("Not an access.log", 0)
    }
  }

  val to_combined: UserDefinedFunction = udf[CombinedLog, String](toCombinedLog)
}
