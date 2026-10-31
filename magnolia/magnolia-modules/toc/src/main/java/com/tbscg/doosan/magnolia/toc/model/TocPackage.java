package com.tbscg.doosan.magnolia.toc.model;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;

import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * ToC package model.
 *
 * @author apuigescobar
 */
public class TocPackage {

    private String id;
    private Map<String, String> title;
    private String brand;
    private String region;
    private String channel;
    private String application;
    private List<TocDocument> documents;
    private String version;
    private Date lastModifiedDate;
    private String lastModifiedBy;
    private Date acceptanceDate;
    private String publishedBy;
    private String reviewedBy;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Map<String, String> getTitle() {
        return title;
    }

    public void setTitle(Map<String, String> title) {
        this.title = title;
    }
    public String getBrand() {
        return brand;
    }

    public void setBrand(String brand) {
        this.brand = brand;
    }
    public String getRegion() {
        return region;
    }

    public void setRegion(String region) {
        this.region = region;
    }

    public String getChannel() {
        return channel;
    }

    public void setChannel(String channel) {
        this.channel = channel;
    }

    public String getApplication() {
        return application;
    }

    public void setApplication(String application) {
        this.application = application;
    }

    public List<TocDocument> getDocuments() {
        return documents;
    }

    public void setDocuments(List<TocDocument> documents) {
        this.documents = documents;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public Date getLastModifiedDate() {
        return lastModifiedDate;
    }

    public void setLastModifiedDate(Date lastModifiedDate) {
        this.lastModifiedDate = lastModifiedDate;
    }

    public String getLastModifiedBy() {
        return lastModifiedBy;
    }

    public void setLastModifiedBy(String lastModifiedBy) {
        this.lastModifiedBy = lastModifiedBy;
    }

    public Date getAcceptanceDate() {
        return acceptanceDate;
    }

    public void setAcceptanceDate(Date acceptanceDate) {
        this.acceptanceDate = acceptanceDate;
    }

    public String getPublishedBy() {
        return publishedBy;
    }

    public void setPublishedBy(String publishedBy) {
        this.publishedBy = publishedBy;
    }

    public String getReviewedBy() {
        return reviewedBy;
    }

    public void setReviewedBy(String reviewedBy) {
        this.reviewedBy = reviewedBy;
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder()
                .append(this.title)
                .append(this.brand)
                .append(this.region)
                .append(this.channel)
                .append(this.application)
                .toHashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof TocPackage) {
            TocPackage toEqual = (TocPackage) obj;
            return new EqualsBuilder()
                    .append(this.title, toEqual.getTitle())
                    .append(this.brand, toEqual.getBrand())
                    .append(this.region, toEqual.getRegion())
                    .append(this.channel, toEqual.getChannel())
                    .append(this.application, toEqual.getApplication())
                    .isEquals();
        }
        return false;
    }

    @Override
    public String toString() {
        ToStringBuilder toStrBuilder = new ToStringBuilder(this);

        toStrBuilder.append("ToC package node ID: ")
                .append(this.id)
                .append(System.lineSeparator());

        toStrBuilder.append("ToC package title: ")
                .append(this.title)
                .append(System.lineSeparator());

        toStrBuilder.append("ToC package brand: ")
                .append(this.brand)
                .append(System.lineSeparator());

        toStrBuilder.append("ToC package region: ")
                .append(this.region)
                .append(System.lineSeparator());

        toStrBuilder.append("ToC package channel: ")
                .append(this.channel)
                .append(System.lineSeparator());

        if (StringUtils.isNotEmpty(this.application)) {
            toStrBuilder.append("ToC package application: ")
                    .append(this.application)
                    .append(System.lineSeparator());
        }

        toStrBuilder.append("ToC package documents: ")
                .append(this.documents)
                .append(System.lineSeparator());

        toStrBuilder.append("ToC package content version: ")
                .append(this.version)
                .append(System.lineSeparator());

        toStrBuilder.append("ToC package content version: ")
                .append(this.version)
                .append(System.lineSeparator());

        toStrBuilder.append("ToC package modified at: ")
                .append(this.lastModifiedDate.toString())
                .append(System.lineSeparator());

        toStrBuilder.append("ToC package modified by: ")
                .append(this.lastModifiedBy)
                .append(System.lineSeparator());

        toStrBuilder.append("ToC package published by: ")
                .append(this.acceptanceDate.toString())
                .append(System.lineSeparator());

        toStrBuilder.append("ToC package accepted at: ")
                .append(this.acceptanceDate.toString())
                .append(System.lineSeparator());

        toStrBuilder.append("ToC package reviewed by: ")
                .append(this.reviewedBy)
                .append(System.lineSeparator());

        return toStrBuilder.toString();
    }
}