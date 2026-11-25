package com.twitch.pages;

import org.openqa.selenium.By;

import config.ConfigManager;
import driver.WaitUtils;

public class HomePage extends BasePage {

	private By browse = By.xpath("//*[@id='root']//*[text()='Browse']");
	private By search = By.xpath("(//input[@type='search'])[1]");
	private By searchResults = By.xpath("//li[contains(@class,'Layout')]//img/ancestor::a//div[2] | //div[contains(@id,'search-result-row')]");

	public HomePage open() {
		driver.get(ConfigManager.getInstance().getUrl());
		waitForPageReady();
		return this;
	}

	/**
	 * Searches from the Browse page and opens the result at the given 1-based
	 * position. Returns the name of the clicked result.
	 */
	public String browseChannel(String searchKeyword, int itemNo) {
		// the tab can be clicked before Twitch's React app is ready, so retry until we land on Browse
		WaitUtils.createFluentWait().until(d -> {
			click(browse);
			return d.getCurrentUrl().contains("/directory");
		});
		click(search);
		type(search, searchKeyword);
		String channelName = clickElementByIndexReturnValue(searchResults, itemNo);
		waitForPageReady();
		return channelName;
	}

}
