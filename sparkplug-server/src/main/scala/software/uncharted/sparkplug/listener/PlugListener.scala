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

package software.uncharted.sparkplug.listener

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.{Flow, Sink, Source}
import com.typesafe.config.Config
import grizzled.slf4j.Logging
import io.scalac.amqp.{Connection, Delivery}
import org.apache.spark.SparkContext
import software.uncharted.sparkplug.Plug
import software.uncharted.sparkplug.handler.PlugHandler
import software.uncharted.sparkplug.model.PlugMessage

class PlugListener private(conf: Config, sparkContext: SparkContext) extends Logging {
  private val handlers = collection.mutable.Map[String, PlugHandler]()

  private var connection: Option[Connection] = None
  private var connected: Boolean = false

  private implicit val system = ActorSystem("SparkPlug")
  private implicit val materializer = ActorMaterializer()

  @throws(classOf[PlugListenerException])
  def connect(): PlugListener = {
    info(s"Checking if PlugListener is connected: $connected; connecting if we are not.")

    if (!connected) {
      info("Connecting PlugListener to RabbitMQ.")

      try {
        connection = Some(Connection())
        connected = true

        info("PlugListener connected to RabbitMQ.")
      } catch {
        case e: Exception =>
          warn(s"Could not connect to RabbitMQ: ${e.getMessage}", e)
          throw new PlugListenerException("Could not connect to RabbitMQ.", e)
      }
    }
    this
  }

  def shutdown(): PlugListener = {
    info(s"Checking if connected: $connected; disconnecting if we are.")

    if (connected) {
      info("Shutting down PlugListener.")
      try {
        materializer.shutdown()
        system.shutdown()
        connection.get.shutdown()
        info("PlugListener shutdown.")
      } catch {
        case e: Exception =>
          warn(s"Could not disconnect from RabbitMQ: ${e.getMessage}", e)
      }
    }
    this
  }

  def registerHandler(command: String, handler: PlugHandler): Unit = {
    handlers.put(command, handler)
  }

  def unregisterHandler(command: String): Unit = {
    handlers.remove(command)
  }

  def run(): Unit = {
    val source = Source.fromPublisher(connection.get.consume(conf.getString("sparkplug.inbound-queue")))
    val sink = Sink.fromSubscriber(connection.get.publishDirectly(conf.getString("sparkplug.outbound-queue")))

    val sparkProcessor = Flow[Delivery].map(delivery => {
      val message = PlugMessage.fromMessage(delivery.message)
      val handler = handlers.get(message.command)
      if (handler.isEmpty) {
        throw new PlugListenerException(s"No handler specified for command $message.command")
      }

      handler.get.onMessage(sparkContext, message)
    })

    source.via(sparkProcessor).to(sink).run()
  }

  def isConnected: Boolean = {
    connected
  }

  def getConnection: Option[Connection] = {
    connection
  }
}

object PlugListener {
  private var instance: Option[PlugListener] = None

  def getInstance(sparkContext: SparkContext): PlugListener = {
    if (instance.isEmpty) {
      instance = Some(new PlugListener(Plug.config, sparkContext))
    }
    instance.get
  }
}
