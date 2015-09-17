package uncharted.sparkplug.message;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

/**
 * Just in case...
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Accessors(chain=true)
public class SparkplugMessage {
  private String uuid;
  private String command;
  private byte[] body;
}
