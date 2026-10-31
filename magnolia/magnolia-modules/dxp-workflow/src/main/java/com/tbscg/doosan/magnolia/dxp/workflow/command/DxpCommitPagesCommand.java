package com.tbscg.doosan.magnolia.dxp.workflow.command;

import com.tbscg.doosan.magnolia.dxp.core.util.NodeUtil;
import com.tbscg.doosan.magnolia.dxp.workflow.service.path.mapping.DxpCustomPathMappingService;
import info.magnolia.commands.impl.BaseRepositoryCommand;
import info.magnolia.context.Context;
import info.magnolia.jcr.predicate.NodeTypePredicate;
import info.magnolia.jcr.util.NodeTypes;
import info.magnolia.jcr.util.SessionUtil;

import javax.inject.Inject;
import javax.jcr.Node;

import static org.apache.commons.lang3.StringUtils.EMPTY;

public class DxpCommitPagesCommand extends BaseRepositoryCommand {

    private final DxpCustomPathMappingService dxpCustomPathMappingService;

    @Inject
    public DxpCommitPagesCommand(DxpCustomPathMappingService dxpCustomPathMappingService) {
        this.dxpCustomPathMappingService = dxpCustomPathMappingService;
    }

    @Override
    public boolean execute(Context context) throws Exception {
        Node node = SessionUtil.getNode("website",  context.getAttribute("path"));
        Iterable<Node> nodes = info.magnolia.jcr.util.NodeUtil.collectAllChildren(node, new NodeTypePredicate(NodeTypes.Page.NAME));
        nodes.forEach(n -> {
            String path = NodeUtil.getPath(n);
            String nodeType = NodeUtil.getPrimaryNodeType(n);
            String workspace = NodeUtil.tryGetNodeWorkspace(n).orElse(EMPTY);
            this.dxpCustomPathMappingService.updatePageWithCustomName(n, path, nodeType, workspace);
        });
        return true;
    }
}
