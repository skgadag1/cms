package com.tbscg.doosan.magnolia.sso.service;

import com.github.scribejava.core.model.OAuth1AccessToken;
import com.github.scribejava.core.model.OAuth2AccessToken;
import com.github.scribejava.core.model.Token;
import com.google.gson.JsonObject;
import info.magnolia.connector.sso.util.JSONUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.client.HttpResponseException;
import org.apache.http.client.fluent.Request;
import org.apache.logging.log4j.util.Strings;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

import static com.tbscg.doosan.magnolia.sso.util.UserAccountUtils.COMPANY_ID_FIELD;
import static com.tbscg.doosan.magnolia.sso.util.UserAccountUtils.COMPANY_CODE_FIELD;

@Slf4j
public class KeyAccountService {

    private static final String COMPANY_NAME = "companyName";
    private static final String USER_GROUPS = "UserGroups";

    /**
     * Gets data from iam-inner-api-DomainToKeyAccountAndSabaOrgTable matching user's email address domain.
     *
     * @param url endpoint's URL to call
     * @param token authorization token to use
     * @param email user's email address
     * @return map with company code and company ID if present
     */
    public static Map<String, String> fetchKeyAccountAndSabaOrgData(String url, Token token, String email) {
        Map<String, String> results = new HashMap<>();
        String accessToken = extractAccessToken(token);
        if(accessToken != null) {
            try {
                String targetURL = url.replace("{email}", URLEncoder.encode(email, StandardCharsets.UTF_8.toString()));
                if (log.isDebugEnabled()) {
                    log.debug("Calling KeyAccountAndSabaOrg endpoint: {}", targetURL);
                }
                String response = Request.Get(targetURL)
                        .connectTimeout(5000)
                        .socketTimeout(5000)
                        .addHeader("Authorization", "Bearer " + accessToken)
                        .execute().returnContent().asString();
                if (log.isDebugEnabled()) {
                    log.debug("Received response: {}", response);
                }
                extractData(results, response);
            } catch (Exception e) {
                if(e instanceof HttpResponseException && ((HttpResponseException)e).getStatusCode() == 404) {
                    if (log.isDebugEnabled()) {
                        log.debug("Received response: not found");
                    }
                } else {
                    log.error("Couldn't get response from endpoint: {}", e.getMessage());
                    return null;
                }
            }
            return results;
        } else {
            log.error("Couldn't extract access token");
            return null;
        }
    }
    private static void extractData(Map<String, String> results, String data) {
        JsonObject jsonObject = JSONUtils.getJsonObject(data, "", "");
        String companyCode = JSONUtils.getFieldValue(jsonObject, COMPANY_NAME);
        if(Strings.isNotBlank(companyCode)) {
            results.put(COMPANY_CODE_FIELD, companyCode);
        }
        String[] companyIds = JSONUtils.getFieldValue(jsonObject, USER_GROUPS).split(",");
        if (companyIds.length > 0) {
            String companyId = companyIds[0];
            if(Strings.isNotBlank(companyId)) {
                results.put(COMPANY_ID_FIELD, companyId);
            }
        }
    }
    private static String extractAccessToken(Token token) {
        if(token instanceof OAuth2AccessToken) {
            return ((OAuth2AccessToken) token).getAccessToken();
        } else if(token instanceof OAuth1AccessToken) {
            return ((OAuth1AccessToken) token).getToken();
        } else {
            return null;
        }
    }
}
