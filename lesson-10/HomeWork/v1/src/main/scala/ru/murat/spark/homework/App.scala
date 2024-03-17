package ru.murat.spark.homework

import org.apache.spark.sql.functions.col
import org.apache.spark.sql.SparkSession
import org.apache.spark.sql.functions._
import ru.murat.spark.homework.model.{TaxiRideProjection, TaxiTime}

import java.util.Properties
import scala.io.StdIn
object App {
  def main(args: Array[String]): Unit = {

    // Создаём SparkSession
    val spark = SparkSession.builder
      .appName("HelloSpark")
      .master("local")
      .getOrCreate()


    println(s"spark.version == ${spark.version}")
    import spark.implicits._

    println("Задание 1")
    val dfTaxiZones = spark.read
                     .format("csv")
                     .option("header","true")
      .option("inferSchema","true")
      .load("src/main/resources/data/taxi_zones.csv")


    dfTaxiZones.show(10,false)

    val dfFactsTaxis = spark.read.parquet("src/main/resources/data/yellow_taxi_jan_25_2018")
    dfFactsTaxis.show(10,false)
    dfFactsTaxis.printSchema()


   val dfPUZones =  dfTaxiZones.join(dfFactsTaxis,dfTaxiZones("LocationId") === dfFactsTaxis("PULocationID"),"inner")


    val dfPUZcnt  = dfPUZones.groupBy("LocationID","Zone").count()

    dfPUZcnt.orderBy($"count".desc).select($"Zone".alias("Zone name"),$"count".alias("Pull ups amount")).limit(3).show

    dfPUZcnt.write.parquet("src/main/resources/data/PU_zones_aggr_count")

    println("Всё\nК заданию 2")
    StdIn.readLine()
    /* ************************* */
    println("Задание 2")

    val taxisFactsTwoDF = spark.read.parquet("src/main/resources/data/yellow_taxi_jan_25_2018")
    taxisFactsTwoDF.printSchema()

    val taxiTimeRdd = taxisFactsTwoDF.rdd

    val taxiTimePU = taxiTimeRdd.map(t => TaxiTime(String.valueOf(t(1)), String.valueOf(t(2)).split(" ")(1).split(":")(0)))
         .map(tt => (tt.hh_pickup, 1))
         .reduceByKey(_ + _)
         .sortBy(_._2, false)

  taxiTimePU.foreach(x => println(x))
  taxiTimePU
  .map(t => new String(t._1 + " " + t._2))
  .saveAsTextFile("src/main/resources/data/hhCallTable")

    println("Всё\nК заданию 2")
    StdIn.readLine()
    /* ************************* */
    println("Задание 3")


    val taxiDF = spark.read.parquet("src/main/resources/data/yellow_taxi_jan_25_2018")
    taxiDF.printSchema()
    val taxiMartDS = taxiDF.as[TaxiRideProjection]
     .groupBy(col("trip_distance"))
      .agg(
        avg("trip_distance").as("avg_trip_distance"),
        max("trip_distance").as("max_trip_distance"),
        min("trip_distance").as("min_trip_distance")
      )
    taxiMartDS.show


    val driver = "org.postgresql.Driver"
    val url = "jdbc:postgresql://localhost:5434/otus"
    val user = "docker"
    val password = "docker"
    val mode = "overwrite"

    val properties = new Properties()
    properties.put("user", user)
    properties.put("password", password)

    // загрузка ds в базу
    taxiMartDS.write.option("driver", driver).jdbc(url, "taxi_rides_destribution_ds", properties)

    println("Вcе задания выполнены")
    StdIn.readLine()
    StdIn.readLine()
    spark.stop()
  }
}

