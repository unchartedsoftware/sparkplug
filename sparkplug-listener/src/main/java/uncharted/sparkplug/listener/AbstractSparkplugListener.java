package uncharted.sparkplug.listener;

import uncharted.sparkplug.context.RabbitmqContextManager;

/**
 * Base class that defines common functionality shared between various Sparkplug implementations
 */
public abstract class AbstractSparkplugListener implements SparkplugListener {
  protected RabbitmqContextManager rabbitmqContextManager;

  @Override
  public void setRabbitmqContextManager(final RabbitmqContextManager rabbitmqContextManager) {
    this.rabbitmqContextManager = rabbitmqContextManager;
  }
}
