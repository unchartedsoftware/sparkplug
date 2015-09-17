package uncharted.sparkplug.adapter;

/**
 * Contract to handle messages received by Sparkplug
 */
public interface SparkplugAdapter {
  void onMessage(final byte[] message);
}
