package com.tbscg.doosan.magnolia.dxp.workflow.service.relatedNodesFinding;

import com.tbscg.doosan.magnolia.dxp.workflow.constant.WorkflowConstant;
import com.tbscg.doosan.magnolia.product.catalog.util.ProductCatalogConstants.PageProperties;
import info.magnolia.jcr.util.NodeTypes;
import lombok.extern.slf4j.Slf4j;

import javax.jcr.Node;
import java.util.ArrayList;
import java.util.List;

@Slf4j
public class ProductRelatedNodesFinder extends RelatedNodesFinder {

    @Override
    protected List<Node> findRelatedNodesForSeeds(final List<String> nodeUuids,
                                                  final String path) {
        final List<Node> relatedNodes = new ArrayList<>();
        relatedNodes.addAll(getNodeList(WorkflowConstant.WEBSITE_WORKSPACE, nodeUuids, NodeTypes.Page.NAME, PageProperties.PRODUCT));
        relatedNodes.addAll(getNodeList(WorkflowConstant.WEBSITE_WORKSPACE, nodeUuids, NodeTypes.Page.NAME, PageProperties.PRODUCT_CATEGORY));
        log.info("ProductRelatedNodesFinder summary: path={}, ids={}, queries={}, matchedNodes={}",
                path, nodeUuids.size(), getQueryBatchCount(nodeUuids) * 2, relatedNodes.size());
        return relatedNodes;
    }

}
