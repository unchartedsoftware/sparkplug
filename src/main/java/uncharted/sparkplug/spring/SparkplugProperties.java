package uncharted.sparkplug.spring;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.UUID;

/**
 * Used to map values from application.properties into the code
 */
@ConfigurationProperties(prefix = "sparkplug")
public class SparkplugProperties {
  private String rabbitMqServer = "localhost";
  private Integer rabbitMqPort = 5672;
  private String rabbitMqUsername = "admin";
  private String rabbitMqPassword = "admin";
  private String rabbitMqVirtualHost = "/";

  private String sparkMaster = null;
  private String appName = "Sparkplug";

  private String inboundExchange = "sparkplug-inbound";
  private String inboundRoutingKey = UUID.randomUUID().toString();

  private String outboundExchange = "sparkplug-outbound";
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

  public void setSessionTimeout(Integer sessionTimeout) {
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
