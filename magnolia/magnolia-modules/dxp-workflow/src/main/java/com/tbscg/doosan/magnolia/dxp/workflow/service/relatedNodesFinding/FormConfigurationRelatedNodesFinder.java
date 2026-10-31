package com.tbscg.doosan.magnolia.dxp.workflow.service.relatedNodesFinding;

import com.tbscg.doosan.magnolia.dxp.workflow.constant.WorkflowConstant;
import info.magnolia.jcr.util.NodeTypes;
import lombok.extern.slf4j.Slf4j;

import javax.jcr.Node;
import java.util.List;

@Slf4j
public class FormConfigurationRelatedNodesFinder extends RelatedNodesFinder {
    @Override
    protected List<Node> findRelatedNodesForSeeds(final List<String> nodeUuids,
                                                  final String path) {
        List<Node> nodeList = getNodeList(WorkflowConstant.WEBSITE_WORKSPACE, nodeUuids, NodeTypes.Page.NAME, WorkflowConstant.FORM_CONFIGURATION_PROPERTY);
        log.info("FormConfigurationRelatedNodesFinder summary: path={}, ids={}, queries={}, matchedNodes={}",
                path, nodeUuids.size(), getQueryBatchCount(nodeUuids), nodeList.size());
        return getParentPageNodeList(nodeList);
    }
}
