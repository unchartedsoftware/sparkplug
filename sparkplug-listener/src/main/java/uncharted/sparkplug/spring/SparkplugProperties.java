package uncharted.sparkplug.spring;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Used to map values from application.properties into the code
 */
@SuppressWarnings("ALL")
@ConfigurationProperties(prefix = "sparkplug")
public class SparkplugProperties {
  private String rabbitMqServer = "localhost";
  private Integer rabbitMqPort = 5672;
  private String rabbitMqUsername = "admin";
  private String rabbitMqPassword = "admin";
  private String rabbitMqVirtualHost = "/";

  private String sparkMaster = null;
  private String sparkAppName = "Sparkplug";

  private String inboundExchange = "sparkplug-inbound";
  private String inboundRoutingKey = UUID.randomUUID().toString();

  private String outboundExchange   = "sparkplug-outbound";
  private String outboundRoutingKey = "sparkplug-response";

  private Integer sessionTimeout = 30 * 60 * 1000;

  public String getRabbitMqServer() {
    return rabbitMqServer;
  }

  public void setRabbitMqServer(String rabbitMqServer) {
    this.rabbitMqServer = rabbitMqServer;
  }

  public Integer getRabbitMqPort() {
    return rabbitMqPort;
  }

  public void setRabbitMqPort(Integer rabbitMqPort) {
    this.rabbitMqPort = rabbitMqPort;
  }

  public String getRabbitMqUsername() {
    return rabbitMqUsername;
  }

  public void setRabbitMqUsername(String rabbitMqUsername) {
    this.rabbitMqUsername = rabbitMqUsername;
  }

  public String getRabbitMqPassword() {
    return rabbitMqPassword;
  }

  public void setRabbitMqPassword(String rabbitMqPassword) {
    this.rabbitMqPassword = rabbitMqPassword;
  }

  public String getRabbitMqVirtualHost() {
    return rabbitMqVirtualHost;
  }

  public void setRabbitMqVirtualHost(String rabbitMqVirtualHost) {
    this.rabbitMqVirtualHost = rabbitMqVirtualHost;
  }

  public Integer getSessionTimeout() {
    return sessionTimeout;
  }

  public void setSessionTimeout(final Integer sessionTimeout) {
    this.sessionTimeout = sessionTimeout;
  }

  public String getInboundRoutingKey() {
    return inboundRoutingKey;
  }

  public void setInboundRoutingKey(String inboundRoutingKey) {
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
