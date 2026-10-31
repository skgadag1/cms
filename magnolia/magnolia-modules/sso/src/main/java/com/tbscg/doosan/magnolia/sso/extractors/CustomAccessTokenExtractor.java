package com.tbscg.doosan.magnolia.sso.extractors;

import com.fasterxml.jackson.databind.JsonNode;
import com.github.scribejava.apis.openid.OpenIdJsonTokenExtractor;
import com.github.scribejava.apis.openid.OpenIdOAuth2AccessToken;
import info.magnolia.context.MgnlContext;
import lombok.extern.slf4j.Slf4j;

import java.util.regex.Pattern;

@Slf4j
public class CustomAccessTokenExtractor extends OpenIdJsonTokenExtractor {
    public static final String ACCESS_TOKEN = "access_token";
    public static final String ID_TOKEN = "id_token";

    private static CustomAccessTokenExtractor _instance;

    public static CustomAccessTokenExtractor instance() {
        if(_instance == null)
            _instance = new CustomAccessTokenExtractor();
        return _instance;
    }

    @Override
    protected OpenIdOAuth2AccessToken createToken(String accessToken, String tokenType, Integer expiresIn, String refreshToken, String scope, JsonNode response, String rawResponse) {
        final JsonNode tokenObject = response.get(ID_TOKEN);
        String idToken = tokenObject == null ? null : tokenObject.textValue();

        final OpenIdOAuth2AccessToken token = new OpenIdOAuth2AccessToken(accessToken, tokenType, expiresIn, refreshToken, scope,
                idToken, rawResponse);


        log.debug("ACCESS TOKEN: " + token.getAccessToken());
        MgnlContext.setAttribute(ACCESS_TOKEN, token.getAccessToken());

        if (token.getOpenIdToken() != null) {
            log.debug("OPENID TOKEN: " + token.getOpenIdToken());
            MgnlContext.setAttribute(ID_TOKEN, token.getOpenIdToken());
        }

        return token;
    }
}