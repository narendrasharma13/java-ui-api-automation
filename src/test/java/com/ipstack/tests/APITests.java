package com.ipstack.tests;

import java.util.Map;

import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import io.restassured.response.Response;

public class APITests extends BaseAPITest {

	@DataProvider(name = "validIps")
	public Object[][] validIps() {
		return new Object[][] {
			// ip, expected type, expected country code
			{ "134.201.250.155", "ipv4", "US" },
			{ "2001:4860:4860::8888", "ipv6", "US" },
		};
	}

	@DataProvider(name = "invalidIps")
	public Object[][] invalidIps() {
		return new Object[][] {
			{ "134.201.250.1555" },  // octet out of range
			{ "134.201.250" },       // too few octets
			{ "not-an-ip" },
		};
	}

	@Test(description = "TC_API_01 - Verify standard lookup returns location data for a valid IP",
			dataProvider = "validIps")
	public void verifySingleIPLookupSuccess(String ip, String type, String countryCode) {
		Response response = lookup(ip);

		Assert.assertEquals(response.statusCode(), 200);
		verifyResponse(response, "ip", ip);
		verifyResponse(response, "type", type);
		verifyResponse(response, "country_code", countryCode);
		restClient.validateJsonSchema(response, "schema.json");
	}

	@Test(description = "TC_API_02 - Verify standard lookup rejects an invalid IP",
			dataProvider = "invalidIps")
	public void verifyStandarIPLookupInvalid(String ip) {
		Response response = lookup(ip);

		verifyError(response, 400, 106, "invalid_ip_address");
	}

	@Test(description = "TC_API_03 - Verify standard lookup fails with an invalid access key")
	public void verifyStandarIPLookupInvalidAccessKey() {
		Response response = lookup("134.201.250.155", Map.of("access_key", "1234567890"));

		verifyError(response, 401, 101, "invalid_access_key");
		Assert.assertNull(response.jsonPath().get("country_code"), "No location data must leak without a valid key");
	}

	@Test(description = "TC_API_04 - Verify standard lookup fails when the access key is missing")
	public void verifyStandarIPLookupMissingAccessKey() {
		Response response = lookup("134.201.250.155", Map.of());

		verifyError(response, 401, 101, "missing_access_key");
	}

	@Test(description = "TC_API_05 - Verify lookup without the mandatory IP path parameter returns no lookup data")
	public void verifyStandarIPLookupWithoutIP() {
		Response response = lookup("");

		// IPStack redirects the bare endpoint to its website instead of answering with JSON
		Assert.assertEquals(response.statusCode(), 302);
		Assert.assertFalse(response.contentType().contains("json"), "Bare endpoint must not return lookup JSON");
	}
}
