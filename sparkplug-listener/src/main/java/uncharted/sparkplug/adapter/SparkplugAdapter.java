package uncharted.sparkplug.adapter;

import org.apache.spark.SparkContext;
import uncharted.sparkplug.message.SparkplugMessage;
import uncharted.sparkplug.message.SparkplugResponse;

/**
 * Contract to handle messages received by Sparkplug
 */
public interface SparkplugAdapter {
  SparkplugResponse onMessage(final SparkContext sparkContext, final SparkplugMessage message);
}
