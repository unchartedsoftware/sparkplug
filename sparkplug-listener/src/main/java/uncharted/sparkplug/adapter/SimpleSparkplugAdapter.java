package uncharted.sparkplug.adapter;

import lombok.extern.slf4j.Slf4j;
import org.apache.spark.SparkContext;
import uncharted.sparkplug.message.SparkplugMessage;

/**
 * Basic implementation of the SparkplugAdapter
 */
@Slf4j
public class SimpleSparkplugAdapter implements SparkplugAdapter {
  @Override
  public void onMessage(final SparkContext sparkContext, final SparkplugMessage message) {
    log.debug("Received a message: {}", message);
  }
}
