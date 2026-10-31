package com.tbscg.doosan.magnolia.regions.model;

import info.magnolia.module.site.Domain;
import info.magnolia.module.site.templates.TemplateSettings;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;

import java.util.ArrayList;
import java.util.List;

/**
 * Region site model.
 *
 * @author apuigescobar
 */
public class RegionSite {

    private String name;
    private String title;
    private TemplateSettings templates;
    private List<Domain> domains;
    private List<Region> regions;

    public RegionSite() {
        this.domains = new ArrayList<>();
        this.regions = new ArrayList<>();
    }

    public RegionSite(String name, String title, TemplateSettings templates, List<Domain> domains, List<Region> regions) {
        this.name = name;
        this.title = title;
        this.templates = templates;
        this.domains = domains;
        this.regions = regions;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public TemplateSettings getTemplates() {
        return templates;
    }

    public void setTemplates(TemplateSettings templates) {
        this.templates = templates;
    }

    public List<Domain> getDomains() {
        return domains;
    }

    public void setDomains(List<Domain> domains) {
        this.domains = domains;
    }

    public List<Region> getRegions() {
        return regions;
    }

    public void setRegions(List<Region> regions) {
        this.regions = regions;
    }

    public boolean hasRegions() {
        return !this.regions.isEmpty();
    }

    public boolean hasDomains() {
        return !this.domains.isEmpty();
    }

    @Override
    public int hashCode() {
        HashCodeBuilder hashCodeBuilder = new HashCodeBuilder();

        hashCodeBuilder.append(this.name);

        return hashCodeBuilder.toHashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof RegionSite) {
            return new EqualsBuilder().append(this.name, ((RegionSite) obj).getName()).isEquals();
        }
        return false;
    }

    @Override
    public String toString() {
        ToStringBuilder toStrBuilder = new ToStringBuilder(this);

        toStrBuilder.append("Site name: ");
        toStrBuilder.append(this.name);
        toStrBuilder.append(System.lineSeparator());

        toStrBuilder.append("Site title: ");
        toStrBuilder.append(this.title);
        toStrBuilder.append(System.lineSeparator());

        toStrBuilder.append("Site domains: ");

        for (int i = 0; i < this.domains.size(); i++) {

            if (i == this.domains.size() - 1) {
                toStrBuilder.append(this.domains.get(i));
                toStrBuilder.append(System.lineSeparator());
            }

            else {
                toStrBuilder.append(this.domains.get(i));
                toStrBuilder.append(", ");
            }
        }

        toStrBuilder.append("Site regions: ");

        for (int i = 0; i < this.regions.size(); i++) {

            if (i == this.regions.size() - 1) {
                toStrBuilder.append(this.regions.get(i).toString());
                toStrBuilder.append(System.lineSeparator());
            }

            else {
                toStrBuilder.append(this.regions.get(i).toString());
                toStrBuilder.append(", ");
            }
        }

        return toStrBuilder.toString();
    }
}
