package com.tbscg.doosan.magnolia.regions.replication;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;

/**
 * Replicator configuration model class.
 *
 * @author apuigescobar
 */
public class ReplicatorConfiguration {

    private Class<? extends RegionReplicator> replicatorClass;
    private String workspace;
    private String site;

    public Class<? extends RegionReplicator> getReplicatorClass() {
        return replicatorClass;
    }

    public void setReplicatorClass(Class<? extends RegionReplicator> replicatorClass) {
        this.replicatorClass = replicatorClass;
    }

    public String getWorkspace() {
        return workspace;
    }

    public void setWorkspace(String workspace) {
        this.workspace = workspace;
    }

    public String getSite() { return site; }

    public void setSite(String site) {this.site = site; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;

        if (o == null || getClass() != o.getClass()) return false;

        ReplicatorConfiguration that = (ReplicatorConfiguration) o;

        return new EqualsBuilder()
                .append(replicatorClass, that.replicatorClass)
                .append(workspace, that.workspace)
                .append(site, that.site)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37)
                .append(replicatorClass)
                .append(workspace)
                .append(site)
                .toHashCode();
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .append("Replicator class: ", replicatorClass)
                .append(System.lineSeparator())
                .append("Workspace: ", workspace)
                .append("Site: ", site)
                .toString();
    }
}
