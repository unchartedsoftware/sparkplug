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
  public JavaSparkContext sparkContext(final SparkplugProperties sparkplugProperties) {
    final SparkConf sparkConf = new SparkConf().setAppName(sparkplugProperties.getSparkAppName());

    if (sparkplugProperties.getSparkMaster() != null) {
      sparkConf.setMaster(sparkplugProperties.getSparkMaster());
    }

    log.info("Created Spark configuration (Spark master: {}, app name: {}", sparkplugProperties.getSparkMaster(), sparkplugProperties.getSparkAppName());

    final JavaSparkContext jsc = new JavaSparkContext(sparkConf);
    log.info("Created Java Spark context.");

    return jsc;
  }
}
