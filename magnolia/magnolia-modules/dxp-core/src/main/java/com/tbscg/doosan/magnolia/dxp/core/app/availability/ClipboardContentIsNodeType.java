package com.tbscg.doosan.magnolia.dxp.core.app.availability;

import com.tbscg.doosan.magnolia.dxp.core.util.NodeUtil;
import info.magnolia.ui.api.availability.AvailabilityDefinition;
import info.magnolia.ui.api.availability.AvailabilityRule;
import info.magnolia.ui.availability.rule.AbstractJcrRule;
import info.magnolia.ui.framework.ContentClipboard;

import javax.inject.Inject;
import javax.jcr.Item;
import javax.jcr.Node;
import java.util.List;
import java.util.Objects;

/**
 * {@link AvailabilityRule} that checks if content clipboard contains items of a specific node type.
 *
 * @author apuigescobar
 */
public class ClipboardContentIsNodeType extends AbstractJcrRule<ClipboardContentRuleDefinition> {

    private final ContentClipboard<Item> contentClipboard;
    private final List<String> nodeTypes;

    @Inject
    public ClipboardContentIsNodeType(AvailabilityDefinition availabilityDefinition, ClipboardContentRuleDefinition ruleDefinition,
                                      ContentClipboard<Item> contentClipboard) {
        super(availabilityDefinition, ruleDefinition);
        this.contentClipboard = contentClipboard;
        this.nodeTypes = ruleDefinition.getNodeTypes();
    }

    @Override
    protected boolean isAvailableForJcrItem(Item item) {
        return this.contentClipboard.getContents().anyMatch(
                clipboardItem -> this.isClipboardContentOfType(clipboardItem, this.nodeTypes));
    }

    /**
     * Checks if clipboard content is from any of the provided node types.
     *
     * @param nodeTypes node types to check.
     * @return whether clipboard content is from at least one of the provided node types or not.
     */
    public boolean isClipboardContentOfType(Item clipboardItem, List<String> nodeTypes) {
        if (clipboardItem instanceof Node) {
            Node clipboardNode = (Node) clipboardItem;
            return nodeTypes.stream().anyMatch(
                    nodeType -> Objects.equals(NodeUtil.getPrimaryNodeType(clipboardNode), nodeType));
        }
        return false;
    }
}
