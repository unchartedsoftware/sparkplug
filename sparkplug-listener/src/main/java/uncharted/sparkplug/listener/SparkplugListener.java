package uncharted.sparkplug.listener;

import org.apache.spark.SparkContext;
import uncharted.sparkplug.message.SparkplugMessage;
import uncharted.sparkplug.message.SparkplugResponse;

/**
 * Contract to handle messages received by Sparkplug
 */
public interface SparkplugListener {
  SparkplugResponse onMessage(final SparkContext sparkContext, final SparkplugMessage message);
}
