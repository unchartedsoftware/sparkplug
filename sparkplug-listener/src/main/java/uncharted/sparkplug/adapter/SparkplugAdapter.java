package uncharted.sparkplug.adapter;

import org.apache.spark.SparkContext;
import uncharted.sparkplug.message.SparkplugMessage;

/**
 * Contract to handle messages received by Sparkplug
 */
public interface SparkplugAdapter {
  void onMessage(final SparkContext sparkContext, final SparkplugMessage message);
}
