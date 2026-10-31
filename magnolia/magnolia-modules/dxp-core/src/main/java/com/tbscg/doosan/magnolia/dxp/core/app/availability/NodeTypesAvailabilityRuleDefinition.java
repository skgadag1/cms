package com.tbscg.doosan.magnolia.dxp.core.app.availability;

import info.magnolia.ui.api.availability.AvailabilityRuleDefinition;
import info.magnolia.ui.api.availability.AvailabilityRuleType;
import info.magnolia.ui.api.availability.ConfiguredAvailabilityRuleDefinition;

import java.util.List;

/**
 * {@link AvailabilityRuleDefinition} that allows to define specific primary node type.
 *
 * @author apuigescobar
 */
@AvailabilityRuleType("nodeTypesRule")
public class NodeTypesAvailabilityRuleDefinition extends ConfiguredAvailabilityRuleDefinition {

    private List<String> nodeTypes;

    public List<String> getNodeTypes() {
        return nodeTypes;
    }

    public void setNodeTypes(List<String> nodeTypes) {
        this.nodeTypes = nodeTypes;
    }
}
