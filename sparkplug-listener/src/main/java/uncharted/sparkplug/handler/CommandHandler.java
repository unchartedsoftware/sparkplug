package uncharted.sparkplug.handler;

import org.apache.spark.api.java.JavaSparkContext;
import uncharted.sparkplug.message.SparkplugMessage;
import uncharted.sparkplug.message.SparkplugResponse;

/**
 * Handle the execution of a specific command
 */
public interface CommandHandler {
  SparkplugResponse onMessage(JavaSparkContext sparkContext, SparkplugMessage message);
}
