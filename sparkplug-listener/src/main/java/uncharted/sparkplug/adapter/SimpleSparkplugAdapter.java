package uncharted.sparkplug.adapter;

import lombok.extern.slf4j.Slf4j;

/**
 * Basic implementation of the SparkplugAdapter
 */
@Slf4j
public class SimpleSparkplugAdapter implements SparkplugAdapter {
  @Override
  public void onMessage(final byte[] message) {
    log.debug("Received a message: {}", new String(message));
  }
}
