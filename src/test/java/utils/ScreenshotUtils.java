package utils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;

import config.ConfigManager;
import driver.DriverManager;

public final class ScreenshotUtils {

	private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss-SSS");

	private ScreenshotUtils() {
	}

	/**
	 * Saves a screenshot of the current thread's browser. Returns the absolute
	 * path, or null when there is no browser (e.g. API tests).
	 */
	public static String capture(String testName) {
		if (!DriverManager.isDriverInitialized()) {
			return null;
		}
		try {
			String screenshotDir = ConfigManager.getInstance().get("screenshot.path", "screenshots");
			FileUtils.ensureDirectoryExists(screenshotDir);
			String fileName = testName + "_" + LocalDateTime.now().format(FORMATTER) + ".png";
			Path destination = Path.of(screenshotDir, fileName).toAbsolutePath();

			byte[] screenshotBytes = ((TakesScreenshot) DriverManager.getDriver()).getScreenshotAs(OutputType.BYTES);
			Files.write(destination, screenshotBytes);
			return destination.toString();
		} catch (IOException | RuntimeException e) {
			System.err.println("Could not capture screenshot: " + e.getMessage());
			return null;
		}
	}
}
