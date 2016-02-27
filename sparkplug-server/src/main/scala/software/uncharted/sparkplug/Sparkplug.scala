package software.uncharted.sparkplug

import org.apache.spark.SparkContext
import org.apache.spark.SparkConf

class Sparkplug {
  val conf = new SparkConf().setAppName("sparkplug")
  val sc = new SparkContext(conf)
}
