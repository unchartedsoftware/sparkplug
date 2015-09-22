package uncharted.sparkplug.spring;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.UUID;

/**
 * Used to map values from application.properties into the code
 */
@ConfigurationProperties(prefix = "sparkplug")
public class SparkplugProperties {
  private String routingKey = UUID.randomUUID().toString();

  private Integer sessionTimeout = 30 * 60 * 1000;

  public Integer getSessionTimeout() {
    return sessionTimeout;
  }

  public void setSessionTimeout(Integer sessionTimeout) {
    this.sessionTimeout = sessionTimeout;
  }

  public String getRoutingKey() {
    return routingKey;
  }

  public void setRoutingKey(String routingKey) {
    this.routingKey = routingKey;
  }
}
