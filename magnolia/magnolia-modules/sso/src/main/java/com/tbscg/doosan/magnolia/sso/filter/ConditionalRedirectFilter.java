package com.tbscg.doosan.magnolia.sso.filter;

import info.magnolia.cms.filters.AbstractMgnlFilter;
import info.magnolia.context.WebContext;
import info.magnolia.voting.Voter;
import java.io.IOException;
import javax.inject.Inject;
import javax.inject.Provider;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;

@Slf4j
public class ConditionalRedirectFilter extends AbstractMgnlFilter {

	private String redirectUrl;

	private boolean appendParams;

	private Voter<HttpServletRequest> voter;

	private Provider<WebContext> webContextProvider;

	@Inject
	public ConditionalRedirectFilter(Provider<WebContext> webContextProvider) {
		this.webContextProvider = webContextProvider;
	}

	@Override
	public void doFilter(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
			throws IOException, ServletException {
		if (voter != null && voter.vote(request) > 0 && StringUtils.isNotEmpty(redirectUrl)) {
			log.debug("Redirecting to {}", redirectUrl);
			if (appendParams) {
				if(isRedirectUrlCurrentUrl(request)) {
					chain.doFilter(request, response);
				} else {
					response.sendRedirect(buildRedirectUrl(request));
				}
			}
		} else {
			chain.doFilter(request, response);
		}
	}

	private String buildRedirectUrl(HttpServletRequest request) {
		return redirectUrl + (redirectUrl.contains("?") ? "&" : "?") + request.getQueryString();
	}

	private boolean isRedirectUrlCurrentUrl(HttpServletRequest request) {
		return request.getRequestURI() != null && request.getRequestURI().equals(redirectUrl.split("\\?")[0]);
	}

	public void setRedirectUrl(String redirectUrl) {
		this.redirectUrl = redirectUrl;
	}

	public String getRedirectUrl() {
		return redirectUrl;
	}

	public boolean isAppendParams() {
		return appendParams;
	}

	public void setAppendParams(boolean appendParams) {
		this.appendParams = appendParams;
	}

	public Voter<HttpServletRequest> getVoter() {
		return voter;
	}

	public void setVoter(Voter<HttpServletRequest> voter) {
		this.voter = voter;
	}
}
