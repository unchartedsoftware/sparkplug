package uncharted.sparkplug.spring;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import uncharted.sparkplug.adapter.SimpleSparkplugAdapter;
import uncharted.sparkplug.adapter.SparkplugAdapter;
import uncharted.sparkplug.listener.rabbitmq.RabbitMqSparkplugListener;
import uncharted.sparkplug.context.RabbitmqContextManager;

import java.util.UUID;

/**
 * Basic configuration
 */
@Configuration
@Slf4j
public class SparkplugConfiguration {
  public SparkplugConfiguration() {
    log.debug("Sparkplug Spring configuration initialized.");
  }

  @Bean
  public RabbitmqContextManager sparkContextManager() {
    return new RabbitmqContextManager();
  }

  @Bean
  public RabbitMqSparkplugListener sparkplugListener() {
    return new RabbitMqSparkplugListener();
  }

  /**
   * If no adapter has been defined, register a simple adapter.
   *
   * @return The simple Sparkplug adapter
   */
  @Bean
  @ConditionalOnMissingBean(SparkplugAdapter.class)
  public SimpleSparkplugAdapter simpleSparkplugAdapter() {
    final SimpleSparkplugAdapter sparkplugAdapter = new SimpleSparkplugAdapter();

    final RabbitmqContextManager rabbitmqContextManager = sparkContextManager();
    rabbitmqContextManager.registerAdapter(UUID.randomUUID().toString(), sparkplugAdapter);

    return sparkplugAdapter;
  }
}
