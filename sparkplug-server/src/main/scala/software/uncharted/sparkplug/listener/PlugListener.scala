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
import akka.stream.scaladsl.Source
import io.scalac.amqp.Connection

import scala.concurrent.ExecutionContext.Implicits.global

import scala.concurrent.Future

class PlugListener private() {
  var connection: Option[Connection] = None
  var connected: Boolean = false

  implicit val system = ActorSystem("QuickStart")
  implicit val materializer = ActorMaterializer()

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
    val res: Future[Unit] = Source.fromPublisher(connection.get.consume("q_sparkplug"))
        .runForeach(println(_))

    Console.out.println("Waiting for completion.")
    res.onComplete((Unit) => Console.out.println("All futures finished."))

    Console.out.println("And we're done.")
  }

  def isConnected: Boolean = {
    connected
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
