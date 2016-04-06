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

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.{Sink, Source}
import com.google.common.net.MediaType
import io.scalac.amqp.{Message, Queue}
import org.scalatest.concurrent.Eventually
import org.scalatest.{BeforeAndAfter, FunSpec, Matchers}

import scala.concurrent.Await

// scalastyle:off underscore.import
import scala.concurrent.duration._

import Matchers._
// scalastyle:on underscore.import

class PlugSpec extends FunSpec with BeforeAndAfter with Eventually {
  val plug = new Plug()

  implicit val system = ActorSystem("SparkPlug-Test")
  implicit val materializer = ActorMaterializer()

  def time[T](str: String)(thunk: => T): T = {
    print(str + "... ")
    val t1 = System.currentTimeMillis
    val x = thunk
    val t2 = System.currentTimeMillis
    println("Execution time: " + (t2 - t1) + " msecs")
    x
  }

  before {
    time("Before each - populating data and starting plug.") {
      val source = Source(1 to 50000)
      val subscriber = plug.listener.getConnection.publishDirectly("q_sparkplug")
      val sink = Sink.fromSubscriber(subscriber)

      source.map(i => {
        if (i % 1000 == 0) Console.out.println(s"Generating new message: $i")
        new Message(correlationId = Some((i % 2).toString), contentType = Some(MediaType.PLAIN_TEXT_UTF_8), body = "This is a test".getBytes)
      }).runWith(sink)
    }
  }

  after {
    time("After each - stopping plug.") {
      plug.shutdown()

      materializer.shutdown()
      system.shutdown()
    }
  }

  describe("Plug") {
    it("should allow the creation of a Plug and run it") {
      plug.run()

      eventually (timeout(scaled(30.seconds))) {
        val messageCount = Await.result(plug.listener.getConnection
          .queueDeclare(Queue("q_sparkplug", durable = true)), 5.seconds)
          .messageCount
        messageCount should be (0)
        Console.out.println("No more messages in queue, cleaning up.")
      }
    }
  }
}
