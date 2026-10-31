package com.tbscg.doosan.magnolia.regions.model;

import com.tbscg.doosan.magnolia.regions.i18n.RegionsI18nContentSupport;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;

import java.util.List;
import java.util.Locale;

/**
 * Region model.
 *
 * @author Alejandro Puig Escobar
 */
public class Region {

    private String title;
    private String name;
    private String countryCode;
    private List<String> publishers;
    private RegionsI18nContentSupport i18n;
    private String xDefault;

    public Region() {
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<String> getPublishers() {
        return publishers;
    }

    public void setPublishers(List<String> publishers) {
        this.publishers = publishers;
    }

    public String getCountryCode() {
        return countryCode;
    }

    public void setCountryCode(String countryCode) {
        this.countryCode = countryCode;
    }

    public RegionsI18nContentSupport getI18n() {
        return i18n;
    }

    public void setI18n(RegionsI18nContentSupport i18n) {
        this.i18n = i18n;
    }

    public String getXDefault() {
        return xDefault;
    }

    public void setXDefault(String xDefault) {
        this.xDefault = xDefault;
    }

    @Override
    public int hashCode() {
        HashCodeBuilder hashCodeBuilder = new HashCodeBuilder();

        hashCodeBuilder.append(this.name).append(this.title);

        return hashCodeBuilder.toHashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof Region) {
            return new EqualsBuilder().append(this.name, ((Region) obj).getName()).isEquals();
        }
        return false;
    }

    @Override
    public String toString() {
        ToStringBuilder toStrBuilder = new ToStringBuilder(this);

        toStrBuilder.append("Region name: ");
        toStrBuilder.append(this.name);
        toStrBuilder.append(System.lineSeparator());

        toStrBuilder.append("Region title: ");
        toStrBuilder.append(this.title);
        toStrBuilder.append(System.lineSeparator());

        toStrBuilder.append("Region languages: ");

        Locale[] locales = this.i18n.getLocales().toArray(new Locale[]{});

        for (int i = 0; i < locales.length; i++) {

            if (i < locales.length - 1) {
                toStrBuilder.append(locales[i].toString());
                toStrBuilder.append(System.lineSeparator());
            } else {
                toStrBuilder.append(locales[i]);
                toStrBuilder.append(", ");
            }
        }

        return toStrBuilder.toString();

    }
}
