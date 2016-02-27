package software.uncharted.sparkplug.listener

import com.spingo.op_rabbit.RabbitControl
import akka.actor.{ActorSystem, Props}

class SparkplugListener {
  implicit val actorSystem = ActorSystem("sparkplug")
  val rabbitControl = actorSystem.actorOf(Props[RabbitControl])
}
