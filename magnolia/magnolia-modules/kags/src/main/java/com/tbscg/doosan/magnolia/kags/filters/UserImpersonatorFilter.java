package com.tbscg.doosan.magnolia.kags.filters;

import info.magnolia.cms.core.AggregationState;
import info.magnolia.cms.filters.AbstractMgnlFilter;
import info.magnolia.context.Context;
import info.magnolia.context.MgnlContext;
import info.magnolia.context.WebContext;
import lombok.extern.slf4j.Slf4j;

import javax.inject.Inject;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Filter that resolves the site to use by considering variation of the set channel. Resulting site gets set in {@link AggregationState}.
 */
@Slf4j
public class UserImpersonatorFilter extends AbstractMgnlFilter {

    public static final String ENFORCE_KEY_ACCOUNT_PARAMETER = "keyAccount";
    public static final String ENFORCE_EXPERT_LEVEL_PARAMETER = "expertLevel";
    public static final String ENFORCE_IS_INTERNAL_PARAMETER = "isInternal";
    public static final String MGNL_PREVIEW_ATTRIBUTE = "mgnlPreview";

    @Inject
    public UserImpersonatorFilter() {

    }

    @Override
    public void doFilter(HttpServletRequest request, HttpServletResponse response, FilterChain chain) throws IOException, ServletException {
        final boolean isPreview = resolvePreviewMode();
        final AggregationState aggregationState = MgnlContext.getAggregationState();
        aggregationState.setPreviewMode(isPreview);
        if (isPreview) {
            resolveKeyAccount();
            resolveExpertLevel();
            resolveIfIsInternal();
        }
        chain.doFilter(request, response);
    }

    @Override
    public boolean bypasses(HttpServletRequest request) {
        return super.bypasses(request);
    }

    protected void resolveKeyAccount() {
        final String keyAccountParameterValue = MgnlContext.getAttribute(ENFORCE_KEY_ACCOUNT_PARAMETER);
        if (keyAccountParameterValue != null) {
            WebContext webCtx = MgnlContext.getWebContext();
            if( webCtx != null ) {
                webCtx.setAttribute("companyId", keyAccountParameterValue, Context.LOCAL_SCOPE);
                log.debug("Added user with company id property {}", keyAccountParameterValue);
            }
        }
    }

    protected void resolveExpertLevel() {
        final String expertLevelParameterValue = MgnlContext.getAttribute(ENFORCE_EXPERT_LEVEL_PARAMETER);
        if (expertLevelParameterValue != null) {
            WebContext webCtx = MgnlContext.getWebContext();
            if( webCtx != null ) {
                webCtx.setAttribute("expertLevel", expertLevelParameterValue, Context.LOCAL_SCOPE);
                log.debug("Added user with expert level property {}", expertLevelParameterValue);
            }
        }
    }

    protected void resolveIfIsInternal() {
        final String isInternalParameterValue = MgnlContext.getAttribute(ENFORCE_IS_INTERNAL_PARAMETER);
        if (isInternalParameterValue != null) {
            WebContext webCtx = MgnlContext.getWebContext();
            if( webCtx != null ) {
                webCtx.setAttribute("isInternal", isInternalParameterValue, Context.LOCAL_SCOPE);
                log.debug("Added user is internal: {}", isInternalParameterValue);
            }
        }
    }

    protected boolean resolvePreviewMode() {
        if (MgnlContext.getParameter(MGNL_PREVIEW_ATTRIBUTE) != null) {
            return Boolean.parseBoolean(MgnlContext.getParameter(MGNL_PREVIEW_ATTRIBUTE));
        }
        return false;
    }
}
