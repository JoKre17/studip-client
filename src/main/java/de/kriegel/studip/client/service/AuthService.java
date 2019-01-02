package de.kriegel.studip.client.service;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.concurrent.ExecutionException;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.kriegel.studip.client.config.Endpoints;
import de.kriegel.studip.client.config.SubPaths;
import de.kriegel.studip.client.content.model.data.Id;
import de.kriegel.studip.client.content.model.data.User;
import de.kriegel.studip.client.exception.NotAuthenticatedException;
import okhttp3.Response;

public class AuthService {

    private static final Logger log = LoggerFactory.getLogger(AuthService.class);

    private final BasicHttpClient httpClient;

    private boolean isAuthenticated;
    private Id currentUserId;
    private String authErrorResponse = "";

    public AuthService(BasicHttpClient httpClient) {
        this.httpClient = httpClient;

        isAuthenticated = false;

    }

    public boolean authenticate() {


        Response response;
        try {
            response = httpClient.get(SubPaths.API.toString() + Endpoints.USER.toString()).get();

            if (response.isSuccessful()) {
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

        } catch (URISyntaxException | IOException e) {
            e.printStackTrace();
            isAuthenticated = false;
            authErrorResponse = e.getClass().getName() + ": " + e.getMessage();
            return false;
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }

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
