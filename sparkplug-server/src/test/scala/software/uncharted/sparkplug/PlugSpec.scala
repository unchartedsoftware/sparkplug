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
import org.apache.spark.sql.SparkSession
import org.scalatest.concurrent.Eventually
import org.scalatest.{BeforeAndAfter, FunSpec}
import software.uncharted.sparkplug.handler.PlugHandler
import software.uncharted.sparkplug.model.{PlugMessage, PlugResponse}
import java.nio.file.{Paths, Files}

import scala.concurrent.{Await, Promise}

// scalastyle:off underscore.import
import org.scalatest.Matchers._

import scala.concurrent.duration._
// scalastyle:on underscore.import

class PlugSpec extends FunSpec with BeforeAndAfter with Eventually {
  val plug = Plug.getInstance()

  implicit val system = ActorSystem("SparkPlug-Test")
  implicit val materializer = ActorMaterializer()

  private val conf = ConfigFactory.load()
  private val q_sparkplug: String = conf.getString("sparkplug.inbound-queue")
  private val r_sparkplug: String = conf.getString("sparkplug.outbound-queue")

  before {
    plug.connect()

    Console.out.println("Defining queues and cleaning as required.")
    val qDecQ = plug.getListener.getConnection.get.queueDeclare(Queue(name = q_sparkplug, durable = true))
    Await.result(qDecQ, 5.seconds)

    val rDecQ = plug.getListener.getConnection.get.queueDeclare(Queue(name = r_sparkplug, durable = true))
    Await.result(rDecQ, 5.seconds)

    val qPurgeQ = plug.getListener.getConnection.get.queuePurge(q_sparkplug)
    Await.result(qPurgeQ, 5.seconds)

    val rPurgeQ = plug.getListener.getConnection.get.queuePurge(r_sparkplug)
    Await.result(rPurgeQ, 5.seconds)

    Console.out.println("Before each - populating data and starting plug.")
    val source = Source(1 to 1)
    val subscriber = plug.getListener.getConnection.get.publishDirectly(q_sparkplug)
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
      val p = Promise[String]()

      Console.out.println("Creating command handler.")
      plug.registerHandler("test", new PlugHandler() {
        override def onMessage(sparkSession: SparkSession, message: PlugMessage): Message = {
          var sc = sparkSession.sparkContext
          val rdd = sc.textFile("sparkplug-server/src/test/resources/spark-sample-data.txt")
          .map(line => (line, 1))
          .reduceByKey((x, y) => x + y)
          .collect()


          val response = collection.mutable.Map[String, String]()
          response.put("lineLengths", "4")
          response.put("totalLength", "3")

          val retVal = new PlugResponse(message.uuid, message.clusterId, message.command, response.toMap.toString.getBytes, message.contentType).toMessage
          p success "Done"
          retVal
        }
      })


      plug.run()

      eventually(timeout(scaled(60.seconds))) {
        val success = Await.result(p.future, 60.seconds)
        success should be ("Done")
      }
    }
  }
}
