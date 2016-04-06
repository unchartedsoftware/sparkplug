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
import akka.stream.{ActorMaterializer, OverflowStrategy}
import akka.stream.scaladsl.Source
import io.scalac.amqp.Connection

class PlugListener private() {
  var connection: Option[Connection] = None
  var connected: Boolean = false

  implicit val system = ActorSystem("SparkPlug")
  implicit val materializer = ActorMaterializer()

  val inFlightIds = scala.collection.mutable.SortedSet[String]()

  @throws(classOf[PlugListenerException])
  def connect(): PlugListener = {
    if (!connected) {
      try {
        connection = Some(Connection())
        connected = true
      } catch {
        case e: Exception =>
          Console.err.println(s"Could not connect to RabbitMQ: $e")
          throw new PlugListenerException("Could not connect to RabbitMQ.", e)
      }
    }
    this
  }

  def end(): PlugListener = {
    Console.out.println(s"Checking if connected: $connected; disconnecting if we are.")

    if (connected) {
      Console.out.println("Shutting down PlugListener.")
      try {
        materializer.shutdown()
        system.shutdown()
        connection.get.shutdown()
        Console.out.println("PlugListener shutdown.")
      } catch {
        case e: Exception =>
          Console.err.println(s"Could not disconnect from RabbitMQ: $e")
      }
    }
    this
  }

  def consume(): Unit = {
    Console.out.println("Consuming.")
    val size: Int = 50
    Source.fromPublisher(connection.get.consume("q_sparkplug"))
        .buffer(size, OverflowStrategy.backpressure)
      .filterNot(p => {
        val cId = p.message.correlationId.get
        val exists = inFlightIds(cId)
        // Console.out.println(s"Checking to see if in-flight IDs contains $cId: $exists")
        if (!exists) {
          // Console.out.println(s"In-flight IDs did not contain $cId - adding and working.")
          inFlightIds += cId
        }
        exists
      })
      .runForeach(p => {
        val cId = p.message.correlationId.get
        // Console.out.println(p)

        // Console.out.println(s"Cleaning up $cId from in-flight IDs.")
        inFlightIds.remove(cId)
      })
    Console.out.println("Consumption completed.")
  }

  def isConnected: Boolean = {
    connected
  }

  def getConnection: Connection = {
    connection.get
  }
}

object PlugListener {
  private var instance: Option[PlugListener] = None

  def getInstance(): PlugListener = {
    if (instance.isEmpty) {
      instance = Some(new PlugListener())
    }
    instance.get
  }
}
