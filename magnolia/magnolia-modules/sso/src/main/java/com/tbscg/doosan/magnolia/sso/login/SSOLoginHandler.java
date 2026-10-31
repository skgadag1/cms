/**
 * This file Copyright (c) 2018 Magnolia International
 * Ltd.  (http://www.magnolia-cms.com). All rights reserved.
 *
 *
 * This program and the accompanying materials are made
 * available under the terms of the Magnolia Network Agreement
 * which accompanies this distribution, and is available at
 * http://www.magnolia-cms.com/mna.html
 *
 * Any modifications to this file must keep this entire header
 * intact.
 *
 */
package com.tbscg.doosan.magnolia.sso.login;

import com.github.scribejava.core.model.*;
import com.github.scribejava.core.oauth.OAuth10aService;
import com.github.scribejava.core.oauth.OAuth20Service;
import com.tbscg.doosan.magnolia.sso.SsoDoosanModule;
import com.tbscg.doosan.magnolia.sso.settings.Settings;
import com.tbscg.doosan.magnolia.sso.util.UserAccountUtils;
import info.magnolia.cms.security.LogoutFilter;
import info.magnolia.cms.security.SecuritySupport;
import info.magnolia.cms.security.SecuritySupportBase;
import info.magnolia.cms.security.auth.callback.CredentialsCallbackHandler;
import info.magnolia.cms.security.auth.login.LoginHandler;
import info.magnolia.cms.security.auth.login.LoginResult;
import info.magnolia.connector.sso.SSOConnector;
import info.magnolia.connector.sso.jaas.callback.ExternalUserCallbackHandler;
import info.magnolia.connector.sso.oic.service.OICServiceRequest;
import info.magnolia.connector.sso.oic.settings.OICSettings;
import info.magnolia.connector.sso.util.JSONUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import javax.inject.Inject;
import javax.security.auth.login.LoginException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Map;

/**
 * Login handler listening for SSO services working with authorization token. It uses a default locale in order to avoid
 * 6.2 bug when editing content.
 *
 */
@Slf4j
public class SSOLoginHandler implements LoginHandler {

    private static final String SSO_AUTHENTICATION_REALM = "sso-authentication";

    /**
     * The JAAS chain/module to use.
     */
    private String jaasChain = SecuritySupportBase.DEFAULT_JAAS_LOGIN_CHAIN;

    private final SecuritySupport securitySupport;
    private final SSOConnector ssoConfig;
    private final SsoDoosanModule ssoDoosanModule;

    @Inject
    public SSOLoginHandler(SecuritySupport securitySupport, SSOConnector ssoConfig, UserAccountUtils userAccountUtils, SsoDoosanModule ssoDoosanModule) {
        this.securitySupport = securitySupport;
        this.ssoConfig = ssoConfig;
        this.ssoDoosanModule = ssoDoosanModule;
    }

    @Override
    public LoginResult handle(HttpServletRequest request, HttpServletResponse response) {
        OICServiceRequest oicServiceRequest = (OICServiceRequest) request.getSession().getAttribute("ssoAuthenticationServiceRequest");

        if (oicServiceRequest != null) {
            String authorizationCode = StringUtils.defaultString(request.getParameter(oicServiceRequest.getCallbackParam()), "");
            String state = StringUtils.defaultString(request.getParameter(OAuthConstants.STATE), "");

            boolean isOAuthConnectorLogin = StringUtils.isNotBlank(authorizationCode);
            boolean isLogoutRequest = StringUtils.isNotBlank(request.getParameter(LogoutFilter.PARAMETER_LOGOUT));

            if (!isOAuthConnectorLogin || isLogoutRequest) {
                return LoginResult.NOT_HANDLED;
            }
            // check if the state parameter delivered matches the original version (state is only required for OAuth 2 services)
            if (oicServiceRequest.getOAuth20Service() != null && !StringUtils.equals(state, oicServiceRequest.getState())) {
                return failedLogin("The delivered state does not match the provided one. Login failed for security reasons.");
            }

            if (request.getParameter("error") != null) {
                return new LoginResult(LoginResult.STATUS_FAILED, new LoginException(request.getParameter("error")));
            }

            // only for debugging, no entries in the log
            // log.debug("Authorization code received: " + authorizationCode);
            String authenticationServiceName = oicServiceRequest.getAuthenticationServiceName();
            OICSettings requestSettings = ssoConfig.getAuthenticationServices().get(authenticationServiceName);
            Token token = getAccessToken(oicServiceRequest, authorizationCode, requestSettings);
            Response oauthResponse = getOICResponse(oicServiceRequest, requestSettings, token);
            Settings moduleSettings = ssoDoosanModule.getAuthenticationServices().get(authenticationServiceName);
            String keyAccountDataEndpointURL = null;
            if(moduleSettings != null && StringUtils.isNotBlank(moduleSettings.getKeyAccountDataEndpoint())) {
                keyAccountDataEndpointURL = moduleSettings.getKeyAccountDataEndpoint();
            }

            if (oauthResponse != null && oauthResponse.isSuccessful()) {
                Map<String, String> userDetails = UserAccountUtils.createUserProperties(oauthResponse, requestSettings, authenticationServiceName, token, keyAccountDataEndpointURL);
                if (userDetails != null) {
                    CredentialsCallbackHandler callbackHandler = new ExternalUserCallbackHandler(userDetails, authenticationServiceName, getJaasChain());
                    return authenticate(callbackHandler, getJaasChain());
                } else {
                    return failedLogin("Could not get user properties from JSON response.");
                }
            } else {
                return failedLogin("OpenID Connect response was not successful, login not possible: " + oauthResponse);
            }
        }
        return LoginResult.NOT_HANDLED;
    }

