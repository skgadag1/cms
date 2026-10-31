package com.tbscg.doosan.magnolia.dxp.workflow.command;

import com.tbscg.doosan.magnolia.dxp.core.util.NodeUtil;
import com.tbscg.doosan.magnolia.dxp.workflow.rules.DxpVersionCommandRule;
import com.tbscg.doosan.magnolia.dxp.workflow.service.path.mapping.DxpCustomPathMappingService;
import com.tbscg.doosan.magnolia.dxp.workflow.util.WorkflowUtil;
import info.magnolia.cms.core.version.VersionManager;
import info.magnolia.cms.security.Permission;
import info.magnolia.commands.impl.VersionCommand;
import info.magnolia.context.Context;
import info.magnolia.context.MgnlContext;
import org.apache.commons.lang3.StringUtils;

import javax.inject.Inject;
import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.RepositoryException;
import javax.jcr.version.Version;

import java.util.*;

import static com.tbscg.doosan.magnolia.dxp.workflow.service.configuration.EventHandlingServiceConfiguration.isEventHandleableWorkspace;
import static com.tbscg.doosan.magnolia.dxp.workflow.util.WorkflowUtil.filterOutLockedNodes;
import static org.apache.commons.lang3.StringUtils.EMPTY;


public class DxpVersionCommand extends VersionCommand {

    private final DxpCustomPathMappingService dxpCustomPathMappingService;
    private final VersionManager versionManager;

    @Inject
    public DxpVersionCommand(VersionManager versionManager, DxpCustomPathMappingService dxpCustomPathMappingService) {
        super(versionManager);
        this.versionManager = versionManager;
        this.dxpCustomPathMappingService = dxpCustomPathMappingService;
    }

    @Override
    public boolean execute(Context ctx) throws Exception {
        // BEGIN: custom code
        if (isEventHandleableWorkspace(getRepository())) {
            WorkflowUtil.setVersionTime(getPath(), getRepository(), isRecursive());
        }
        try {
            registerMapping();
        } catch (Exception e) {
            log.warn("Failed to register mapping for path {}", getPath(), e);
        }
        setRule(new DxpVersionCommandRule());
        // END: custom code

        final Node node = getJCRNode(ctx, Permission.WRITE);
        if (isRecursive()) {
            // set versionMap and version name for this node
            List<Map<String, String>> versionMap = new ArrayList<>();
            versionNodes(node, ctx, versionMap);
            ctx.setAttribute(Context.ATTRIBUTE_VERSION_MAP, versionMap, Context.LOCAL_SCOPE);
        } else {
            Version version = versionManager.addVersion(node, getRule(),
                    StringUtils.isBlank(getUserName()) ? (MgnlContext.getUser() != null ? MgnlContext.getUser().getName() : EMPTY) : getUserName(),
                    getComment() != null ? getComment() : EMPTY
            );
            if (version != null) {
                ctx.setAttribute(Context.ATTRIBUTE_VERSION, version.getName(), Context.LOCAL_SCOPE);
            }
        }

        return true;
    }

    private void versionNodes(Node node, Context ctx, List<Map<String, String>> versionMap) throws RepositoryException {
        String rootNodeType = node.getPrimaryNodeType().getName();
        List<Node> nodes = collectPrimaryNodesToVersion(node);
        nodes.forEach(vNode -> {
            try {
                versionRecursively(vNode, ctx, versionMap, rootNodeType);
            } catch (RepositoryException e) {
                log.error("Error while versioning node {}", vNode);
                throw new RuntimeException(e);

            }
        });
    }

    private List<Node> collectPrimaryNodesToVersion(Node rootNode) throws RepositoryException {
        List<Node> nodes = new ArrayList<>(NodeUtil.getNodes(rootNode, rootNode.getPrimaryNodeType().getName()));
        nodes.add(rootNode);
        Collections.reverse(nodes);
        nodes = filterOutLockedNodes(nodes);
        return nodes;
    }

    private void versionRecursively(Node node, Context ctx, List<Map<String, String>> versionMap, String rootNodeType) throws RepositoryException {
        Version version = versionManager.addVersion(node, getDefaultRule(),
                StringUtils.isBlank(getUserName()) ? (MgnlContext.getUser() != null ? MgnlContext.getUser().getName() : EMPTY) : getUserName(),
                getComment() != null ? getComment() : EMPTY
        );

        Map<String, String> entry = new HashMap<>();
        entry.put("uuid", node.getIdentifier());
        if (version != null) {
            entry.put("version", version.getName());
            if (StringUtils.isEmpty(Objects.toString(ctx.getAttribute(Context.ATTRIBUTE_VERSION), EMPTY))) {
                ctx.setAttribute(Context.ATTRIBUTE_VERSION, version.getName(), Context.LOCAL_SCOPE);
            }
        }
        versionMap.add(entry);

        NodeIterator children = node.getNodes();
        while (children.hasNext()) {
            Node child = children.nextNode();
            if (isNodeToPublish(child, rootNodeType)) {
                versionRecursively(child, ctx, versionMap, rootNodeType);
            }
        }
    }

    private boolean isNodeToPublish(Node node, String type) throws RepositoryException {
        return !node.getPrimaryNodeType().getName().equals(type) && !getRule().isAllowed(node);
    }

    private void registerMapping() throws Exception {
        dxpCustomPathMappingService.updateMappings(getPath(), getRepository(), isRecursive());
    }

}
