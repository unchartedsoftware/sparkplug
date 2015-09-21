package uncharted.sparkplug.test.listener;

import lombok.extern.slf4j.Slf4j;
import org.apache.spark.api.java.JavaPairRDD;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;
import scala.Tuple2;
import uncharted.sparkplug.listener.SparkplugListener;
import uncharted.sparkplug.message.SparkplugMessage;
import uncharted.sparkplug.message.SparkplugResponse;

import java.util.List;
import java.util.Map;

/**
 * Basic implementation of the SparkplugAdapter
 */
@Slf4j
public class SimpleSparkplugListener implements SparkplugListener {
  private final List<String> words;

  public SimpleSparkplugListener(final List<String> words) {
    this.words = words;
  }

  @Override
  public SparkplugResponse onMessage(final JavaSparkContext sparkContext, final SparkplugMessage message) {
    log.debug("Sparkplug SimpleSparkplugListener handling a message: {}", message);

    // create some in memory "words"
    final JavaRDD<String> wordsRdd = sparkContext.parallelize(words).sample(false, 0.10d);
    final JavaPairRDD<String, Integer> pairs = wordsRdd.mapToPair(word -> new Tuple2<>(word, 1));
    final JavaPairRDD<String, Integer> counts = pairs.reduceByKey((a, b) -> a + b);

    final Map<String, Integer> collected = counts.collectAsMap();

    final String responseMessage = String.format("Collected results include %d keys and %d values.", collected.keySet().size(), collected.values().size());
    log.debug(responseMessage);

    try {
      log.debug("Going to sleep for a while.");
      Thread.sleep(5000);
    } catch (InterruptedException e) {
      // whatever
    }

    return new SparkplugResponse(message.getUuid(), responseMessage.getBytes());
  }
}
