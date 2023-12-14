package sprest.utils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.regex.Pattern;
import org.mozilla.universalchardet.UniversalDetector;

/**
 */
public abstract class FileUtils {

    private static final Pattern INVALID_PATH_CHARACTERS = Pattern.compile("[\\\\/:\"*?<>|]+");

    public static boolean isValidFolderName(String name) {
        return !INVALID_PATH_CHARACTERS.matcher(name).find();
    }

    public static boolean isUtf8(File file) throws IOException {
        String encoding = UniversalDetector.detectCharset(file);

        return Charset.forName(encoding).contains(StandardCharsets.UTF_8);
    }

    public static boolean isUtf8(InputStream file) throws IOException {
        file.mark(Integer.MAX_VALUE);
        String encoding = UniversalDetector.detectCharset(file);
        file.reset();
        return Charset.forName(encoding).contains(StandardCharsets.UTF_8);
    }
}
