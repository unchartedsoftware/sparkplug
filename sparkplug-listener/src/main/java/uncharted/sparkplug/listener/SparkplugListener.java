package uncharted.sparkplug.listener;

import uncharted.sparkplug.context.RabbitmqContextManager;

/**
 * Defines the contract between an application and Spark
 */
public interface SparkplugListener {
  void setRabbitmqContextManager(final RabbitmqContextManager rabbitmqContextManager);
}
