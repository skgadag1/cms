package com.tbscg.doosan.magnolia.dxp.workflow.rules;

import info.magnolia.cms.util.Rule;
import info.magnolia.jcr.util.NodeTypes;

import javax.jcr.Node;
import javax.jcr.RepositoryException;

public class DxpVersionCommandRule extends Rule {
    public DxpVersionCommandRule() {
        addAllowType(NodeTypes.MetaData.NAME);
        addAllowType(NodeTypes.Resource.NAME);
        addAllowType(NodeTypes.ContentNode.NAME);
    }

    @Override
    public boolean isAllowed(Node node) throws RepositoryException {
        if (node.isLocked()) {
            return true;
        }
        return super.isAllowed(node);
    }
}
