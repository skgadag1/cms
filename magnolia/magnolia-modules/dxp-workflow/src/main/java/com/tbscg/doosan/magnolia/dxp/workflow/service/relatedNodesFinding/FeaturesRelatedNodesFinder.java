package com.tbscg.doosan.magnolia.dxp.workflow.service.relatedNodesFinding;

import com.tbscg.doosan.magnolia.dxp.workflow.constant.WorkflowConstant;
import com.tbscg.doosan.magnolia.product.catalog.util.ProductCatalogConstants.NodeTypes.Product;
import com.tbscg.doosan.magnolia.product.catalog.util.ProductCatalogConstants.NodeTypes.ProductGroup;
import com.tbscg.doosan.magnolia.product.catalog.util.ProductCatalogConstants.PageProperties;
import info.magnolia.jcr.util.NodeTypes;
import info.magnolia.jcr.util.NodeUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import javax.jcr.Node;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static com.tbscg.doosan.magnolia.product.catalog.util.ProductCatalogConstants.PRODUCTS_WORKSPACE;

@Slf4j
public class FeaturesRelatedNodesFinder extends RelatedNodesFinder {

    @Override
    protected List<Node> findRelatedNodesForSeeds(final List<String> nodeUuids,
                                                  final String path) {
        List<Node> relatedNodes = new ArrayList<>();
        String[] productNodeTypes = {Product.NAME, ProductGroup.NAME};
        List<Node> relatedProducts = getNodeList(PRODUCTS_WORKSPACE, nodeUuids, productNodeTypes, Product.Properties.FEATURES);
        List<String> relatedProductIds = relatedProducts.stream()
                .map(NodeUtil::getNodeIdentifierIfPossible)
                .filter(StringUtils::isNotBlank)
                .toList();
        List<Node> relatedPages = relatedProductIds.isEmpty()
                ? Collections.emptyList()
                : getNodeList(WorkflowConstant.WEBSITE_WORKSPACE, relatedProductIds, NodeTypes.Page.NAME, PageProperties.PRODUCT);
        relatedNodes.addAll(relatedProducts);
        relatedNodes.addAll(relatedPages);
        final int queryCount = getQueryBatchCount(nodeUuids) + getQueryBatchCount(relatedProductIds);
        log.info("FeaturesRelatedNodesFinder summary: path={}, ids={}, queries={}, matchedProducts={}, matchedPages={}, matchedNodes={}",
                path,
                nodeUuids.size(),
                queryCount,
                relatedProducts.size(),
                relatedPages.size(),
                relatedNodes.size());
        return relatedNodes;
    }

}
