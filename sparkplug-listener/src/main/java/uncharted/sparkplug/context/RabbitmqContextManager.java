package uncharted.sparkplug.context;

import lombok.extern.slf4j.Slf4j;
import org.apache.spark.SparkContext;
import org.springframework.amqp.core.*;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.listener.SimpleMessageListenerContainer;
import org.springframework.amqp.rabbit.listener.adapter.MessageListenerAdapter;
import org.springframework.beans.factory.annotation.Autowired;
import uncharted.sparkplug.adapter.SparkplugAdapter;
import uncharted.sparkplug.message.SparkplugMessage;
import uncharted.sparkplug.message.SparkplugResponse;

import javax.annotation.PostConstruct;
import java.util.*;
import java.util.concurrent.*;

/**
 * Schedule and manage the various different bits of the execution context
 */
@Slf4j
public class RabbitmqContextManager {
  @Autowired
  private ConnectionFactory connectionFactory;

  @Autowired
  private AmqpAdmin amqpAdmin;

  @Autowired
  private AmqpTemplate amqpTemplate;

  private final Map<String, SparkplugAdapter> registeredAdapters = new HashMap<>();
  private final DirectExchange directExchange = new DirectExchange("sparkplug-inbound", true, false);

  private ExecutorService executorService = Executors.newWorkStealingPool();
  private LinkedList<Future<SparkplugResponse>> futures = new LinkedList<>();

  /**
   * Called automatically after the Spring initialization cycle is complete.
   * <p>
   * Registers the inbound exchange.
   */
  @PostConstruct
  public void init() {
    log.debug("Creating Sparkplug RabbitMQ exchange.");
    amqpAdmin.declareExchange(directExchange);

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

          final Iterator<Future<SparkplugResponse>> iter = futures.parallelStream().filter(Future::isDone).iterator();
          while (iter.hasNext()) {
            final Future<SparkplugResponse> res = iter.next();
            try {
              final SparkplugResponse sparkplugResponse = res.get();
              amqpTemplate.send("sparkplug-outbound", sparkplugResponse.getUuid(), new Message(sparkplugResponse.getBody(), null));
            } catch (InterruptedException | ExecutionException e) {
              log.error("Could not collect Sparkplug response from callable task.", e);
            }

            iter.remove();
          }
        }
      }
    }.start();
  }

  /**
   * Register an adapter that will handle messages that match the corresponding routing key
   *
   * @param routingKey The routing key that this adapter should bind itself to; can be '*' for all messages.
   * @param adapter    The adapter to invoke for messages that match the routing key
   */
  public void registerAdapter(final String routingKey, final SparkplugAdapter adapter) {
    if (registeredAdapters.containsKey(routingKey)) {
      log.error("Another Sparkplug adapter is already listening for routing key {}.", routingKey);
      throw new IllegalArgumentException(String.format("Another Sparkplug adapter is already listening for routing key %s.", routingKey));
    }

    log.debug("Registering Sparkplug adapter for routing key {}.", routingKey);
    registeredAdapters.put(routingKey, adapter);

    connectAdapterToQueue(routingKey, adapter);
  }

  private void connectAdapterToQueue(final String routingKey, final SparkplugAdapter adapter) {
    final Queue queue = new Queue(UUID.randomUUID().toString(), false);

    log.debug("Created queue {} for routing key {}.", queue.getName(), routingKey);
    amqpAdmin.declareQueue(queue);

    log.debug("Binding queue {} to exchange {} with routing key {}.", queue.getName(), directExchange.getName(), routingKey);
    final Binding binding = BindingBuilder.bind(queue).to(directExchange).with(routingKey);
    amqpAdmin.declareBinding(binding);

    log.debug("Creating message listener for routing key {}.", routingKey);
    final SimpleMessageListenerContainer listenerContainer = new SimpleMessageListenerContainer(connectionFactory);
    listenerContainer.addQueues(queue);
    listenerContainer.setMessageListener(new MessageListenerAdapter((MessageListener) message -> {
      final SparkplugMessage sparkplugMessage = new SparkplugMessage();
      sparkplugMessage.setBody(message.getBody());

      // schedule the task for execution
      final Future<SparkplugResponse> future = executorService.submit(() -> {
        final String threadName = Thread.currentThread().getName();
        log.debug("Sparkplug running in thread {}.", threadName);
        final SparkContext sparkContext = new SparkContext();
        return adapter.onMessage(sparkContext, sparkplugMessage);
      });

      futures.add(future);
    }));
    listenerContainer.start();
  }
}
