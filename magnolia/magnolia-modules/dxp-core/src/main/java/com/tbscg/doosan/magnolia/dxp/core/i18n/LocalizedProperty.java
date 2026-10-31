package com.tbscg.doosan.magnolia.dxp.core.i18n;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;

/**
 * LocalizedProperty is a class which contains locale code and corresponding value. Can be used whenever supporting of multiple locales is required.
 */
public final class LocalizedProperty {
    private final String locale;
    private final String value;

    public LocalizedProperty(String locale, String value) {
        this.locale = locale;
        this.value = value;
    }

    public String getLocale() {
        return locale;
    }

    public String getValue() {
        return value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;

        if (o == null || getClass() != o.getClass()) return false;

        LocalizedProperty that = (LocalizedProperty) o;

        return new EqualsBuilder()
                .append(locale, that.locale)
                .append(value, that.value)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37)
                .append(locale)
                .append(value)
                .toHashCode();
    }
}
