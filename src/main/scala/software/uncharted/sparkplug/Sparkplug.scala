package software.uncharted.sparkplug

import org.apache.spark.SparkContext
import org.apache.spark.SparkContext._
import org.apache.spark.SparkConf

object Spark {
  val conf = new SparkConf().setAppName("sparkplug")
  val sc = new SparkContext(conf)
}
