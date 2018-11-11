package net.sanori.spark

import org.apache.hadoop.hive.ql.udf.generic.GenericUDF
import org.apache.hadoop.hive.serde2.objectinspector.PrimitiveObjectInspector.PrimitiveCategory
import org.apache.hadoop.hive.serde2.objectinspector._
import org.apache.hadoop.hive.serde2.objectinspector.primitive.PrimitiveObjectInspectorFactory

import scala.collection.JavaConverters._

/** Access.log parser in Hive User defined function.
 *
 * For example, `to_combined` function can be defined as follows in spark-sql:
 * {{{
 *   CREATE OR REPLACE FUNCTION to_combined
 *   AS "net.sanori.spark.ToCombined";
 * }}}
 *
 * The `to_combined` function parses the text column in Combined Log format
 * to columns in [[accessLog.CombinedLog]].
 * If the `value` column is in access.log text, one can parse the column
 * as follows:
 * {{{
 *   SELECT to_combined(value) AS log
 *   FROM accessLogText;
 * }}}
 */
class ToCombined extends GenericUDF {
  protected var inputInspector: PrimitiveObjectInspector = _

  override def initialize(arguments: Array[ObjectInspector]):
  StructObjectInspector = {
    assert(arguments.length == 1)
    this.inputInspector = arguments(0).asInstanceOf[PrimitiveObjectInspector]

    val stringOI = PrimitiveObjectInspectorFactory
      .getPrimitiveJavaObjectInspector(PrimitiveCategory.STRING)
    val timestampOI = PrimitiveObjectInspectorFactory
      .getPrimitiveJavaObjectInspector(PrimitiveCategory.TIMESTAMP)
    val longOI = PrimitiveObjectInspectorFactory
      .getPrimitiveJavaObjectInspector(PrimitiveCategory.LONG)

    val outputFieldNames = Seq("remoteAddr", "remoteUser", "time", "request",
      "status", "bytesSent", "httpReferer", "httpUserAgent")
    val outputInspectors = Seq(stringOI, stringOI, timestampOI, stringOI,
      stringOI, longOI, stringOI, stringOI)
      .asInstanceOf[List[ObjectInspector]]

    ObjectInspectorFactory.getStandardStructObjectInspector(
      outputFieldNames.asJava, outputInspectors.asJava
    )
  }

  override def getDisplayString(children: Array[String]): String =
    "to_combined(" + children.mkString(",") + ")"

  override def evaluate(arguments: Array[GenericUDF.DeferredObject]): AnyRef = {
    val input = inputInspector.getPrimitiveJavaObject(arguments(0).get)
    val fields = accessLog.toCombinedLog(input.asInstanceOf[String])
    Array(
      fields.remoteAddr,
      fields.remoteUser,
      fields.time,
      fields.request,
      fields.status,
      fields.bytesSent match {
        case Some(i) => i
        case None => null
      },
      fields.httpReferer,
      fields.httpUserAgent
    )
  }
}
