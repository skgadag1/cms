package com.tbscg.doosan.magnolia.sso.filter;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;

import com.google.common.collect.Maps;

import info.magnolia.cms.filters.AbstractMgnlFilter;
import info.magnolia.connector.sso.util.SSOUtils;
import info.magnolia.context.WebContext;
import info.magnolia.voting.voters.RequestParameterVoter;

@Slf4j
public class SessionInvalidateFilter extends AbstractMgnlFilter {

	private String paramName = "session";
	private String paramValue = "invalidate";

	private Provider<WebContext> webContextProvider;

	@Inject
	public SessionInvalidateFilter(Provider<WebContext> webContextProvider) {
		this.webContextProvider = webContextProvider;
	}

	@Override
	public void doFilter(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
			throws IOException, ServletException {

		String redirectURL = this.getRedirectLink(request);

		if (isParamMatching(request) && StringUtils.isNotEmpty(redirectURL)) {
			log.debug("Session filter triggered with parameter: {}={}", getParamName(), getParamValue());
			if (request.getSession(false) != null) {
				log.debug("Invalidating session {}", request.getSession().getId());
				request.getSession().invalidate();
			}
			log.debug("Redirecting to {}", redirectURL);
			response.sendRedirect(redirectURL);
		} else {
			chain.doFilter(request, response);
		}
	}

	@SuppressWarnings("unchecked")
	private boolean isParamMatching(HttpServletRequest request) {
		RequestParameterVoter voter = new RequestParameterVoter(this.webContextProvider);
		voter.setParameterName(getParamName());
		voter.setParameterValue(getParamValue());
		return voter.vote(request) > 0;
	}

	private String getRedirectLink(HttpServletRequest request) {
		Map<String, String[]> params = new HashMap<>(request.getParameterMap());
		Map<String, String[]> filtered = Maps.filterKeys(params, s -> !s.equals(getParamName()));
		if (params.equals(filtered)) {
			return null;
		} else {
			return SSOUtils.getOriginalRedirectURL(request.getRequestURI(), filtered);
		}
	}

	public void setParamName(String paramName) {
		this.paramName = paramName;
	}

	public void setParamValue(String paramValue) {
		this.paramValue = paramValue;
	}

	public String getParamName() {
		return paramName;
	}

	public String getParamValue() {
		return paramValue;
	}
}
