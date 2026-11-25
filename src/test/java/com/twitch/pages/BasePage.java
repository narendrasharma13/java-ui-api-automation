package com.twitch.pages;

import java.util.List;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import driver.DriverManager;
import driver.WaitUtils;

public class BasePage {

	protected final WebDriver driver;

	protected BasePage() {
		this.driver = DriverManager.getDriver();
	}

	protected void click(By locator) {
		WaitUtils.waitForClickable(locator).click();
	}

	protected void type(By locator, String txt) {
		WebElement element = WaitUtils.waitForVisible(locator);
		element.clear();
		element.sendKeys(txt);
	}

	protected String getText(By locator) {
		return WaitUtils.waitForVisible(locator).getText();
	}

	protected boolean isDisplayed(By locator) {
		return WaitUtils.waitForVisible(locator).isDisplayed();
	}

	protected List<WebElement> findElements(By locator) {
		return WaitUtils.waitForAllVisible(locator);
	}

	protected void waitForElement(By locator) {
		WaitUtils.waitForVisible(locator);
	}

	/**
	 * Clicks the element at the given 1-based position and returns its text.
	 */
	protected String clickElementByIndexReturnValue(By locator, int index) {
		if (index < 1) {
			throw new IllegalArgumentException("Index is 1-based, got: " + index);
		}
		List<WebElement> list = WaitUtils.waitForMinimumCount(locator, index);
		WebElement element = WaitUtils.waitForVisible(list.get(index - 1));
		String value = element.getText();
		element.click();
		return value;
	}

	protected void waitForPageReady() {
		WaitUtils.waitForPageLoad();
	}

	protected void waitForInvisiblityOf(By locator) {
		WaitUtils.waitForInvisible(locator);
	}

	protected String getPageTitle() {
		return driver.getTitle();
	}

	protected String getCurrentUrl() {
		return driver.getCurrentUrl();
	}

}
