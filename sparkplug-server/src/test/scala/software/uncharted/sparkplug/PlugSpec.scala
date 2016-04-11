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
import io.scalac.amqp.{Message, Queue}
import org.apache.spark.SparkContext
import org.scalatest.concurrent.Eventually
import org.scalatest.{BeforeAndAfter, FunSpec, Matchers}
import play.api.libs.json.Json
import software.uncharted.sparkplug.handler.PlugHandler
import software.uncharted.sparkplug.model.{PlugMessage, PlugResponse}

import scala.concurrent.{Await, Future}
import scala.concurrent.ExecutionContext.Implicits.global

// scalastyle:off underscore.import
import scala.concurrent.duration._

import Matchers._
// scalastyle:on underscore.import

class PlugSpec extends FunSpec with BeforeAndAfter with Eventually {
  val plug = new Plug()

  implicit val system = ActorSystem("SparkPlug-Test")
  implicit val materializer = ActorMaterializer()

  before {
    Console.out.println("Before each - populating data and starting plug.")
    val source = Source(1 to 1)
    val subscriber = plug.listener.getConnection.get.publishDirectly("q_sparkplug")
    val sink = Sink.fromSubscriber(subscriber)

    source.map(i => {
      if (i % 1000 == 0) Console.out.println(s"Generating new message: $i")
      val headers = collection.mutable.Map[String, String]()
      headers.put("command", "test")
      headers.put("uuid", UUID.randomUUID().toString)

      new Message(headers = headers.toMap, contentType = Some(MediaType.PLAIN_TEXT_UTF_8), body = "This is a test".getBytes)
    }).runWith(sink)
  }

  after {
    Console.out.println("After each - stopping plug.")
    plug.shutdown()

    materializer.shutdown()
    system.shutdown()
  }

  describe("Plug") {
    it("should allow the creation of a Plug and run it") {
      var handlerResponse: Option[Future[PlugResponse]] = None

      Console.out.println("Creating command handler.")
      plug.registerHandler("test", new PlugHandler() {
        override def onMessage(sc: SparkContext, message: PlugMessage): Future[PlugResponse] = {
          handlerResponse = Some(Future[PlugResponse] {
            Console.out.println(s"Test command handler handling: $message")

            val distFile = sc.textFile("/opt/sparkplug/src/test/resources/spark-sample-data.txt")
            val lineLengths = distFile.map(s => s.length)
            val totalLength = lineLengths.reduce((a, b) => a + b)

            val response = collection.mutable.Map[String, String]()
            response.put("lineLengths", lineLengths.collect().foldLeft("Lengths: ")((b,a) => s"$b+$a"))
            response.put("totalLength", totalLength.toString)

            Console.out.println(s"Test command handler done executing: $response")

            new PlugResponse(message.uuid, Json.toJson(response.toMap).toString().getBytes, message.contentType)
          })
          handlerResponse.get
        }
      })

      plug.run()

      eventually (timeout(scaled(30.seconds))) {
        val messageCount = Await.result(plug.listener.getConnection.get
          .queueDeclare(Queue("q_sparkplug", durable = true)), 5.seconds)
          .messageCount
        messageCount should be (0)
        // Console.out.println("No more messages in queue, cleaning up.")

        // Console.out.println("Awaiting handler execution completion.")
        Await.result(handlerResponse.get, 5.seconds)
        Console.out.println("Handler execution completed.")
      }
    }
  }
}
