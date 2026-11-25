package driver;

import config.ConfigManager;

import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.StaleElementReferenceException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedCondition;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.FluentWait;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.util.List;

/**
 * Explicit wait utilities with stale element recovery and smart polling.
 */
public final class WaitUtils {

    private WaitUtils() {
    }

    public static WebDriverWait createWait() {
        ConfigManager config = ConfigManager.getInstance();
        return new WebDriverWait(DriverManager.getDriver(), Duration.ofSeconds(config.getTimeout()));
    }

    public static FluentWait<WebDriver> createFluentWait() {
        ConfigManager config = ConfigManager.getInstance();
        return new FluentWait<>(DriverManager.getDriver())
                .withTimeout(Duration.ofSeconds(config.getTimeout()))
                .pollingEvery(Duration.ofMillis(config.getPollingInterval()))
                .ignoring(StaleElementReferenceException.class);
    }

    public static WebElement waitForVisible(By locator) {
        return createWait().until(ExpectedConditions.visibilityOfElementLocated(locator));
    }
    
    public static WebElement waitForVisible(WebElement element) {
        return createWait().until(ExpectedConditions.visibilityOf(element));
    }

    public static WebElement waitForClickable(By locator) {
        return createWait().until(ExpectedConditions.elementToBeClickable(locator));
    }

    public static boolean waitForInvisible(By locator) {
        return createWait().until(ExpectedConditions.invisibilityOfElementLocated(locator));
    }

    public static List<WebElement> waitForAllVisible(By locator) {
        return createWait().until(ExpectedConditions.visibilityOfAllElementsLocatedBy(locator));
    }

    public static boolean waitForUrlContains(String partialUrl) {
        return createWait().until(ExpectedConditions.urlContains(partialUrl));
    }

    public static boolean waitForTitleContains(String title) {
        return createWait().until(ExpectedConditions.titleContains(title));
    }

    public static void waitForPageLoad() {
        ConfigManager config = ConfigManager.getInstance();
        WebDriver driver = DriverManager.getDriver();
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(config.getInt("page.load.timeout", 50)));

        ExpectedCondition<Boolean> pageLoaded = webDriver ->
                ((JavascriptExecutor) webDriver).executeScript("return document.readyState").equals("complete");

        wait.until(pageLoaded);
    }

    public static List<WebElement> waitForMinimumCount(By locator, int count) {
        return createFluentWait().until(d -> {
            List<WebElement> elements = d.findElements(locator);
            return elements.size() >= count ? elements : null;
        });
    }

}
