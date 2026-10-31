package com.tbscg.doosan.magnolia.passport.data.provider.api;

import com.fasterxml.jackson.databind.JsonNode;
import com.tbscg.doosan.magnolia.passport.PassportModule;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.message.BasicNameValuePair;

import javax.inject.Inject;
import java.util.Arrays;

@Slf4j
public class TokenService {

    private final PassportModule passportModule;
    private String token;
    private long tokenExpiration;

    @Inject
    public TokenService(PassportModule passportModule) {
        this.passportModule = passportModule;
    }

    public synchronized String getToken() {
        if (!isTokenValid()) {
            refreshToken();
        }
        return this.token;
    }

    private boolean isTokenValid() {
        return this.token != null && System.currentTimeMillis() < this.tokenExpiration;
    }

    private void refreshToken() {
        HttpService http = new HttpService(passportModule.getTokenEndpoint());
        http.setHeaders(Arrays.asList(
                new BasicNameValuePair("cache-control", "no-cache"),
                new BasicNameValuePair("accept", "application/json"),
                HttpService.getBasicAuthHeader(passportModule.getTokenUser(), passportModule.getTokenPassword())
        ));
        long timestamp = System.currentTimeMillis();
        String result = http.postHttpForm("", Arrays.asList(
                new BasicNameValuePair("grant_type", "client_credentials"),
                new BasicNameValuePair("scope", "ngpassport:documents")
        ));
        JsonNode json = ApiMapper.jsonNode(result);
        if (json != null) {
            JsonNode accessToken = json.get("access_token");
            JsonNode expiresIn = json.get("expires_in");
            if (accessToken != null && expiresIn != null) {
                this.token = accessToken.asText();
                this.tokenExpiration = timestamp + (expiresIn.asLong() * 1000);
                return;
            }
        }
        throw new RuntimeException("Invalid token response: " + result);
    }
}
