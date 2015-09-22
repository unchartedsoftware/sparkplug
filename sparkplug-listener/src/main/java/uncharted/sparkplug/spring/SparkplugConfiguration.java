package uncharted.sparkplug.spring;

import lombok.extern.slf4j.Slf4j;
import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaSparkContext;
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
  public SparkplugListener sparkplugListener() {
    return new SparkplugListener();
  }

  @Bean
  public JavaSparkContext sparkContext() {
    final SparkConf sparkConf = new SparkConf().setAppName("Sparkplug");
    log.info("Created Spark configuration.");

    final JavaSparkContext jsc = new JavaSparkContext(sparkConf);
    log.info("Created Java Spark context.");

    return jsc;
  }
}
