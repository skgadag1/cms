package com.tbscg.doosan.magnolia.regions.i18n;

import com.tbscg.doosan.magnolia.dxp.core.i18n.I18nUriBuilder;
import com.tbscg.doosan.magnolia.dxp.core.util.DxpUtil;
import info.magnolia.cms.i18n.DefaultI18nContentSupport;
import info.magnolia.objectfactory.Components;
import org.apache.commons.lang3.RegExUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import static org.apache.jackrabbit.ocm.repository.NodeUtil.PATH_SEPARATOR;

/**
 * {@link info.magnolia.cms.i18n.I18nContentSupport} class for sites defined on regions workspace.
 *
 * @author Alejandro Puig Escobar
 */
public class RegionsI18nContentSupport extends DefaultI18nContentSupport {

    // Map locale to its hreflang codes
    private Map<Locale, List<String>> hreflangLocalesByLocale = new HashMap<>();

    @Override
    public String toI18NURI(String uri, Locale locale) {

        String[] uriTokens = StringUtils.split(uri, PATH_SEPARATOR);

        I18nUriBuilder i18nUriBuilder = Components.getComponent(I18nUriBuilder.class);

        if (uriTokens.length > 1) {

            if (DxpUtil.isAuthor()) {

                return i18nUriBuilder.buildAuthorI18nUri(uriTokens, locale);

            } else {

                return i18nUriBuilder.buildPublicI18nUri(uriTokens, locale);
            }

        } else {

            return uri + locale.toString();
        }
    }

    @Override
    protected String toRawURI(String i18nURI, Locale locale) {
        String raw = i18nURI;
        String localeSuffix = PATH_SEPARATOR + locale.toString();
        if (StringUtils.contains(raw, localeSuffix)) {
            raw = RegExUtils.removeFirst(raw, localeSuffix);
        }
        return raw;
    }

    public Optional<Locale> getRegionDefaultLocale(String region) {
        if (region != null) {
            Collection<Locale> locales = this.getLocales();
            return locales.stream().findFirst();
        }
        return Optional.empty();
    }

    /**
     * Gets hreflang locale codes for a specific locale.
     * @param locale the locale to get hreflang codes for
     * @return list of hreflang codes (e.g., ["en-us", "en-ca"]), empty if none
     */
    public List<String> getHreflangLocales(Locale locale) {
        return hreflangLocalesByLocale.getOrDefault(locale, Collections.emptyList());
    }

    /**
     * Sets hreflang locale codes for a specific locale.
     * Used by converter during model construction.
     * @param locale the locale to set hreflang codes for
     * @param hreflangCodes list of hreflang codes to associate with this locale
     */
    public void setHreflangLocales(Locale locale, List<String> hreflangCodes) {
        if (hreflangCodes != null && !hreflangCodes.isEmpty()) {
            this.hreflangLocalesByLocale.put(locale, new ArrayList<>(hreflangCodes));
        }
    }

    /**
     * Gets all hreflang locale codes across all locales.
     * @return all hreflang codes defined in this region
     */
    public List<String> getAllHreflangLocales() {
        return hreflangLocalesByLocale.values().stream()
                .flatMap(List::stream)
                .collect(Collectors.toList());
    }

    /**
     * Checks if a hreflang locale code exists in any locale.
     * @param hreflangCode the code to check
     * @return the locale that contains this code, or empty if not found
     */
    public Optional<Locale> findLocaleByHreflangCode(String hreflangCode) {
        return hreflangLocalesByLocale.entrySet().stream()
                .filter(entry -> entry.getValue().contains(hreflangCode))
                .map(Map.Entry::getKey)
                .findFirst();
    }
}
