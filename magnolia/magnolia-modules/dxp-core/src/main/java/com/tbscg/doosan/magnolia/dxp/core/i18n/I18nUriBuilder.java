package com.tbscg.doosan.magnolia.dxp.core.i18n;

import java.util.Locale;

public interface I18nUriBuilder {

    /**
     * Builds i18n URI for author instance.
     *
     * @param uriTokens uri tokens.
     * @param locale matching locale.
     * @return author i18n URI.
     */
    String buildAuthorI18nUri(String[] uriTokens, Locale locale);

    /**
     * Builds i18n URI for public instance.
     *
     * @param uriTokens uri tokens.
     * @param locale matching locale.
     * @return public i18n URI.
     */
    String buildPublicI18nUri(String[] uriTokens, Locale locale);
}


