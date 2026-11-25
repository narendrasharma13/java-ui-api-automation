package com.twitch.tests;

import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Listeners;

import driver.BrowserFactory;
import driver.DriverManager;
import testngListener.TestListener;

/**
 * Gives every UI test its own fresh browser. Screenshots on failure are taken
 * by {@link TestListener} before the browser is closed.
 */
@Listeners(TestListener.class)
public class BaseTest {

	@BeforeMethod(alwaysRun = true)
	public void startBrowser() {
		DriverManager.setDriver(BrowserFactory.createDriver());
	}

	@AfterMethod(alwaysRun = true)
	public void closeBrowser() {
		DriverManager.quitDriver();
	}

}
