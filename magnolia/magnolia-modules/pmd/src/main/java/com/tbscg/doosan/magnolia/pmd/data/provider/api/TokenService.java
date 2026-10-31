package com.tbscg.doosan.magnolia.pmd.data.provider.api;

import com.fasterxml.jackson.databind.JsonNode;
import com.tbscg.doosan.magnolia.pmd.PmdModule;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.message.BasicNameValuePair;

import javax.inject.Inject;
import java.util.Arrays;

@Slf4j
public class TokenService {

    private final PmdModule pmdModule;
    private String token;
    private long tokenExpiration;

    @Inject
    public TokenService(PmdModule pmdModule) {
        this.pmdModule = pmdModule;
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
        HttpService http = new HttpService(pmdModule.getTokenEndpoint());
        http.setHeaders(Arrays.asList(
                new BasicNameValuePair("cache-control", "no-cache"),
                new BasicNameValuePair("accept", "application/json"),
                HttpService.getBasicAuthHeader(pmdModule.getTokenUser(), pmdModule.getTokenPassword())
        ));
        long timestamp = System.currentTimeMillis();
        String result = http.postHttpForm("", Arrays.asList(
                new BasicNameValuePair("grant_type", "client_credentials"),
                new BasicNameValuePair("scope", "product")
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
