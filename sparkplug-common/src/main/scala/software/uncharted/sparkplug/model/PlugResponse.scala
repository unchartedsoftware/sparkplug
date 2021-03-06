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
package software.uncharted.sparkplug.model

import akka.util.ByteStringBuilder
import com.google.common.net.MediaType
import io.scalac.amqp.Message

case class PlugResponse(uuid: String, clusterId: String, command: String, body: IndexedSeq[Byte], contentType: MediaType) {
  override def toString: String = s"UUID: [ $uuid ], Cluster ID: [ $clusterId ], Command: [ $command ], " +
    s"Body: [ ${new ByteStringBuilder().putBytes(body.toArray).result().utf8String} ], Content Type: [ $contentType ]"

  def toMessage : Message = {
    val headers = collection.mutable.Map[String, String]()
    headers.put("UUID", this.uuid)
    headers.put("cluster-id", this.clusterId)

    new Message(body = this.body, headers = headers.toMap, contentType = Some(this.contentType))
  }
}

object PlugResponse {
  def fromMessage(message: Message) : PlugResponse = {
    new PlugResponse(message.headers.getOrElse("uuid", "no-uuid-found"), message.headers.getOrElse("cluster-id", "1"),
      message.headers.getOrElse("command", "no-command-found"), message.body, message.contentType.getOrElse(MediaType.ANY_TYPE))
  }
}
