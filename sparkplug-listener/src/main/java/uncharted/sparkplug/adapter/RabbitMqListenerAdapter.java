package uncharted.sparkplug.adapter;

import lombok.extern.slf4j.Slf4j;
import org.apache.spark.api.java.JavaSparkContext;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageProperties;
import uncharted.sparkplug.listener.SparkplugListener;
import uncharted.sparkplug.message.SparkplugMessage;
import uncharted.sparkplug.message.SparkplugResponse;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Maintain a queue of messages to shove into a listener
 */
@Slf4j
public class RabbitMqListenerAdapter {
  private final BlockingQueue<SparkplugMessage> messages = new LinkedBlockingQueue<>();

  private final AtomicReference<ExecutorService>   executorService;
  private final AtomicReference<AmqpTemplate>      amqpTemplate;
  private final AtomicReference<SparkplugListener> sparkplugListener;

  private final AtomicReference<JavaSparkContext> sparkContext;

  private final AtomicReference<Future<SparkplugResponse>> executingThread = new AtomicReference<>();

  private AtomicBoolean inFlight = new AtomicBoolean(false);

  public RabbitMqListenerAdapter(final ExecutorService executorService, final AmqpTemplate amqpTemplate, final SparkplugListener sparkplugListener,
                                 final JavaSparkContext sparkContext) {
    this.executorService = new AtomicReference<>(executorService);
    this.amqpTemplate = new AtomicReference<>(amqpTemplate);
    this.sparkplugListener = new AtomicReference<>(sparkplugListener);

    this.sparkContext = new AtomicReference<>(sparkContext);

    run();
  }

  public void queueMessage(final SparkplugMessage message) {
    log.debug("Adding message to queue.");
    messages.add(message);
  }

  private void run() {
    new Thread() {
      @SuppressWarnings("InfiniteLoopStatement")
      @Override
      public void run() {
        while (true) {
          try {
            sleep(100);
          } catch (final InterruptedException ie) {
            // whatever
          }

          if (!messages.isEmpty() && !inFlight.get()) {
            log.debug("Message was in queue, setting flag to in flight.");
            inFlight.set(true);
            final SparkplugMessage message = messages.poll();
            executingThread.set(executorService.get().submit(() -> sparkplugListener.get().onMessage(sparkContext.get(), message)));
          }
        }
      }
    }.start();

    new Thread() {
      @SuppressWarnings("InfiniteLoopStatement")
      @Override
      public void run() {
        while (true) {
          try {
            sleep(100);
          } catch (final InterruptedException ie) {
            // whatever
          }

          final Future<SparkplugResponse> future = executingThread.get();
          if (inFlight.get()) {
            if (future != null && future.isDone()) {
              try {
                final SparkplugResponse response = future.get();

                log.debug("Sending message back to upstream: {}", response);

                final MessageProperties messageProperties = new MessageProperties();
                messageProperties.getHeaders().put("uuid", response.getUuid());

                amqpTemplate.get().send("sparkplug-outbound", "sparkplug-response", new Message(response.getBody(), messageProperties));

                executingThread.set(null);
                inFlight.set(false);
              } catch (InterruptedException | ExecutionException e) {
                log.error("Could not retrieve response from Sparkplug adapter.", e);
              }
            } else if (future == null) {
              log.debug("Flag was set to in flight, but no future was pending. Resetting.");
              inFlight.set(false);
            }
          }
        }
      }
    }.start();
  }
}
