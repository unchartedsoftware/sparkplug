package uncharted.sparkplug.spring;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import uncharted.sparkplug.context.RabbitmqContextManager;
import uncharted.sparkplug.listener.SimpleSparkplugListener;
import uncharted.sparkplug.listener.SparkplugListener;

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

  /**
   * If no listener has been defined, register a simple listener.
   *
   * @return The simple Sparkplug listener
   */
  @Bean
  @ConditionalOnMissingBean(SparkplugListener.class)
  public SimpleSparkplugListener simpleSparkplugAdapter() {
    final SimpleSparkplugListener sparkplugAdapter = new SimpleSparkplugListener();

    final RabbitmqContextManager rabbitmqContextManager = sparkContextManager();
    rabbitmqContextManager.registerAdapter(UUID.randomUUID().toString(), sparkplugAdapter);

    return sparkplugAdapter;
  }
}
