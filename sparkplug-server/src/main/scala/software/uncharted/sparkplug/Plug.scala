/*
 * Copyright 2015-2016 Uncharted Software Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package software.uncharted.sparkplug

import com.typesafe.config.ConfigFactory
import org.apache.spark.SparkContext
import org.apache.spark.SparkConf
import software.uncharted.sparkplug.listener.PlugListener

class Plug {
  var sc: Option[SparkContext] = None
  var listener: Option[PlugListener] = None

  def main(args: Array[String]) {
    run()
  }

  def run(): Boolean = {
    val config = ConfigFactory.load()
    val master = config.getString("sparkplug.master")

    val conf = new SparkConf().setAppName("sparkplug").setMaster(master)
    sc = Some(new SparkContext(conf))

    println("Connected to Spark.")

    println("Connecting to RabbitMQ.")
    listener = Some(PlugListener.getInstance())
    listener.get.connect()

    println("Kicking off consume.")
    listener.get.consume()

    println("Kicked off consume.")
    true
  }

  def shutdown(): Unit = {
    Console.out.println("Shutting down.")
    listener.get.end()
  }
}
