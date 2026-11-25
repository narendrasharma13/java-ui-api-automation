package utils;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;

public final class FileUtils {

    private FileUtils() {
    }

    public static void ensureDirectoryExists(String directoryPath) {
        try {
            Files.createDirectories(Path.of(directoryPath));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static InputStream getResourceAsStream(String resourcePath) {
        InputStream stream = Thread.currentThread().getContextClassLoader().getResourceAsStream(resourcePath);
        return stream;
    }

    public static String readResourceAsString(String resourcePath) {
        try (InputStream stream = getResourceAsStream(resourcePath)) {
            return new String(stream.readAllBytes());
        } catch (IOException e) {
        	e.printStackTrace();
        }
		return null;
    }
}
