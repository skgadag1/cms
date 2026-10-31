package com.tbscg.doosan.magnolia.sso.filter;

import info.magnolia.audit.AuditLoggingUtil;
import info.magnolia.cms.filters.MgnlFilterChain;
import info.magnolia.cms.security.auth.logout.SSOLogoutFilter;
import info.magnolia.connector.sso.SSOConnector;
import info.magnolia.connector.sso.oic.settings.OICSettings;
import info.magnolia.context.Context;
import info.magnolia.context.MgnlContext;
import info.magnolia.context.UserContext;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import javax.inject.Inject;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.math.BigInteger;
import java.security.SecureRandom;

import static com.tbscg.doosan.magnolia.sso.filter.SSOAuthenticationTokenLoginFilter.SSO_AUTHENTICATION_SERVICE_NAME;

@Slf4j
public class CustomSSOLogoutFilter extends SSOLogoutFilter {
    private final SSOConnector ssoConfig;
    private String openIdToken;

    @Inject
    public CustomSSOLogoutFilter(SSOConnector ssoConfig) {
        super(ssoConfig);
        this.ssoConfig = ssoConfig;
    }

    @Override
    public void doFilter(HttpServletRequest request, HttpServletResponse response, FilterChain chain) throws IOException, ServletException {
        String authenticationServiceName = (String) request.getSession().getAttribute(SSO_AUTHENTICATION_SERVICE_NAME);
        MgnlContext.setAttribute(SSO_AUTHENTICATION_SERVICE_NAME, authenticationServiceName);
        OICSettings oicSettings = this.ssoConfig.getAuthenticationServices().get(authenticationServiceName);
        if (oicSettings != null && oicSettings.isOpenIdLogoutEnabled()) {
             this.openIdToken = (String) request.getSession().getAttribute("ssoAuthenticationOpenIdToken");
             String state = StringUtils.defaultString(request.getParameter("state"), "");
             String stateOrig = (String) request.getSession().getAttribute("ssoLogoutState");
             if (!StringUtils.isEmpty(stateOrig) && !StringUtils.isEmpty(state) && !StringUtils.equals(state, stateOrig)) {
                  log.error("The delivered state does not match the provided one. Logout from the OP failed.");
             }
             request.getSession().removeAttribute("ssoLogoutState");
        }

        if (null != request.getParameter("mgnlLogout")) {
            Context ctx = MgnlContext.getInstance();
            if (ctx instanceof UserContext) {
                AuditLoggingUtil.log((UserContext)ctx);
                ((UserContext)ctx).logout();
            }

            if (request.getSession(false) != null) {
                request.getSession().invalidate();
            }

            if (chain instanceof MgnlFilterChain) {
                ((MgnlFilterChain)chain).reset();
            }

            response.sendRedirect(this.resolveLogoutRedirectLink(request));

        } else {
            chain.doFilter(request, response);
        }
    }

    @Override
    protected String resolveLogoutRedirectLink(HttpServletRequest request) {
        String authenticationServiceName = MgnlContext.getAttribute(SSO_AUTHENTICATION_SERVICE_NAME); 
        OICSettings oicSettings = this.ssoConfig.getAuthenticationServices().get(authenticationServiceName);
        if (oicSettings != null && !StringUtils.isBlank(oicSettings.getEndSessionEndpoint())) {
             if (!oicSettings.isOpenIdLogoutEnabled()) {
                  log.info(oicSettings.getEndSessionEndpoint() + request.getRequestURL().toString());
                  return oicSettings.getEndSessionEndpoint() + request.getRequestURL().toString();
             }
             
             if (StringUtils.isNotBlank(this.openIdToken)) {
                  String state = this.getRandomState();
                  request.getSession().setAttribute("ssoLogoutState", state);
                  return oicSettings.getEndSessionEndpoint() + "?id_token_hint=" + this.openIdToken + "&" + "state" + "=" + state + "&post_logout_redirect_uri=" + oicSettings.getCallbackURL();
             } else {
            	 log.warn("ID token not found");
             }
        } else {
             log.warn("No end session configuration found");
        }
        return request.getRequestURL().toString();
    }

    private String getRandomState() {
        return (new BigInteger(130, new SecureRandom())).toString(32);
    }

}
