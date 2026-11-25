package com.twitch.pages;

import org.openqa.selenium.By;

public class ChannelPage extends BasePage {

	private By pageHeading = By.xpath("//h1[contains(@class,'CoreText')]");
	private By loadingSpinner = By.xpath("//*[contains(@class,'LoadingSpinner')]");
	private By thumbnail = By.xpath("//*[@class='tw-image']");

	public String getHeading() {
		return getText(pageHeading).trim();
	}

	/**
	 * Waits until the page has finished loading its content (spinner gone and
	 * at least one stream/video thumbnail rendered).
	 */
	public boolean isContentLoaded() {
		waitForInvisiblityOf(loadingSpinner);
		return isDisplayed(thumbnail);
	}

}
