package uncharted.sparkplug.test;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.amqp.RabbitAutoConfiguration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.annotation.PropertySources;
import uncharted.sparkplug.spring.SparkplugConfiguration;
import uncharted.sparkplug.test.spring.SparkplugTestConfiguration;

/**
 * Test harness for Sparkplug
 */
@SpringBootApplication
@Import({SparkplugConfiguration.class, RabbitAutoConfiguration.class, SparkplugTestConfiguration.class})
@PropertySources(value = {@PropertySource("classpath:application.properties")})
public class Main {
  public static void main(final String... args) {
    SpringApplication.run(Main.class, args);
  }
}
