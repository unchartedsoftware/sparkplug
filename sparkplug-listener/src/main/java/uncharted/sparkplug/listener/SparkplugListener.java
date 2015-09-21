package uncharted.sparkplug.listener;

import org.apache.spark.api.java.JavaSparkContext;
import uncharted.sparkplug.message.SparkplugMessage;
import uncharted.sparkplug.message.SparkplugResponse;

/**
 * Contract to handle messages received by Sparkplug
 */
public interface SparkplugListener {
  SparkplugResponse onMessage(final JavaSparkContext sparkContext, final SparkplugMessage message);
}