    /**
     * Retrieve the OIC access token and user information needed for creating a user object.
     *
     * @param oicServiceRequest Session object containing information about the initial request.
     * @param requestSettings Configuration of the used service as stored in the JCR.
     * @param token authorization token used to get returned object
     * @return OAuth Response with requested data needed for login.
     */
    private Response getOICResponse(OICServiceRequest oicServiceRequest, OICSettings requestSettings, Token token) {
        Response oauthResponse = null;
        // OAuth 2.x and OpenID Connect services
        if (oicServiceRequest.getOAuth20Service() != null) {
            OAuth20Service oauthService = oicServiceRequest.getOAuth20Service();
            OAuth2AccessToken accessToken = (OAuth2AccessToken) token;
            try {
                String openIdToken = JSONUtils.getOpenIdTokenValue(accessToken, requestSettings.getOpenIdAccessTokenAttributeName());
                oicServiceRequest.setOpenIdToken(openIdToken);
                final OAuthRequest oAuthRequest = new OAuthRequest(Verb.GET, oicServiceRequest.getUserInfoUrl());
                oauthService.signRequest(accessToken, oAuthRequest);
                oauthResponse = oauthService.execute(oAuthRequest);
            } catch (Exception e) {
                log.error("Could not get access token from OAuth 2.x service.", e);
            }
        } else {
            // OAuth 1.x services
            OAuth10aService oauthService = oicServiceRequest.getOAuth10aService();
            final OAuth1AccessToken accessToken = (OAuth1AccessToken) token;
            try {
                OAuthRequest oAuthRequest = new OAuthRequest(Verb.GET, oicServiceRequest.getUserInfoUrl());
                oauthService.signRequest(accessToken, oAuthRequest);
                oauthResponse = oauthService.execute(oAuthRequest);
            } catch (Exception e) {
                log.error("Could not get access token from OAuth 1.x service.", e);
            }
        }
        return oauthResponse;
    }

    private LoginResult failedLogin(String errorMsg) {
        log.error(errorMsg);
        return new LoginResult(LoginResult.STATUS_FAILED, new LoginException(errorMsg));
    }

    protected LoginResult authenticate(CredentialsCallbackHandler callbackHandler, String jaasModuleName) {
        return securitySupport.authenticate(callbackHandler, jaasModuleName);
    }

    private String getJaasChain() {
        return StringUtils.isNotBlank(jaasChain) ? jaasChain : SSO_AUTHENTICATION_REALM;
    }

    @SuppressWarnings("unused")
    public void setJaasChain(String jaasChain) {
        this.jaasChain = jaasChain;
    }

    /**
     * Retrieve the OIC access token needed for creating a user object.
     *
     * @param oicServiceRequest Session object containing information about the initial request.
     * @param authorizationCode Authorization code retrieved after login from the external service, used for gettign the final access token.
     * @param requestSettings Configuration of the used service as stored in the JCR.
     * @return token needed for accessing services behind api
     */
    private Token getAccessToken(OICServiceRequest oicServiceRequest, String authorizationCode, OICSettings requestSettings) {
        if (oicServiceRequest.getOAuth20Service() != null) {
            OAuth20Service oauthService = oicServiceRequest.getOAuth20Service();
            OAuth2AccessToken accessToken;
            try {
                accessToken = oauthService.getAccessToken(authorizationCode);
                // Only enable this for debugging purposes, no access tokens in the log!
                // log.debug("Access token received: " + accessToken);
                // Make usage of the extensions provided by teh OpneID Connect protocol
                if (requestSettings.isOpenIdEnabled()) {
                    if (log.isDebugEnabled()) {
                        log.debug("Using OpenID Connect for SSO authentication.");
                    }
                    if (!JSONUtils.validOpenIdToken(requestSettings, accessToken, oicServiceRequest)) {
                        log.error("The OpenID token could not be validated. Please adjust your configuration. The login process will be cancelled.");
                        return null;
                    }
                }
                return accessToken;
            } catch (Exception e) {
                log.error("Could not get access token from OAuth 2.x service.", e);
            }
        } else {
            // OAuth 1.x services
            OAuth10aService oauthService = oicServiceRequest.getOAuth10aService();
            final OAuth1AccessToken accessToken;
            try {
                accessToken = oauthService.getAccessToken(oicServiceRequest.getRequestToken(), authorizationCode);
                return accessToken;
            } catch (Exception e) {
                log.error("Could not get access token from OAuth 1.x service.", e);
            }
        }
        return null;
    }

}
