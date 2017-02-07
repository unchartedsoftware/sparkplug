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

import com.typesafe.config.{Config, ConfigFactory}
import org.apache.spark.{SparkConf, SparkContext}
import software.uncharted.sparkplug.handler.PlugHandler
import software.uncharted.sparkplug.listener.PlugListener

class Plug private(config: Config) {
  private val master = config.getString("sparkplug.master")

  private val sparkConf: SparkConf = new SparkConf()
  sparkConf.set("spark.eventLog.enabled", "true")
  sparkConf.set("spark.eventLog.dir", "/tmp")

  private var sc: Option[SparkContext] = None
  private var listener: Option[PlugListener] = None

  private var connected: Boolean = false

  def connect(): Unit = {
    println(s"Connecting to spark master: $master")

    val conf = sparkConf.setAppName("sparkplug").setMaster(master)
    sc = Some(new SparkContext(conf))

    println("Connected to Spark.")

    println("Connecting to RabbitMQ.")
    listener = Some(PlugListener.getInstance(sc.get))
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
    if (connected) {
      listener.get.shutdown()
    }
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
  private[sparkplug] var config = ConfigFactory.load()

  /**
    * Set the configuration with which the Plug instance will be created.  This will do nothing if the instance is
    * already created.
    *
    * @param newConfig The configuration to use
    */
  def setConfig (newConfig: Config): Unit = {
    if (instance.isDefined) {
      Console.err.println("Attempt to define plug configuration after instance is created")
    } else {
      config = newConfig
    }
  }

  /**
    * Get the singleton Plug instance
    */
  def getInstance(): Plug = {
    if (instance.isEmpty) {
      instance = Some(new Plug(config))
    }
    instance.get
  }
}
