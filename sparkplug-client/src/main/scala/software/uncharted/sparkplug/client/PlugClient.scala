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
package software.uncharted.sparkplug.client

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.{Sink, Source}
import com.typesafe.config.{Config, ConfigFactory}
import grizzled.slf4j.Logging
import io.scalac.amqp.{Connection, Message}
import software.uncharted.sparkplug.model.{PlugMessage, PlugResponse}

/**
  * Dirt simple way of interacting with a Spark cluster using sparkplug
  */
class PlugClient private(config: Config) extends Logging {
  private var connection: Option[Connection] = None
  private var connected: Boolean = false

  private val outboundMessages = new scala.collection.mutable.Queue[Message]

  private var handler: Option[PlugResponseHandler] = None

  private implicit val system = ActorSystem("SparkPlugClient")
  private implicit val materializer = ActorMaterializer()

  /**
    * Connect to the RabbitMQ server and wire together the akka streams magic
    *
    * @return This
    */
  def connect(): PlugClient = {
    info(s"Checking if PlugClient is connected: $connected; connecting if we are not.")

    if (!connected) {
      info("Connecting PlugClient to RabbitMQ.")

      try {
        connection = Some(Connection())
        connected = true

        info("PlugClient connected to RabbitMQ.")
      } catch {
        case e: Exception =>
          warn(s"Could not connect to RabbitMQ: ${e.getMessage}", e)
          throw new Exception("Could not connect to RabbitMQ.", e)
      }

      info("Wiring together the inbound/outbound streams.")

      val outboundSource = Source.fromIterator(() => outboundMessages.iterator)
      val outboundPublisher = connection.get.publishDirectly(config.getString("sparkplug.outbound-queue"))
      val outboundSink = Sink.fromSubscriber(outboundPublisher)
      outboundSource.to(outboundSink)

      val inboundSource = Source.fromPublisher(connection.get.consume(config.getString("sparkplug.inbound-queue")))
      inboundSource.runForeach(delivery => {
        if (handler.isDefined) handler.get.onMessage(PlugResponse.fromMessage(delivery.message))
      })

      info("Inbound/outbound streams wired together.")
    }
    this
  }

  def shutdown(): PlugClient = {
    info(s"Checking if connected: $connected; disconnecting if we are.")

    if (connected) {
      info("Shutting down PlugClient.")
      try {
        materializer.shutdown()
        system.shutdown()
        connection.get.shutdown()
        info("PlugClient shutdown.")
      } catch {
        case e: Exception =>
          warn(s"Could not disconnect from RabbitMQ: $e")
      }
    }
    this
  }

  /**
    * Bind a handler for the response messages; you really should do this *before* you connect, otherwise messages could be lost.
    *
    * @param handler The handler to notify when messages come back
    */
  def setHandler(handler: PlugResponseHandler): Unit = {
    this.handler = Some(handler)
  }

  /**
    * Send a message to the sparkplug server
    *
    * @param message The message to send
    */
  def sendMessage(message: PlugMessage): Unit = {
    if (!connected) {
      warn("Not connected to RabbitMQ, cannot send message.")
      throw new Exception("Not connected to RabbitMQ, cannot send message.")
    }

    outboundMessages.enqueue(message.toMessage)
  }

  def isConnected: Boolean = {
    connected
  }

  def getConnection: Option[Connection] = {
    connection
  }
}

object PlugClient extends Logging {
  private var instance: Option[PlugClient] = None
  private var config: Config = ConfigFactory.load()

  def setConfig (newConfig: Config): Unit = {
    if (instance.isDefined) {
      warn("Attempt to set client configuration after client has been launched")
    } else {
      config = newConfig
    }
  }

  def getInstance(): PlugClient = {
    if (instance.isEmpty) {
      instance = Some(new PlugClient(config))
    }
    instance.get
  }
}
