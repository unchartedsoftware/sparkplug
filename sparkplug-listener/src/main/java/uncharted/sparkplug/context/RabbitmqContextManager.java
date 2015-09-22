package uncharted.sparkplug.context;

import lombok.extern.slf4j.Slf4j;
import org.apache.spark.api.java.JavaSparkContext;
import org.springframework.amqp.core.AmqpAdmin;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.MessageListener;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.listener.SimpleMessageListenerContainer;
import org.springframework.amqp.rabbit.listener.adapter.MessageListenerAdapter;
import org.springframework.beans.factory.annotation.Autowired;
import uncharted.sparkplug.listener.RabbitMqListenerAdapter;
import uncharted.sparkplug.listener.SparkplugListener;
import uncharted.sparkplug.message.SparkplugMessage;
import uncharted.sparkplug.spring.SparkplugProperties;

import javax.annotation.PostConstruct;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Schedule and manage the various different bits of the execution context
 */
@Slf4j
public class RabbitmqContextManager {
  @Autowired
  private SparkplugProperties sparkplugProperties;

  @Autowired
  private ConnectionFactory connectionFactory;

  @Autowired
  private AmqpAdmin amqpAdmin;

  @Autowired
  private AmqpTemplate amqpTemplate;

  @Autowired
  private JavaSparkContext sparkContext;

  @Autowired
  private SparkplugListener listener;

  private final DirectExchange                       inboundDirectExchange  = new DirectExchange("sparkplug-inbound", true, false);
  private final DirectExchange                       outboundDirectExchange = new DirectExchange("sparkplug-outbound", true, false);

  private ExecutorService executorService = Executors.newWorkStealingPool();

  /**
   * Called automatically after the Spring initialization cycle is complete.
   * <p>
   * Registers the inbound exchange.
   */
  @PostConstruct
  public void init() {
    log.debug("Creating Sparkplug RabbitMQ exchange.");
    amqpAdmin.declareExchange(inboundDirectExchange);
    amqpAdmin.declareExchange(outboundDirectExchange);

    connectAdapterToQueue();
  }

  private void connectAdapterToQueue() {
    final String routingKey = sparkplugProperties.getRoutingKey();

    log.debug("Registering Sparkplug listener for routing key {}.", routingKey);

    final Queue queue = new Queue(UUID.randomUUID().toString(), true, false, true);

    log.debug("Created queue {} for routing key {}.", queue.getName(), routingKey);
    amqpAdmin.declareQueue(queue);

    log.debug("Binding queue {} to exchange {} with routing key {}.", queue.getName(), inboundDirectExchange.getName(), routingKey);
    final Binding binding = BindingBuilder.bind(queue).to(inboundDirectExchange).with(routingKey);
    amqpAdmin.declareBinding(binding);

    final RabbitMqListenerAdapter listenerAdapter = new RabbitMqListenerAdapter(executorService, amqpTemplate, listener, sparkContext);

    log.debug("Creating message listener for routing key {}.", routingKey);
    final SimpleMessageListenerContainer listenerContainer = new SimpleMessageListenerContainer(connectionFactory);
    listenerContainer.addQueues(queue);
    listenerContainer.setMessageListener(new MessageListenerAdapter((MessageListener) message -> {
      final SparkplugMessage sparkplugMessage = new SparkplugMessage();
      sparkplugMessage.setUuid((String) message.getMessageProperties().getHeaders().getOrDefault("uuid", "no-uuid-found"));
      sparkplugMessage.setCommand((String) message.getMessageProperties().getHeaders().getOrDefault("command", "no-command-found"));
      sparkplugMessage.setOrder((Integer) message.getMessageProperties().getHeaders().getOrDefault("order", -1));
      sparkplugMessage.setBody(message.getBody());

      if (sparkplugMessage.getUuid().equals("no-uuid-found")) {
        log.error("Inbound message had no UUID, ignoring.");
      } else if (sparkplugMessage.getCommand().equals("no-command-found")) {
        log.error("Inbound message had no command, ignoring.");
      } else {
        log.debug("Queueing message for {}.", routingKey);
        listenerAdapter.queueMessage(sparkplugMessage);
      }
    }));
    listenerContainer.start();
  }
}
