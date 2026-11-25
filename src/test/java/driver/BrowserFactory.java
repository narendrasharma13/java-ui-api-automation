package driver;

import java.time.Duration;
import java.util.List;
import java.util.Map;

import org.openqa.selenium.PageLoadStrategy;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.edge.EdgeDriver;
import org.openqa.selenium.edge.EdgeOptions;

import config.ConfigManager;
import io.github.bonigarcia.wdm.WebDriverManager;
import utils.Constants;

public class BrowserFactory {

	private BrowserFactory() {
	}

	public static WebDriver createDriver() {
		ConfigManager config = ConfigManager.getInstance();
		String browser = config.getBrowser();
		boolean headless = config.isHeadless();
		String device = config.isMobileEmulation() ? config.getDeviceName() : null;

		WebDriver driver;
		switch (browser) {
		case Constants.CHROME:
			driver = createChromeDriver(headless, device);
			break;
		case Constants.EDGE:
			driver = createEdgeDriver(headless, device);
			break;
		default:
			throw new IllegalArgumentException("Unsupported browser: " + browser + " (supported: chrome, edge)");
		}

		if (!headless && device == null) {
			driver.manage().window().maximize();
		}
		configureTimeouts(driver, config);
		return driver;
	}

	/**
	 * @param deviceName Chrome DevTools device to emulate, or null for a normal
	 *                   desktop window
	 */
	private static WebDriver createChromeDriver(boolean headless, String deviceName) {
		WebDriverManager.chromedriver().setup();
		ChromeOptions options = new ChromeOptions();
		if (headless) {
			options.addArguments("--headless=new");
			// headless has no screen to maximize to, so give it a desktop-sized window
			options.addArguments("--window-size=1920,1080");
		} else {
			options.addArguments("--start-maximized");
		}
		if (deviceName != null) {
			options.setExperimentalOption("mobileEmulation", Map.of("deviceName", deviceName));
		}

		// clean, distraction-free browser for stable runs
		options.addArguments("--disable-notifications");
		options.addArguments("--disable-popup-blocking");
		options.addArguments("--disable-infobars");
		options.addArguments("--disable-extensions");
		options.addArguments("--disable-search-engine-choice-screen");
		options.addArguments("--no-first-run");
		options.addArguments("--no-default-browser-check");
		options.addArguments("--lang=en-US");
		options.addArguments("--remote-allow-origins=*");
		// needed in containers / CI agents
		options.addArguments("--no-sandbox");
		options.addArguments("--disable-dev-shm-usage");

		// hide the "Chrome is being controlled by automated test software" bar
		options.setExperimentalOption("excludeSwitches", List.of("enable-automation"));
		// no "save password?" bubbles or notification permission prompts
		options.setExperimentalOption("prefs", Map.of(
				"credentials_enable_service", false,
				"profile.password_manager_enabled", false,
				"profile.default_content_setting_values.notifications", 2));

		options.setAcceptInsecureCerts(true);
		options.setPageLoadStrategy(PageLoadStrategy.NORMAL);
		return new ChromeDriver(options);
	}

	private static WebDriver createEdgeDriver(boolean headless, String deviceName) {
		WebDriverManager.edgedriver().setup();
		EdgeOptions options = new EdgeOptions();
		if (headless) {
			options.addArguments("--headless=new");
			options.addArguments("--window-size=1920,1080");
		} else {
			options.addArguments("--start-maximized");
		}
		if (deviceName != null) {
			options.setExperimentalOption("mobileEmulation", Map.of("deviceName", deviceName));
		}
		options.addArguments("--disable-notifications");
		options.setExperimentalOption("excludeSwitches", List.of("enable-automation"));
		return new EdgeDriver(options);
	}

	private static void configureTimeouts(WebDriver driver, ConfigManager config) {
		driver.manage().timeouts().implicitlyWait(Duration.ZERO);
		driver.manage().timeouts().pageLoadTimeout(Duration.ofSeconds(config.getInt("page.load.timeout", 30)));
		driver.manage().timeouts().scriptTimeout(Duration.ofSeconds(config.getInt("script.timeout", 30)));
	}

}
