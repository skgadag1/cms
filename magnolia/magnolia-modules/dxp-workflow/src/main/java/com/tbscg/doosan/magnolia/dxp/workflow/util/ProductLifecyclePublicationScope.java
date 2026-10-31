package com.tbscg.doosan.magnolia.dxp.workflow.util;

import com.tbscg.doosan.magnolia.dxp.workflow.model.PublicationItem;

import javax.jcr.Node;
import java.util.List;

import static com.tbscg.doosan.magnolia.product.catalog.util.ProductCatalogConstants.PRODUCTS_WORKSPACE;

record ProductLifecyclePublicationScope(String lifecycle) implements PublicationScope {

    @Override
    public List<Node> filterNodes(final String workspace, final List<Node> nodes) {
        if (!PRODUCTS_WORKSPACE.equals(workspace)) {
            return nodes;
        }
        return WorkflowUtil.filterProductsByLifecycle(nodes, lifecycle);
    }

    @Override
    public boolean includes(final String workspace, final PublicationItem item) {
        return !PRODUCTS_WORKSPACE.equals(workspace)
                || WorkflowUtil.matchesProductLifecycle(lifecycle, item.productLifecycle());
    }
}
