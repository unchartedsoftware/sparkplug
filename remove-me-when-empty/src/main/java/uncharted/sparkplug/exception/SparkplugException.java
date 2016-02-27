package uncharted.sparkplug.exception;

/**
 * Top level exception for Sparkplug
 */
public class SparkplugException extends Exception {
  public SparkplugException() {
    super();
  }

  public SparkplugException(final Throwable cause) {
    super(cause);
  }

  public SparkplugException(final String message) {
    super(message);
  }

  public SparkplugException(final String message, final Throwable cause) {
    super(message, cause);
  }

  public SparkplugException(final String message, final Throwable cause, final boolean enableSuppression, final boolean writableStackTrace) {
    super(message, cause, enableSuppression, writableStackTrace);
  }
}
