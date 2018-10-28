# access.log parser for Spark SQL
Simple HTTPd log (a.k.a. access.log) parser for Spark SQL.

Currently, Combined and Common Log format is supported.

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
AS net.sanori.spark.ToCombined;

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
import net.sanori.spark.accessLog._
import org.apache.spark.sql.functions._

val lineDs = spark.read.textFile("access.log")
val logDs = lineDs
  .select(to_combined(col("value")).as("log"))
  .select(col("log.*"))
  .as[CombinedLog]

```

### RDD in Scala
```scala
import net.sanori.spark.accessLog._

val lines = sc.textFile("access.log")
val rdd = lines.map(toCombinedLog)
```


