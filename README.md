# access.log parser for Spark SQL
Simple HTTPd log (a.k.a. access.log) parser for Spark SQL.

Currently, [Combined](https://httpd.apache.org/docs/2.4/en/logs.html#combined)
and
[Common](https://en.wikipedia.org/wiki/Common_Log_Format)
log formats are supported.

## How to use

### SQL (spark-sql)
When start spark-sql, include jar file of this project.
```sh
spark-sql --jars accesslog_2.11-0.1.0-SNAPSHOT.jar
```

In SQL, you can create user defined function and use it:
```sql
-- attach ToCombined as to_combined(text_line)
CREATE OR REPLACE FUNCTION to_combined
AS "net.sanori.spark.ToCombined";

-- read raw log file as one column table
CREATE OR REPLACE TEMP VIEW accessLogText
USING text
OPTIONS (path "access.log");

-- create parsed log as a table
CREATE OR REPLACE TEMP VIEW accessLog
AS SELECT log.*
    FROM (
        SELECT to_combined(value) AS log
        FROM accessLogText
    )
```

### Spark SQL in Scala
```scala
import net.sanori.spark.accessLog.{to_combined, CombinedLog}
import org.apache.spark.sql.functions._

val lineDs = spark.read.textFile("access.log")
val logDs = lineDs
  .select(to_combined(col("value")).as("log"))
  .select(col("log.*"))
  .as[CombinedLog]
```

### RDD in Scala
```scala
import net.sanori.spark.accessLog.toCombinedLog

val lines = sc.textFile("access.log")
val rdd = lines.map(toCombinedLog)
```

## What is provided
Combined or Common logs are transformed to the table
which has the following meaning:

| name          | type      | default value        |
|---------------|-----------|----------------------|
| remoteAddr    | String    | ""                   |
| remoteUser    | String    | ""                   |
| time          | Timestamp | 1970-01-01T00:00:00Z |
| request       | String    | ""                   |
| status        | String    | ""                   |
| bytesSent     | Long      | null                 |
| httpReferer   | String    | ""                   |
| httpUserAgent | String    | ""                   |


## Other information

### How to build
```
sbt clean package
```
generates `accesslog_2.11-0.1.0-SNAPSHOT.jar` in `target/scala-2.11`.

### Motivation
 * To simplify analysis of web server logs
 * Most of the logs of web server, that is HTTP server, are in Combined or Common log format.
 * To make user defined function that can be used on spark-sql command

### Alternative
If you want to view access.log as a table on Hive, not on Spark,
or want to process various log formats,
[nielsbasjes/logparser](https://github.com/nielsbasjes/logparser/)
might be better solution.

### Contribution
Suggestions, idea, comments, pull requests are welcome.
 * https://github.com/sanori/spark-access-log/issues
