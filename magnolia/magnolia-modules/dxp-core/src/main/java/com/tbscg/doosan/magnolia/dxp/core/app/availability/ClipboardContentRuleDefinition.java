package com.tbscg.doosan.magnolia.dxp.core.app.availability;


import info.magnolia.ui.api.availability.AvailabilityRuleType;

/**
 * {@link info.magnolia.ui.api.availability.AvailabilityRuleDefinition} that supports primary node types definitions for
 * clipboard content items.
 *
 * @author apuigescobar
 */
@AvailabilityRuleType("clipboardContentRule")
public class ClipboardContentRuleDefinition extends NodeTypesAvailabilityRuleDefinition {
    public ClipboardContentRuleDefinition() {
        setImplementationClass(ClipboardContentIsNodeType.class);
    }
}
