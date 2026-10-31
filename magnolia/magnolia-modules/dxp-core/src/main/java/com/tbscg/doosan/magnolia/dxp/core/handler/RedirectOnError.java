package com.tbscg.doosan.magnolia.dxp.core.handler;

import info.magnolia.voting.Voter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class RedirectOnError implements ErrorHandler {

    private String redirectUrl;
    private Voter<HttpServletRequest> voter;

    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response, Exception exception) {
        if (voter == null || voter.vote(request) > 0) {
            if (redirectUrl == null) {
                throw new RuntimeException(exception);
            }

            try {
                if (!isRedirectUrlCurrentUrl(request)) {
                    response.sendRedirect(buildRedirectUrl(exception));
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    public String buildRedirectUrl(Exception error) {
        return redirectUrl + "?error=" + error.getClass().getSimpleName() + "&error_description=" + error.getMessage();
    }

    public boolean isRedirectUrlCurrentUrl(HttpServletRequest request) {
        return request.getRequestURI() != null && request.getRequestURI().equals(redirectUrl.split("\\?")[0]);
    }

    public String getRedirectUrl() {
        return redirectUrl;
    }

    public void setRedirectUrl(String redirectUrl) {
        this.redirectUrl = redirectUrl;
    }

    public Voter<HttpServletRequest> getVoter() {
        return voter;
    }

    public void setVoter(Voter<HttpServletRequest> voter) {
        this.voter = voter;
    }
}
