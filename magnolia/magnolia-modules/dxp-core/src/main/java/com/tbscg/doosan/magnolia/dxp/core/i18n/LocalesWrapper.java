package com.tbscg.doosan.magnolia.dxp.core.i18n;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;

import java.util.List;
import java.util.Locale;

/**
 * Locales is a wrapper holding references to supported locales for given site. It acts as a    data structure.
 */
public final class LocalesWrapper {
    private final List<Locale> locales;
    private final Locale defaultLocale;

    public LocalesWrapper(List<Locale> locales, Locale defaultLocale) {
        this.locales = locales;
        this.defaultLocale = defaultLocale;
    }

    public List<Locale> getLocales() {
        return locales;
    }

    public Locale getDefaultLocale() {
        return defaultLocale;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;

        if (o == null || getClass() != o.getClass()) return false;

        LocalesWrapper localesWrapper = (LocalesWrapper) o;

        return new EqualsBuilder()
                .append(locales, localesWrapper.locales)
                .append(defaultLocale, localesWrapper.defaultLocale)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37)
                .append(locales)
                .append(defaultLocale)
                .toHashCode();
    }
}
