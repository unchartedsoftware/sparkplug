package uncharted.sparkplug.spring;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;

/**
 * Basic configuration
 */
@Configuration
@Slf4j
public class SpringConfiguration {
  public SpringConfiguration() {
    log.debug("Sparkplug spring configuration initialized.");
  }
}
