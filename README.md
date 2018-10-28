# access.log parser for Spark SQL
Simple HTTPd log (a.k.a. access.log) parser for Spark SQL.

Currently, Combined and Common Log format is supported.

## How to use

### Spark SQL in Scala
```scala
import net.sanori.spark.accessLog._
import org.apache.spark.sql.functions._

val lineDf = spark.read.textFile("access.log")
val logDf = lineDf
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


