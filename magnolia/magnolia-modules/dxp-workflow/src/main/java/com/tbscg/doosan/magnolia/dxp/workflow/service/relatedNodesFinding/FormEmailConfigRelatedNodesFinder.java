package com.tbscg.doosan.magnolia.dxp.workflow.service.relatedNodesFinding;

import com.tbscg.doosan.magnolia.dxp.workflow.constant.WorkflowConstant;
import info.magnolia.jcr.util.NodeTypes;
import lombok.extern.slf4j.Slf4j;

import javax.jcr.Node;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Slf4j
public class FormEmailConfigRelatedNodesFinder extends RelatedNodesFinder {
    @Override
    protected List<Node> findRelatedNodesForSeeds(final List<String> nodeUuids,
                                                  final String path) {
        List<Node> relatedNodes = new ArrayList<>();
        List<Node> relatedFormConfigurations = getNodeList(WorkflowConstant.FORMS_CONFIGURATION_WORKSPACE, nodeUuids, WorkflowConstant.FORM_CONFIGURATION_TYPE, WorkflowConstant.FORM_EMAIL_CONFIG_PROPERTY);
        List<String> relatedConfigurationIds = relatedFormConfigurations.stream()
                .map(info.magnolia.jcr.util.NodeUtil::getNodeIdentifierIfPossible)
                .filter(org.apache.commons.lang3.StringUtils::isNotBlank)
                .toList();
        List<Node> relatedPageNodes = relatedConfigurationIds.isEmpty()
                ? Collections.emptyList()
                : getNodeList(WorkflowConstant.WEBSITE_WORKSPACE, relatedConfigurationIds, NodeTypes.Page.NAME, WorkflowConstant.FORM_CONFIGURATION_PROPERTY);
        relatedNodes.addAll(getParentPageNodeList(relatedPageNodes));
        relatedNodes.addAll(relatedFormConfigurations);
        final int queryCount = getQueryBatchCount(nodeUuids) + getQueryBatchCount(relatedConfigurationIds);
        log.info("FormEmailConfigRelatedNodesFinder summary: path={}, ids={}, queries={}, matchedConfigs={}, matchedPages={}, matchedNodes={}",
                path,
                nodeUuids.size(),
                queryCount,
                relatedFormConfigurations.size(),
                relatedPageNodes.size(),
                relatedNodes.size());
        return relatedNodes;
    }
}
