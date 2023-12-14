package sprest.utils;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.IOException;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.core.io.ClassPathResource;

/**
 * Unit tests for {@link FileUtils} class.
 *
 */
class FileUtilsTest {

  @ParameterizedTest
  @CsvSource(value = {
      "test true",
      "t|est false",
      "te*t false",
      "te?t false",
      "te\"t false",
      "te\\st false",
      "te?t false",
      "tes/t false",
      "te:st false",
      "te<t false",
      "te>t false"
  }, delimiter = ' ')
  public void mustValidateDirectoryName(String name, String isValid) {
    assertEquals(Boolean.valueOf(isValid), FileUtils.isValidFolderName(name));
  }

  @ParameterizedTest
  @CsvSource(value = {
      "/file_utf8.txt true",
      "/file_utf8bom.txt true",
      "/file_ascii.txt false",
      "/file_iso8859-2.txt false",
  }, delimiter = ' ')
  public void mustDetectUtfEncoding(String fileName, String isValid) throws IOException {
    var f = new ClassPathResource(fileName).getFile();
    assertEquals(Boolean.valueOf(isValid), FileUtils.isUtf8(f));

  }
}