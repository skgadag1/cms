package com.tbscg.doosan.magnolia.sso.filter;

import com.tbscg.doosan.magnolia.dxp.core.handler.ErrorHandler;
import info.magnolia.audit.AuditLoggingUtil;
import info.magnolia.cms.security.User;
import info.magnolia.cms.security.auth.login.FormLogin;
import info.magnolia.cms.security.auth.login.LoginHandler;
import info.magnolia.cms.security.auth.login.LoginResult;
import info.magnolia.cms.security.auth.login.SSOAuthenticationLoginFilter;
import info.magnolia.cms.util.RequestDispatchUtil;
import info.magnolia.connector.sso.SSOConnector;
import info.magnolia.connector.sso.oic.service.OICServiceRequest;
import info.magnolia.context.MgnlContext;
import info.magnolia.monitoring.AccessRestrictedException;
import info.magnolia.monitoring.SystemMonitor;
import info.magnolia.objectfactory.Components;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.security.auth.Subject;
import javax.security.auth.login.CredentialException;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.Temporal;

import static com.tbscg.doosan.magnolia.sso.extractors.CustomAccessTokenExtractor.ACCESS_TOKEN;
import static com.tbscg.doosan.magnolia.sso.extractors.CustomAccessTokenExtractor.ID_TOKEN;

@Slf4j
public class SSOAuthenticationTokenLoginFilter extends SSOAuthenticationLoginFilter {

    public static final String SSO_AUTHENTICATION_SERVICE_NAME = "ssoAuthenticationServiceName";

    private ErrorHandler errorHandler;

    public SSOAuthenticationTokenLoginFilter(SSOConnector ssoConfig) {
        super(ssoConfig);
    }

