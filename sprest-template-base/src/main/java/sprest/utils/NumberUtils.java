package sprest.utils;

import java.text.NumberFormat;
import java.text.ParseException;
import java.util.Locale;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

@Slf4j
public abstract class NumberUtils {

  private static final NumberFormat DECIMAL_FORMAT = NumberFormat.getInstance(Locale.GERMAN);

  public static Double tryParseDouble(String value) {
    if (StringUtils.isBlank(value)) {
      return null;
    }

    try {
      return DECIMAL_FORMAT.parse(value).doubleValue();
    } catch (NumberFormatException | ParseException e) {
      log.debug("Failed to parse {} as double.", value);

      return null;
    }
  }

  public static Integer percentDoubleToInteger(Double value) {
      if (value == null) {
          return null;
      }

      return (int) Math.round(value * 100);
  }

  public static Double percentIntegerToDouble(Integer value) {
      if (value == null) {
          return null;
      }

      return (double) value / 100;
  }
}
