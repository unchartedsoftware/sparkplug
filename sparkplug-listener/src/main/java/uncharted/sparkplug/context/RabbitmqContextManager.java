package uncharted.sparkplug.context;

import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.AmqpAdmin;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.listener.SimpleMessageListenerContainer;
import org.springframework.amqp.rabbit.listener.adapter.MessageListenerAdapter;
import org.springframework.beans.factory.annotation.Autowired;
import uncharted.sparkplug.adapter.SparkplugAdapter;

import javax.annotation.PostConstruct;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Schedule and manage the various different bits of the execution context
 */
@Slf4j
public class RabbitmqContextManager {
  @Autowired
  private ConnectionFactory connectionFactory;

  @Autowired
  private AmqpAdmin amqpAdmin;

  private final Map<String, SparkplugAdapter> registeredAdapters = new HashMap<>();
  private final DirectExchange                directExchange     = new DirectExchange("sparkplug-inbound", true, false);

  /**
   * Called automatically after the Spring initialization cycle is complete.
   * <p>
   * Registers the inbound exchange.
   */
  @PostConstruct
  public void init() {
    log.debug("Creating Sparkplug RabbitMQ exchange.");
    amqpAdmin.declareExchange(directExchange);
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
    listenerContainer.setMessageListener(new MessageListenerAdapter(adapter, "onMessage"));
    listenerContainer.start();
  }
}