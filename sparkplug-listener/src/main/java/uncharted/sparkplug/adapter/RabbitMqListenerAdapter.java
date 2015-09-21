package uncharted.sparkplug.adapter;

import lombok.extern.slf4j.Slf4j;
import uncharted.sparkplug.listener.SparkplugListener;
import uncharted.sparkplug.message.SparkplugMessage;

import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.ExecutorService;

/**
 * Maintain a queue of messages to shove into a listener
 */
@Slf4j
public class RabbitMqListenerAdapter {
  private final Queue<SparkplugMessage> messages = new LinkedList<>();

  private final ExecutorService executorService;
  private final SparkplugListener sparkplugListener;

  public RabbitMqListenerAdapter(final ExecutorService executorService, final final SparkplugListener sparkplugListener) {
    this.executorService = executorService;
    this.sparkplugListener = sparkplugListener;
  }

  public void queueMessage(final SparkplugMessage message) {
    messages.add(message);
  }

  private void run() {
    new Thread(){
      @Override
      public void run() {
        while (true) {
          try {
            sleep(100);
          } catch (final InterruptedException ie) {
            // whatever
          }

          if (!messages.isEmpty()) {
            final SparkplugMessage message = messages.poll();
            sparkplugListener.onMessage()
          }
        }
      }
    }.start();
  }
}
