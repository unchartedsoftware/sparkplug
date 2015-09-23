package uncharted.sparkplug.message;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

/**
 * Cuz we want to send something back, right?
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Accessors(chain=true)
public class SparkplugResponse {
  private String uuid;
  private byte[] body;
}
