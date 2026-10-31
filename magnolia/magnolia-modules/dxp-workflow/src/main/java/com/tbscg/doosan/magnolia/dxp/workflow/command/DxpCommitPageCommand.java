package com.tbscg.doosan.magnolia.dxp.workflow.command;

import com.tbscg.doosan.magnolia.dxp.core.util.NodeUtil;
import com.tbscg.doosan.magnolia.dxp.workflow.service.path.mapping.DxpCustomPathMappingService;
import info.magnolia.commands.impl.BaseRepositoryCommand;
import info.magnolia.context.Context;
import info.magnolia.jcr.util.SessionUtil;

import javax.inject.Inject;
import javax.jcr.Node;

import static org.apache.commons.lang3.StringUtils.EMPTY;

public class DxpCommitPageCommand extends BaseRepositoryCommand {

    private final DxpCustomPathMappingService dxpCustomPathMappingService;

    @Inject
    public DxpCommitPageCommand(DxpCustomPathMappingService dxpCustomPathMappingService) {
        this.dxpCustomPathMappingService = dxpCustomPathMappingService;
    }

    @Override
    public boolean execute(Context context) {
        Node node = SessionUtil.getNode("website", context.getAttribute("path"));
        String path = NodeUtil.getPath(node);
        String nodeType = NodeUtil.getPrimaryNodeType(node);
        String workspace = NodeUtil.tryGetNodeWorkspace(node).orElse(EMPTY);
        this.dxpCustomPathMappingService.updatePageWithCustomName(node, path, nodeType, workspace);
        return true;
    }
}
