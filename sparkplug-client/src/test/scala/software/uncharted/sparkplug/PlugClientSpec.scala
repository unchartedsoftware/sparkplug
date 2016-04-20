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

import java.util.UUID

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.{Sink, Source}
import com.google.common.net.MediaType
import com.typesafe.config.ConfigFactory
import io.scalac.amqp.{Message, Queue}
import org.scalatest.concurrent.Eventually
import org.scalatest.{BeforeAndAfter, FunSpec}
import software.uncharted.sparkplug.client.{PlugClient, PlugResponseHandler}
import software.uncharted.sparkplug.model.PlugResponse

import scala.concurrent.{Await, Promise}

// scalastyle:off underscore.import
import org.scalatest.Matchers._

import scala.concurrent.duration._
// scalastyle:on underscore.import

class PlugClientSpec extends FunSpec with BeforeAndAfter with Eventually {
  val plugClient = PlugClient.getInstance()

  implicit val system = ActorSystem("SparkPlugClient-Test")
  implicit val materializer = ActorMaterializer()

  private val conf = ConfigFactory.load()
  private val q_sparkplug: String = conf.getString("sparkplug.inbound-queue")
  private val r_sparkplug: String = conf.getString("sparkplug.outbound-queue")

  before {
    plugClient.connect()

    Console.out.println("Defining queues and cleaning as required.")
    val qDecQ = plugClient.getConnection.get.queueDeclare(Queue(name = q_sparkplug, durable = true))
    Await.result(qDecQ, 5.seconds)

    val rDecQ = plugClient.getConnection.get.queueDeclare(Queue(name = r_sparkplug, durable = true))
    Await.result(rDecQ, 5.seconds)

    val qPurgeQ = plugClient.getConnection.get.queuePurge(q_sparkplug)
    Await.result(qPurgeQ, 5.seconds)

    val rPurgeQ = plugClient.getConnection.get.queuePurge(r_sparkplug)
    Await.result(rPurgeQ, 5.seconds)
  }

  after {
    Console.out.println("After each - stopping PlugClient.")
    plugClient.shutdown()

    materializer.shutdown()
    system.shutdown()
  }

  describe("PlugClient") {
    it("should allow the creation of a PlugClient and run it") {
      val p = Promise[String]()

      Console.out.println("Creating response handler.")
      plugClient.setHandler(new PlugResponseHandler {
        override def onMessage(message: PlugResponse): Unit = {
          Console.out.println(s"Got a response from the server: $message")
          p success "Done"
        }
      })

      plugClient.connect()

      Console.out.println(s"Generating test responses, putting on queue $q_sparkplug")
      val source = Source(1 to 1)
      val subscriber = plugClient.getConnection.get.publishDirectly(q_sparkplug)
      val sink = Sink.fromSubscriber(subscriber)

      source.map(i => {
        val headers = collection.mutable.Map[String, String]()
        headers.put("command", "test")
        headers.put("uuid", UUID.randomUUID().toString)

        new Message(headers = headers.toMap, contentType = Some(MediaType.PLAIN_TEXT_UTF_8), body = "This is a test".getBytes)
      }).runWith(sink)

      Console.out.println("Test messages generated.")

      eventually(timeout(scaled(30.seconds))) {
        val success = Await.result(p.future, 30.seconds)
        success should be ("Done")
      }
    }
  }
}
