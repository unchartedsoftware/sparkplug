package uncharted.sparkplug.spring;

import lombok.extern.slf4j.Slf4j;
import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaSparkContext;
import org.springframework.amqp.core.AmqpAdmin;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.rabbit.annotation.EnableRabbit;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.CachingConnectionFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import uncharted.sparkplug.context.RabbitmqContextManager;
import uncharted.sparkplug.listener.SparkplugListener;

/**
 * Basic configuration
 */
@Configuration
@EnableConfigurationProperties(SparkplugProperties.class)
@EnableRabbit
@Slf4j
public class SparkplugConfiguration {
  @Value("${rabbit.server}")
  private String rabbitServer;

  @Value("${rabbit.port}")
  private String rabbitPort;

  @Value("${rabbit.username}")
  private String rabbitUsername;

  @Value("${rabbit.password}")
  private String rabbitPassword;

  @Value("${rabbit.virtualHost}")
  private String rabbitVirtualHost;

  public SparkplugConfiguration() {
    log.debug("Sparkplug Spring configuration initialized.");
  }

  @Bean
  public MessageConverter messageConverter() {
    final Jackson2JsonMessageConverter converter = new Jackson2JsonMessageConverter();
    converter.setCreateMessageIds(true);
    return converter;
  }

  @Bean
  public ConnectionFactory connectionFactory() {
    final CachingConnectionFactory connectionFactory = new CachingConnectionFactory(rabbitServer);
    connectionFactory.setUsername(rabbitUsername);
    connectionFactory.setPassword(rabbitPassword);
    return connectionFactory;
  }

  @Bean
  public SimpleRabbitListenerContainerFactory rabbitListenerContainerFactory() {
    SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();
    factory.setConnectionFactory(connectionFactory());
    factory.setConcurrentConsumers(3);
    factory.setMaxConcurrentConsumers(10);
    return factory;
  }

  @Bean
  public AmqpAdmin amqpAdmin() {
    return new RabbitAdmin(connectionFactory());
  }

  @Bean
  public RabbitTemplate rabbitTemplate() {
    final RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory());
    rabbitTemplate.setMessageConverter(messageConverter());
    return rabbitTemplate;
  }

  @Bean
  public DirectExchange directExchange() {
    return new DirectExchange("p2p", true, false);
  }

  @Bean
  public RabbitmqContextManager sparkContextManager() {
    return new RabbitmqContextManager();
  }

  @Bean
  public SparkplugListener sparkplugListener() {
    return new SparkplugListener();
  }

  @Bean
  public JavaSparkContext sparkContext(final SparkplugProperties sparkplugProperties) {
    final SparkConf sparkConf = new SparkConf().setAppName(sparkplugProperties.getAppName());

    if (sparkplugProperties.getSparkMaster() != null) {
      sparkConf.setMaster(sparkplugProperties.getSparkMaster());
    }

    log.info("Created Spark configuration (Spark master: {}, app name: {}", sparkplugProperties.getSparkMaster(), sparkplugProperties.getAppName());

    final JavaSparkContext jsc = new JavaSparkContext(sparkConf);
    log.info("Created Java Spark context.");

    return jsc;
  }
}
