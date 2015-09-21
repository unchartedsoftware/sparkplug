package uncharted.sparkplug.listener;

import lombok.extern.slf4j.Slf4j;
import org.apache.spark.SparkContext;
import uncharted.sparkplug.message.SparkplugMessage;
import uncharted.sparkplug.message.SparkplugResponse;

/**
 * Basic implementation of the SparkplugAdapter
 */
@Slf4j
public class SimpleSparkplugListener implements SparkplugListener {
  @Override
  public SparkplugResponse onMessage(final SparkContext sparkContext, final SparkplugMessage message) {
    log.debug("Received a message: {}", message);

    return null;
  }
}
