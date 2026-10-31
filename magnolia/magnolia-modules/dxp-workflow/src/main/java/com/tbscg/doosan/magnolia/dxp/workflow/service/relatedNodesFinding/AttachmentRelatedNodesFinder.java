package com.tbscg.doosan.magnolia.dxp.workflow.service.relatedNodesFinding;

import com.tbscg.doosan.magnolia.dxp.workflow.constant.WorkflowConstant;
import com.tbscg.doosan.magnolia.product.catalog.util.ProductCatalogConstants.PageProperties;
import info.magnolia.jcr.util.NodeTypes;
import lombok.extern.slf4j.Slf4j;

import javax.jcr.Node;
import java.util.List;

@Slf4j
public class AttachmentRelatedNodesFinder extends RelatedNodesFinder {


    @Override
    protected List<Node> findRelatedNodesForSeeds(final List<String> nodeUuids,
                                                  final String path) {
        final List<Node> relatedNodes = getNodeList(
                WorkflowConstant.WEBSITE_WORKSPACE,
                nodeUuids,
                NodeTypes.Page.NAME,
                PageProperties.ATTACHMENT
        );
        log.info("AttachmentRelatedNodesFinder summary: path={}, ids={}, queries={}, matchedNodes={}",
                path, nodeUuids.size(), getQueryBatchCount(nodeUuids), relatedNodes.size());
        return relatedNodes;
    }

}
