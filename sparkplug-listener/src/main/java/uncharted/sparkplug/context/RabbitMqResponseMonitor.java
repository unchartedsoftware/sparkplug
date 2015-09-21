package uncharted.sparkplug.context;

import org.apache.commons.lang3.tuple.Pair;
import uncharted.sparkplug.message.SparkplugMessage;

import java.util.Map;
import java.util.Queue;

/**
 * 1. Receive a message
 * 2. Check the UUID header
 * 3.
 *   a. If internal queue exists, add to queue
 *   b. If not, create queue and add
 * 4. Consume queue and feed into listener
 * 5. Monitor responses and feed back
 * 6. Periodically check the queue to see when it was last used and nuke
 */
public class RabbitMqResponseMonitor {
  private Map<String, Pair<Long, Queue<SparkplugMessage>>> queueMapper;
}
