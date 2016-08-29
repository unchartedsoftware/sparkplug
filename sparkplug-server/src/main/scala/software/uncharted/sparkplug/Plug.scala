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
import org.apache.spark.{SparkConf, SparkContext}
import org.apache.spark.sql.SparkSession
import software.uncharted.sparkplug.handler.PlugHandler
import software.uncharted.sparkplug.listener.PlugListener

class Plug private() {
  private val config = ConfigFactory.load()
  private val master = config.getString("sparkplug.master")

  private val sparkConf: SparkConf = new SparkConf()
  sparkConf.set("spark.eventLog.enabled", "true")
  sparkConf.set("spark.eventLog.dir", "/tmp")

  private var sc: SparkContext = null
  private var sparkSession: SparkSession = null
  private var listener: Option[PlugListener] = None

  private var connected: Boolean = false

  def connect(): Unit = {
    println(s"Connecting to spark master: $master")

    val conf = sparkConf.setAppName("sparkplug").setMaster(master)
    // sc = Some(new SparkContext(conf))
    sparkSession = SparkSession
      .builder()
      .appName("sparkplug")
      .config(conf)
      .getOrCreate()
    sc = sparkSession.sparkContext
    Console.out.println(sparkSession)

    println("Connected to Spark.")

    println("Connecting to RabbitMQ.")
    listener = Some(PlugListener.getInstance(sparkSession))
    listener.get.connect()
    connected = true
    println("Connected to RabbitMQ.")
  }

  def run(): Unit = {
    if (!connected) {
      Console.err.println("Not connected to Spark/RabbitMQ, cannot run - perhaps you need to run `connect()` first?")
      throw new Exception("Not connected to Spark/RabbitMQ, cannot run - perhaps you need to run `connect()` first?")
    }

    println("Kicking off consume.")
    listener.get.run()
    println("Kicked off consume.")
  }

  def shutdown(): Unit = {
    Console.out.println("Shutting down.")
    if (connected) listener.get.shutdown()
  }

  def registerHandler(command: String, handler: PlugHandler) : Unit = {
    if (listener.isDefined) {
      listener.get.registerHandler(command, handler)
    } else {
      Console.err.println("Not connected to Spark/RabbitMQ, cannot add handler - perhaps you need to run `connect()` first?")
      throw new Exception("Not connected to Spark/RabbitMQ, cannot add handler - perhaps you need to run `connect()` first?")
    }
  }

  def unregisterHandler(command: String) : Unit = {
    if (listener.isDefined) {
      listener.get.unregisterHandler(command)
    } else {
      Console.err.println("Not connected to Spark/RabbitMQ, cannot remove handler - perhaps you need to run `connect()` first?")
      throw new Exception("Not connected to Spark/RabbitMQ, cannot remove handler - perhaps you need to run `connect()` first?")
    }
  }

  def getListener: PlugListener = {
    if (listener.isDefined) {
      listener.get
    } else {
      Console.err.println("Not connected to Spark/RabbitMQ, cannot retrieve listener - perhaps you need to run `connect()` first?")
      throw new Exception("Not connected to Spark/RabbitMQ, cannot retrieve listener - perhaps you need to run `connect()` first?")
    }
  }
}

object Plug {
  private var instance: Option[Plug] = None

  def getInstance(): Plug = {
    if (instance.isEmpty) {
      instance = Some(new Plug())
    }
    instance.get
  }
}
