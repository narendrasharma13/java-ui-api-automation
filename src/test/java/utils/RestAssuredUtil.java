package utils;

import java.util.Map;

import org.hamcrest.Matchers;

import config.ConfigManager;
import io.restassured.RestAssured;
import io.restassured.filter.log.LogDetail;
import io.restassured.module.jsv.JsonSchemaValidator;
import io.restassured.response.Response;

public class RestAssuredUtil {

	private static final int TOO_MANY_REQUESTS = 429;

	/**
	 * GET baseUrl + path with the given query params. Redirects are not
	 * followed so tests can assert on the real status code. Rate-limited calls
	 * (429) are retried with backoff. Request and response are logged only
	 * when the call does not return 2xx.
	 */
	public Response getWithQueryParamMap(Map<String, ?> queryParams, String baseUrl, String path) {
		int retries = ConfigManager.getInstance().getRetries();
		Response response = get(queryParams, baseUrl + path);
		for (int attempt = 1; attempt <= retries && response.statusCode() == TOO_MANY_REQUESTS; attempt++) {
			System.out.println("Rate limited (429), retry " + attempt + " of " + retries);
			backoff(attempt);
			response = get(queryParams, baseUrl + path);
		}
		return response;
	}

	private Response get(Map<String, ?> queryParams, String url) {
		return RestAssured
				.given()
				.queryParams(queryParams)
				.redirects().follow(false)
				.relaxedHTTPSValidation()
				.when()
				.get(url)
				.then()
				.log().ifStatusCodeMatches(Matchers.allOf(Matchers.greaterThanOrEqualTo(300),
						Matchers.not(TOO_MANY_REQUESTS)))
				.extract().response();
	}

	private void backoff(int attempt) {
		try {
			Thread.sleep(1000L * attempt);
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
		}
	}

	public void validateJsonSchema(Response response, String schemaResourcePath) {
		String schema = FileUtils.readResourceAsString(Constants.SCHEMA_FOLDER + schemaResourcePath);
		response.then().assertThat().body(JsonSchemaValidator.matchesJsonSchema(schema));
	}
}
