package uncharted.sparkplug.listener;

import lombok.extern.slf4j.Slf4j;
import org.apache.spark.api.java.JavaSparkContext;
import uncharted.sparkplug.exception.SparkplugException;
import uncharted.sparkplug.handler.CommandHandler;
import uncharted.sparkplug.message.SparkplugMessage;
import uncharted.sparkplug.message.SparkplugResponse;

import java.util.HashMap;
import java.util.Map;

/**
 * Contract to handle messages received by Sparkplug
 */
@Slf4j
public final class SparkplugListener {
  private final Map<String, CommandHandler> commandHandlers = new HashMap<>();

  public void registerCommandHandler(final String command, final CommandHandler handler) {
    if (commandHandlers.containsKey(command)) {
      log.error("Another Sparkplug handler is already listening for command {}.", command);
      throw new IllegalArgumentException(String.format("Another Sparkplug handler is already listening for command %s.", command));
    }

    commandHandlers.put(command, handler);
  }

  final SparkplugResponse onMessage(final JavaSparkContext sparkContext, final SparkplugMessage message) throws SparkplugException {
    if (commandHandlers.isEmpty()) {
      log.error("No command handlers registered, cannot process message.");
      throw new SparkplugException("No command handlers registered, cannot process message.");
    }

    if (!commandHandlers.containsKey(message.getCommand())) {
      log.error("No handler has been registered for command {}, cannot process message.", message.getCommand());
      throw new SparkplugException(String.format("No handler has been registered for command %s, cannot process message.", message.getCommand()));
    }

    final CommandHandler commandHandler = commandHandlers.get(message.getCommand());
    return commandHandler.onMessage(sparkContext, message);
  }
}
