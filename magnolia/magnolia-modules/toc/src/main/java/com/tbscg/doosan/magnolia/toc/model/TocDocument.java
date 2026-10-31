package com.tbscg.doosan.magnolia.toc.model;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;

import java.util.Date;
import java.util.Map;

/**
 * ToC document model.
 *
 * @author apuigescobar
 */
public class TocDocument {

    private String id;
    private Map<String, String> title;
    private Map<String, String> content;
    private String version;
    private String lastModifiedBy;
    private Date lastModifiedDate;
    private Date publicationDate;

    public TocDocument(String id, Map<String, String> title, Map<String, String> content, String version, String lastModifiedBy,
                       Date lastModifiedDate, Date publicationDate) {
        this.id = id;
        this.title = title;
        this.content = content;
        this.version = version;
        this.lastModifiedBy = lastModifiedBy;
        this.lastModifiedDate = lastModifiedDate;
        this.publicationDate = publicationDate;
    }

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

    public Map<String, String> getContent() {
        return content;
    }

    public void setContent(Map<String, String> content) {
        this.content = content;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getLastModifiedBy() {
        return lastModifiedBy;
    }

    public void setLastModifiedBy(String lastModifiedBy) {
        this.lastModifiedBy = lastModifiedBy;
    }

    public Date getLastModifiedDate() {
        return lastModifiedDate;
    }

    public void setLastModifiedDate(Date lastModifiedDate) {
        this.lastModifiedDate = lastModifiedDate;
    }

    public Date getPublicationDate() {
        return publicationDate;
    }

    public void setPublicationDate(Date publicationDate) {
        this.publicationDate = publicationDate;
    }

    @Override
    public int hashCode() {
        HashCodeBuilder hashCodeBuilder = new HashCodeBuilder();

        hashCodeBuilder.append(this.title).append(this.content).append(this.version);

        return hashCodeBuilder.toHashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof TocDocument) {
            TocDocument toEqual = (TocDocument) obj;
            return new EqualsBuilder()
                    .append(this.title, toEqual.getTitle())
                    .append(this.content, toEqual.getContent())
                    .append(this.version, toEqual.getVersion())
                    .isEquals();
        }
        return false;
    }

    @Override
    public String toString() {
        ToStringBuilder toStrBuilder = new ToStringBuilder(this);

        toStrBuilder.append("ToC document node ID: ")
                .append(this.id)
                .append(System.lineSeparator());

        toStrBuilder.append("ToC document title: ")
                .append(this.title)
                .append(System.lineSeparator());

        toStrBuilder.append("ToC document content: ")
                .append(this.content)
                .append(System.lineSeparator());

        toStrBuilder.append("ToC document vanity version: ")
                .append(this.version)
                .append(System.lineSeparator());

        toStrBuilder.append("ToC document last modified by:")
                .append(this.lastModifiedBy)
                .append(System.lineSeparator());

        toStrBuilder.append("ToC document modified at:")
                .append(this.lastModifiedDate.toString())
                .append(System.lineSeparator());

        toStrBuilder.append("ToC document published at: ")
                .append(this.publicationDate.toString())
                .append(System.lineSeparator());

        return toStrBuilder.toString();
    }
}