package com.twitch.tests;

import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import com.twitch.pages.ChannelPage;
import com.twitch.pages.HomePage;

public class VerifyChannel extends BaseTest {

	@DataProvider(name = "searchData")
	public Object[][] searchData() {
		return new Object[][] {
			// search keyword, position of result to open (1-based)
			{ "StarCraft II", 2 },
		};
	}

	@Test(description = "TC_UI_01 - Verify searched result opens the matching page on Twitch mobile",
			dataProvider = "searchData")
	public void verifyIfDropdownOpensCorrectChannel(String keyword, int position) {

		String channelName = new HomePage().open().browseChannel(keyword, position);
		Assert.assertNotNull(channelName, "No search result found at position " + position);

		ChannelPage channelPage = new ChannelPage();
		String heading = channelPage.getHeading();
		Assert.assertTrue(channelName.contains(heading),
				"Page heading '" + heading + "' does not match the clicked result '" + channelName + "'");
		Assert.assertTrue(channelPage.isContentLoaded(), "Page content (thumbnails) did not load");
	}

}
