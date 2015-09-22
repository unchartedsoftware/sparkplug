package uncharted.sparkplug.test.spring;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.amqp.core.AmqpAdmin;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageListener;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.listener.SimpleMessageListenerContainer;
import org.springframework.amqp.rabbit.listener.adapter.MessageListenerAdapter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import uncharted.sparkplug.listener.SparkplugListener;
import uncharted.sparkplug.test.handler.SimpleSparkplugCommandHandler;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.util.List;
import java.util.UUID;

/**
 * Basic configuration
 */
@Configuration
@Slf4j
public class SparkplugTestConfiguration {
  @Autowired
  private ConnectionFactory connectionFactory;

  @Autowired
  private AmqpAdmin amqpAdmin;

  @Autowired
  private AmqpTemplate amqpTemplate;

  @Autowired
  private SparkplugListener sparkplugListener;

  private final DirectExchange outboundDirectExchange = new DirectExchange("sparkplug-outbound", true, false);
  private final Queue          responseQueue          = new Queue("sparkplug-test-response", true, false, true);

  public SparkplugTestConfiguration() {
    log.debug("Sparkplug Test Spring configuration initialized.");
  }

  /**
   * Register a listener for the response queue/exchange
   */
  @PostConstruct
  public void init() {
    final List<String> wordList;
    try {
      wordList = IOUtils.readLines(new ClassPathResource("wordlist.txt").getInputStream());
    } catch (IOException ie) {
      log.error("Could not load word list.", ie);
      return;
    }

    sparkplugListener.registerCommandHandler("derp", new SimpleSparkplugCommandHandler(wordList));

    amqpAdmin.declareExchange(outboundDirectExchange);
    amqpAdmin.declareQueue(responseQueue);

    final String routingKey = "sparkplug-response";
    final Binding binding = BindingBuilder.bind(responseQueue).to(outboundDirectExchange).with(routingKey);
    amqpAdmin.declareBinding(binding);

    log.debug("Creating response message listener for routing key {}.", routingKey);
    final SimpleMessageListenerContainer listenerContainer = new SimpleMessageListenerContainer(connectionFactory);
    listenerContainer.addQueues(responseQueue);
    listenerContainer.setMessageListener(new MessageListenerAdapter((MessageListener) message ->
                                                                                        log.debug("Received response from Sparkplug; message: {}", message)));
    listenerContainer.start();

    new Thread() {
      @SuppressWarnings("InfiniteLoopStatement")
      @Override
      public void run() {
        // while (true) {
        log.debug("Send thread fired up.");
        try {
          sleep(30000);
        } catch (InterruptedException e) {
          // whatever
        }

        final String uuid = UUID.randomUUID().toString();
        for (int i = 0; i < 10; i++) {
          log.debug("Sending test message {}.", i);
          final MessageProperties messageProperties = new MessageProperties();
          messageProperties.getHeaders().put("uuid", uuid);
          messageProperties.getHeaders().put("order", i);
          messageProperties.getHeaders().put("command", "derp");
          amqpTemplate.send("sparkplug-inbound", "sparkplug-test",
                             new Message(RandomStringUtils.randomAlphanumeric(256).getBytes(), messageProperties));
        }
        //}
      }
    }.start();
  }
}
