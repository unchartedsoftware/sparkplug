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
import akka.stream.scaladsl.{Sink, Source}

class PlugListener {
  // streaming invoices to Accounting Department
  val connection = Connection()
  // create org.reactivestreams.Publisher
  val queue = connection.consume(queue = "invoices")
  // create org.reactivestreams.Subscriber
  val exchange = connection.publish(exchange = "accounting_department",
    routingKey = "invoices")

  implicit val system = ActorSystem()
  implicit val mat = ActorMaterializer()
  // Run akka-streams with queue as Source and exchange as Sink
  Source.fromPublisher(queue).map(_.message).runWith(Sink.fromSubscriber(exchange))
}
