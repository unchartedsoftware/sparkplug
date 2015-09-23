package uncharted.sparkplug.spring;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.UUID;

/**
 * Used to map values from application.properties into the code
 */
@ConfigurationProperties(prefix = "sparkplug")
public class SparkplugProperties {
  private String sparkMaster = null;
  private String appName = "Sparkplug";

  private String inboundExchange = "sparkplug-inbound";

  private String outboundExchange = "sparkplug-outbound";
  private String outboundRoutingKey = "sparkplug-response";

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

  public String getInboundExchange() {
    return inboundExchange;
  }

  public void setInboundExchange(String inboundExchange) {
    this.inboundExchange = inboundExchange;
  }

  public String getOutboundExchange() {
    return outboundExchange;
  }

  public void setOutboundExchange(String outboundExchange) {
    this.outboundExchange = outboundExchange;
  }

  public String getOutboundRoutingKey() {
    return outboundRoutingKey;
  }

  public void setOutboundRoutingKey(String outboundRoutingKey) {
    this.outboundRoutingKey = outboundRoutingKey;
  }

  public String getAppName() {
    return appName;
  }

  public void setAppName(String appName) {
    this.appName = appName;
  }

  public String getSparkMaster() {
    return sparkMaster;
  }

  public void setSparkMaster(String sparkMaster) {
    this.sparkMaster = sparkMaster;
  }
}
