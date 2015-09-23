package uncharted.sparkplug.spring;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Used to map values from application.properties into the code
 */
@SuppressWarnings("ALL")
@ConfigurationProperties(prefix = "sparkplug")
public class SparkplugProperties {
  private String sparkMaster = null;
  private String sparkAppName = "Sparkplug";

  private String inboundExchange   = "sparkplug-inbound";
  private String inboundRoutingKey = "sparkplug-request";

  private String outboundExchange   = "sparkplug-outbound";
  private String outboundRoutingKey = "sparkplug-response";

  private Integer sessionTimeout = 30 * 60 * 1000;

  public Integer getSessionTimeout() {
    return sessionTimeout;
  }

  public void setSessionTimeout(final Integer sessionTimeout) {
    this.sessionTimeout = sessionTimeout;
  }

  public String getInboundRoutingKey() {
    return inboundRoutingKey;
  }

  public void setInboundRoutingKey(final String inboundRoutingKey) {
    this.inboundRoutingKey = inboundRoutingKey;
  }

  public String getInboundExchange() {
    return inboundExchange;
  }

  public void setInboundExchange(final String inboundExchange) {
    this.inboundExchange = inboundExchange;
  }

  public String getOutboundExchange() {
    return outboundExchange;
  }

  public void setOutboundExchange(final String outboundExchange) {
    this.outboundExchange = outboundExchange;
  }

  public String getOutboundRoutingKey() {
    return outboundRoutingKey;
  }

  public void setOutboundRoutingKey(final String outboundRoutingKey) {
    this.outboundRoutingKey = outboundRoutingKey;
  }

  public String getSparkAppName() {
    return sparkAppName;
  }

  public void setSparkAppName(final String sparkAppName) {
    this.sparkAppName = sparkAppName;
  }

  public String getSparkMaster() {
    return sparkMaster;
  }

  public void setSparkMaster(final String sparkMaster) {
    this.sparkMaster = sparkMaster;
  }
}
