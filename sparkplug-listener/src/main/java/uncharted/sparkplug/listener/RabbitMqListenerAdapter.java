package uncharted.sparkplug.listener;

import lombok.extern.slf4j.Slf4j;
import org.apache.spark.api.java.JavaSparkContext;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageProperties;
import uncharted.sparkplug.message.SparkplugMessage;
import uncharted.sparkplug.message.SparkplugResponse;

import java.util.Iterator;
import java.util.Map.Entry;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Maintain a queue of messages to shove into a listener
 */
@Slf4j
public class RabbitMqListenerAdapter {
  private final ConcurrentMap<String, BlockingQueue<SparkplugMessage>> messages         = new ConcurrentHashMap<>();
  private final ConcurrentMap<String, Boolean>                         inFlight         = new ConcurrentHashMap<>();
  private final ConcurrentMap<String, Future<SparkplugResponse>>       executingThreads = new ConcurrentHashMap<>();

  private final AtomicReference<ExecutorService>   executorService;
  private final AtomicReference<AmqpTemplate>      amqpTemplate;
  private final AtomicReference<SparkplugListener> sparkplugListener;

  private final AtomicReference<JavaSparkContext> sparkContext;

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

    final String uuid = message.getUuid();
    if (messages.containsKey(uuid)) {
      log.debug("Adding message to existing queue for {}.", uuid);
      messages.get(uuid).add(message);
    } else {
      log.debug("No queue exists for {}, creating and adding.", uuid);
      final LinkedBlockingQueue<SparkplugMessage> messageQueue = new LinkedBlockingQueue<>();
      messageQueue.add(message);
      messages.put(uuid, messageQueue);
    }
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

          // loop through the messages to see if we have any queue
          for (final Entry<String, BlockingQueue<SparkplugMessage>> messageEntry : messages.entrySet()) {
            final String uuid = messageEntry.getKey();
            if (!inFlight.containsKey(uuid)) {
              final BlockingQueue<SparkplugMessage> messageQueue = messageEntry.getValue();
              if (!messageQueue.isEmpty()) {
                log.debug("There are {} messages for queue {}.", uuid, messageQueue.size());
                final SparkplugMessage message = messageQueue.poll();
                inFlight.put(uuid, true);
                executingThreads.put(uuid, executorService.get().submit(() -> sparkplugListener.get().onMessage(sparkContext.get(), message)));
              }
            }
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

          final Iterator<Entry<String, Future<SparkplugResponse>>> futureIter = executingThreads.entrySet().parallelStream()
                                                                                  .filter(e -> e.getValue().isDone()).iterator();
          while (futureIter.hasNext()) {
            final Entry<String, Future<SparkplugResponse>> futureEntry = futureIter.next();
            try {
              final SparkplugResponse response = futureEntry.getValue().get();

              log.debug("Sending message back to upstream: {}", response);

              final MessageProperties messageProperties = new MessageProperties();
              messageProperties.getHeaders().put("uuid", response.getUuid());

              amqpTemplate.get().send("sparkplug-outbound", "sparkplug-response", new Message(response.getBody(), messageProperties));

              executingThreads.remove(futureEntry.getKey());
              inFlight.remove(futureEntry.getKey());
            } catch (InterruptedException | ExecutionException e) {
              log.error("Could not retrieve response from Sparkplug adapter.", e);
            }
          }
        }
      }
    }.start();
  }
}
