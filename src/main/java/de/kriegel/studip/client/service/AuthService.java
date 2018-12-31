package de.kriegel.studip.client.service;

import java.io.IOException;
import java.net.URISyntaxException;

import org.apache.http.HttpResponse;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import de.kriegel.studip.client.config.Endpoints;
import de.kriegel.studip.client.config.SubPaths;
import de.kriegel.studip.client.content.model.data.Id;
import de.kriegel.studip.client.content.model.data.User;
import de.kriegel.studip.client.exception.NotAuthenticatedException;

public class AuthService {

	private static final Logger log = LogManager.getLogger(AuthService.class);

	private final BasicHttpClient httpClient;

	private boolean isAuthenticated;
	private Id currentUserId;
	private String authErrorResponse = "";

	public AuthService(BasicHttpClient httpClient) {
		this.httpClient = httpClient;

		isAuthenticated = false;

	}

	public boolean authenticate() {
		HttpResponse response;
		try {
			response = httpClient.get(SubPaths.API.toString() + Endpoints.USER.toString());
		} catch (URISyntaxException | IOException e) {
			e.printStackTrace();
			isAuthenticated = false;
			authErrorResponse = e.getClass().getName() + ": " + e.getMessage();
			return false;
		}

		if (response.getStatusLine().getStatusCode() / 100 == 2) {
			isAuthenticated = true;

			String responseBody = BasicHttpClient.getResponseBody(response);
			log.debug(responseBody);

			try {
				User user = User.fromJson((JSONObject) new JSONParser().parse(responseBody));
				currentUserId = user.getId();
			} catch (ParseException e) {
				e.printStackTrace();
			}

			return true;
		} else {
			authErrorResponse = BasicHttpClient.getResponseBody(response);
		}

		isAuthenticated = false;
		return false;
	}

	public String getAuthErrorResponse() {
		return authErrorResponse;
	}

	public boolean isAuthenticated() {
		return isAuthenticated;
	}

	public boolean checkIfAuthenticated() throws NotAuthenticatedException {
		if (!isAuthenticated) {
			throw new NotAuthenticatedException();
		} else {
			return isAuthenticated;
		}
	}

	public Id getCurrentUserId() {
		return currentUserId;
	}

}
