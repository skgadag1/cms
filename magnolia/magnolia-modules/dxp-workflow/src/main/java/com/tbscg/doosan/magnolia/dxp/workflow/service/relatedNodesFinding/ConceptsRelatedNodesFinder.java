package com.tbscg.doosan.magnolia.dxp.workflow.service.relatedNodesFinding;

import com.tbscg.doosan.magnolia.dxp.workflow.constant.WorkflowConstant;
import info.magnolia.jcr.util.NodeTypes;
import info.magnolia.jcr.util.NodeUtil;

import javax.jcr.Node;
import java.util.Collections;
import java.util.List;

public class ConceptsRelatedNodesFinder extends RelatedNodesFinder {

    private static final String DISCLAIMERS_FOLDER = "disclaimers";
    private static final String DISCLAIMER_PROPERTY = "disclaimerConcept";

    @Override
    protected List<Node> findRelatedNodesForSeeds(final List<String> nodeUuids,
                                                  final String path) {
        final List<String> disclaimerConceptIds = nodeUuids.stream()
                .filter(this::isDisclaimerConcept)
                .toList();
        if (disclaimerConceptIds.isEmpty()) {
            return Collections.emptyList();
        }
        List<Node> nodeList = getNodeList(WorkflowConstant.WEBSITE_WORKSPACE, disclaimerConceptIds, NodeTypes.Page.NAME, DISCLAIMER_PROPERTY);
        return getParentPageNodeList(nodeList);
    }

    private boolean isDisclaimerConcept(String nodeId) {
        try {
            Node node = NodeUtil.getNodeByIdentifier(WorkflowConstant.CONCEPTS_WORKSPACE, nodeId);
            String path = node.getPath();
            return path.contains(DISCLAIMERS_FOLDER);
        } catch (Exception e) {
            return false;
        }
    }
}