    @Override
    public void doFilter(HttpServletRequest request, HttpServletResponse response, FilterChain chain) throws IOException, ServletException {

        for (LoginHandler handler : this.getLoginHandlers()) {
            LoginResult loginResult = handler.handle(request, response);
            LoginResult.setCurrentLoginResult(loginResult);

            log.debug("login handler {} result: {}", handler.getClass(), loginResult.getStatus());

            if (loginResult.getStatus() == LoginResult.STATUS_FAILED) {
                String errorMsg = "Invalid login result from handler [" + handler.getClass().getName() + "] returned STATUS_FAILED";
                log.error(errorMsg);
                AuditLoggingUtil.log(loginResult, request);
                errorHandler.handle(request, response, loginResult.getLoginException());
                return;
            } else  if (loginResult.getStatus() == LoginResult.STATUS_IN_PROCESS) {
                // special handling to support multi step login mechanisms like ntlm
                // do not continue with the filter chain
                AuditLoggingUtil.log(loginResult, request);
                return;
            } else if (loginResult.getStatus() == LoginResult.STATUS_SUCCEEDED ||
                    loginResult.getStatus() == LoginResult.STATUS_SUCCEEDED_REDIRECT_REQUIRED) {

                if (Components.getComponent(SystemMonitor.class).isMemoryLimitReached()) {
                    final String memoryLimitReachedMessage = String.format(SystemMonitor.MEMORY_LIMIT_IS_REACHED_STRING_FORMAT, "That is why further logins have to be blocked for now.");
                    log.error(memoryLimitReachedMessage);
                    LoginResult memoryLimitResult = new LoginResult(LoginResult.STATUS_FAILED, new AccessRestrictedException());
                    LoginResult.setCurrentLoginResult(memoryLimitResult);
                    AuditLoggingUtil.log(memoryLimitResult, request);
                    break;
                }

                Subject subject = loginResult.getSubject();
                if (subject == null) {
                    String errorMsg = "Invalid login result from handler [" + handler.getClass().getName() + "] returned STATUS_SUCCEEDED but no subject";
                    log.error(errorMsg);
                    LoginResult nullSubjectResult = new LoginResult(LoginResult.STATUS_FAILED, new CredentialException(errorMsg));
                    LoginResult.setCurrentLoginResult(nullSubjectResult);
                    AuditLoggingUtil.log(nullSubjectResult, request);
                    throw new ServletException("Invalid login result");
                }

                // Save the originally requested URL
                OICServiceRequest oicServiceRequest = (OICServiceRequest) request.getSession().getAttribute("ssoAuthenticationServiceRequest");
                String requestedUrl = "";
                String openIdToken = "";
                String authenticationServiceName = "";

                if (oicServiceRequest != null) {
                    requestedUrl = oicServiceRequest.getRequestedUrl();
                    openIdToken = oicServiceRequest.getOpenIdToken();
                    authenticationServiceName = oicServiceRequest.getAuthenticationServiceName();
                }

                if (request.getSession(false) != null) {
                    request.getSession().invalidate();
                }

                // SSO authenticaion request add originally requested URL
                if (StringUtils.isNotBlank(requestedUrl)) {
                    request.getSession().setAttribute("ssoAuthenticationRedirectUrl", requestedUrl);
                }

                if (StringUtils.isNotBlank(openIdToken)) {
                    request.getSession().setAttribute("ssoAuthenticationOpenIdToken", openIdToken);
                }

                if (StringUtils.isNotBlank(authenticationServiceName)) {
                    request.getSession().setAttribute(SSO_AUTHENTICATION_SERVICE_NAME, authenticationServiceName);
                }

                if (MgnlContext.hasAttribute(ACCESS_TOKEN)) {
                    final String accessToken = MgnlContext.getAttribute(ACCESS_TOKEN);
                    log.debug("new access token found, setting session attribute");
                    request.getSession().setAttribute(ACCESS_TOKEN, accessToken);
                }

                if (MgnlContext.hasAttribute(ID_TOKEN)) {
                    final String idToken = MgnlContext.getAttribute(ID_TOKEN);
                    log.debug("new id token found, setting session attribute");
                    request.getSession().setAttribute(ID_TOKEN, idToken);
                }

                MgnlContext.login(subject);

                AuditLoggingUtil.log(loginResult, request);

                if (oicServiceRequest != null) {
                    reportSSOLogin(request);
                }

                if (StringUtils.isNotBlank(requestedUrl)) {
                    log.debug("Redirecting to original request URL {}", requestedUrl);
                    response.sendRedirect(requestedUrl);
                    return;
                }

                // Send response via redirect to follow PRG (Post/Redirect/Get) pattern to prevent logging in via Back button after logout. http://en.wikipedia.org/wiki/Post/Redirect/Get
                // Applied only in case authentication is done with POST request.
                if (loginResult.getStatus() == LoginResult.STATUS_SUCCEEDED_REDIRECT_REQUIRED) {
                    String location = request.getParameter(FormLogin.PARAMETER_RETURN_TO);
                    // Fallback to current request uri if no FormLogin.PARAMETER_RETURN_TO was specified
                    if (location == null) {
                        location = request.getRequestURL().toString();
                    }
                    location = RequestDispatchUtil.REDIRECT_PREFIX + location;
                    RequestDispatchUtil.dispatch(location, request, response);
                    return;
                }
                break;
            } else {
                // just log.
                AuditLoggingUtil.log(loginResult, request);
            }
        }

        // continue even if all login handlers failed
        chain.doFilter(request, response);
    }

    private static final Logger SSO_LOGINS_LOGGER = LoggerFactory.getLogger("com.tbscg.magnolia-sso-logins");

    private void reportSSOLogin(HttpServletRequest request) {
        User user = MgnlContext.getUser();
        if (user == null) {
            log.warn("tried to report SSO login, but user is null");
            return;
        }
        Temporal now = OffsetDateTime.now();
        SSO_LOGINS_LOGGER.info("",
                DateTimeFormatter.ISO_LOCAL_DATE.format(now),
                DateTimeFormatter.ISO_OFFSET_TIME.format(now),
                request.getServerName(),
                user.getProperty("email"),
                user.getName());
    }

    public ErrorHandler getErrorHandler() {
        return errorHandler;
    }

    public void setErrorHandler(ErrorHandler errorHandler) {
        this.errorHandler = errorHandler;
    }
}
