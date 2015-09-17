package uncharted.sparkplug.test;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Import;
import uncharted.sparkplug.spring.SparkplugConfiguration;

/**
 * Test harness for Sparkplug
 */
@SpringBootApplication
@Import(SparkplugConfiguration.class)
public class Main {
  public static void main(final String... args) {
    SpringApplication.run(Main.class, args);
  }
}
