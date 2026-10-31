package com.tbscg.doosan.magnolia.dxp.core.setup.task;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;

/**
 * Wrapper class for {@link PropertyMigrationTask} data attributes.
 */
public class PropertyMigrationData {

    private final String workspace;
    private final String nodeType;
    private final String oldPropertyName;
    private final String newPropertyName;

    public PropertyMigrationData(String workspace, String nodeType, String oldPropertyName, String newPropertyName) {
        this.workspace = workspace;
        this.nodeType = nodeType;
        this.oldPropertyName = oldPropertyName;
        this.newPropertyName = newPropertyName;
    }

    protected String getWorkspace() {
        return this.workspace;
    }

    protected String getOldPropertyName() {
        return this.oldPropertyName;
    }

    protected String getNewPropertyName() {
        return this.newPropertyName;
    }

    protected String getNodeType() {
        return this.nodeType;
    }

    @Override
    public int hashCode() {
        HashCodeBuilder hashCodeBuilder = new HashCodeBuilder();

        hashCodeBuilder.append(this.oldPropertyName)
                       .append(this.newPropertyName)
                       .append(this.workspace)
                       .append(this.nodeType);

        return hashCodeBuilder.toHashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof PropertyMigrationData) {
            EqualsBuilder equalsBuilder = new EqualsBuilder();
            PropertyMigrationData dataObj = (PropertyMigrationData) obj;

            return equalsBuilder.append(this.oldPropertyName, dataObj.getOldPropertyName())
                    .append(this.newPropertyName, dataObj.getNewPropertyName())
                    .append(this.nodeType, dataObj.getNodeType())
                    .append(this.workspace, dataObj.getWorkspace())
                    .isEquals();
        }
        return false;
    }

    @Override
    public String toString() {
        ToStringBuilder toStrBuilder = new ToStringBuilder(this);

        toStrBuilder.append("Old property name: ");
        toStrBuilder.append(this.oldPropertyName);
        toStrBuilder.append(System.lineSeparator());

        toStrBuilder.append("New property name: ");
        toStrBuilder.append(this.newPropertyName);
        toStrBuilder.append(System.lineSeparator());

        toStrBuilder.append("Node type: ");
        toStrBuilder.append(this.nodeType);
        toStrBuilder.append(System.lineSeparator());

        toStrBuilder.append("Workspace: ");
        toStrBuilder.append(this.workspace);
        toStrBuilder.append(System.lineSeparator());

        return toStrBuilder.toString();
    }
}
