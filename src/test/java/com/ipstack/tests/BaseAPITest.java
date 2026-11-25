package com.ipstack.tests;

import java.util.Map;

import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Listeners;

import config.ConfigManager;
import io.restassured.response.Response;
import testngListener.TestListener;
import utils.RestAssuredUtil;

@Listeners(TestListener.class)
public class BaseAPITest {

	protected ConfigManager config;
	protected RestAssuredUtil restClient;

	@BeforeClass(alwaysRun = true)
	public void setupClient() {
		config = ConfigManager.getInstance();
		restClient = new RestAssuredUtil();
	}

	/** Standard lookup: GET {baseUrl}/{ip}?access_key=... */
	protected Response lookup(String ip, Map<String, ?> queryParams) {
		return restClient.getWithQueryParamMap(queryParams, config.getBaseUrl(), ip);
	}

	protected Response lookup(String ip) {
		return lookup(ip, Map.of("access_key", config.getApiKey()));
	}

	protected void verifyResponse(Response res, String jsonPath, String expectedValue) {
		String actual = res.jsonPath().getString(jsonPath);
		Assert.assertEquals(actual, expectedValue, "Unexpected value at '" + jsonPath + "'");
	}

	/** IPStack error body: {"success":false,"error":{"code":..,"type":..,"info":..}} */
	protected void verifyError(Response res, int expectedStatus, int expectedCode, String expectedType) {
		Assert.assertEquals(res.statusCode(), expectedStatus, "Unexpected HTTP status");
		verifyResponse(res, "success", "false");
		verifyResponse(res, "error.code", String.valueOf(expectedCode));
		verifyResponse(res, "error.type", expectedType);
		Assert.assertFalse(res.jsonPath().getString("error.info").isBlank(), "error.info should explain the error");
	}
}
