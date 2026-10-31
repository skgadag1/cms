package com.tbscg.doosan.magnolia.regions.i18n;

import com.tbscg.doosan.magnolia.dxp.core.i18n.I18nUriBuilder;
import org.apache.jackrabbit.ocm.repository.NodeUtil;

import java.util.Locale;

public class RegionsI18nUriBuilder implements I18nUriBuilder {

    @Override
    public String buildAuthorI18nUri(String[] uriTokens, Locale locale) {

        StringBuilder i18nUriBuilder = new StringBuilder();

        for (int i = 0; i < uriTokens.length; i++) {

            if (i != 1) {

                this.appendUriToken(i18nUriBuilder, uriTokens[i]);

            } else {

                this.appendUriTokenWithLocale(i18nUriBuilder, uriTokens[i], locale);

            }
        }

        return i18nUriBuilder.toString();
    }


    public String buildPublicI18nUri(String[] uriTokens, Locale locale) {

        StringBuilder i18nUriBuilder = new StringBuilder();

        for (int i = 0; i < uriTokens.length; i++) {

            if (i != 0) {

                this.appendUriToken(i18nUriBuilder, uriTokens[i]);

            } else {

                this.appendUriTokenWithLocale(i18nUriBuilder, uriTokens[i], locale);

            }
        }

        return i18nUriBuilder.toString();
    }

    /**
     * Appends URI token path to URI builder.
     *
     * @param i18nUriBuilder i18n URI builder.
     * @param token          URI token.
     */
    private void appendUriToken(StringBuilder i18nUriBuilder, String token) {
        i18nUriBuilder.append(NodeUtil.PATH_SEPARATOR);
        i18nUriBuilder.append(token);
    }

    /**
     * Appends URI token along with matching locale path to URI builder.
     *
     * @param i18nUriBuilder i18n URI builder.
     * @param token          URI token.
     * @param locale         matching locale.
     */
    private void appendUriTokenWithLocale(StringBuilder i18nUriBuilder, String token, Locale locale) {

        i18nUriBuilder.append(NodeUtil.PATH_SEPARATOR);
        i18nUriBuilder.append(token);
        i18nUriBuilder.append(NodeUtil.PATH_SEPARATOR);
        i18nUriBuilder.append(locale.toString());

    }
}
