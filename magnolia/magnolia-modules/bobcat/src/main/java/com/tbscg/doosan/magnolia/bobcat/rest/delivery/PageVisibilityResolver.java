package com.tbscg.doosan.magnolia.bobcat.rest.delivery;

import com.tbscg.doosan.magnolia.bobcat.rest.model.PageVisibility;
import com.tbscg.doosan.magnolia.dxp.core.util.NodeUtil;
import com.tbscg.doosan.magnolia.dxp.core.util.PropertyUtil;
import info.magnolia.context.MgnlContext;
import info.magnolia.rest.reference.ReferenceResolver;
import info.magnolia.rest.reference.jcr.JcrReferenceResolverDefinition;
import lombok.extern.slf4j.Slf4j;
import org.apache.logging.log4j.util.Strings;

import javax.inject.Inject;
import javax.jcr.Node;
import javax.jcr.RepositoryException;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import static info.magnolia.context.MgnlContext.getJCRSession;

@Slf4j
public class PageVisibilityResolver implements ReferenceResolver<String, Object> {
    private static final String WEBSITE_WORKSPACE = "website";
    private static final String VISIBILITY_WORKSPACE = "visibility";
    private static final String CATEGORY_WORKSPACE = "category";
    private static final String LINK_NODE = "link";
    private static final String PAGE_URL_PROPERTY = "pageUrl";
    private static final String PAGE_VISIBILITY_PROPERTY = "pageVisibility";
    private static final String PAGE_VISIBILITY_CONTRACT_TYPE = "contractTypes";
    private static final String PAGE_VISIBILITY_CONTRACT_CODE = "contractCodes";
    private static final String PAGE_VISIBILITY_DEALER_ROLE = "dealerRole";
    private final JcrReferenceResolverDefinition definition;

    @Inject
    public PageVisibilityResolver(final JcrReferenceResolverDefinition definition) {
        this.definition = definition;
    }

    @Override
    public Optional<Object> resolve(String itemID) {
        Optional<Node> navigationNode = getNodeByTypeAndId(definition.getTargetWorkspace(), itemID);
        List<String> visibilityNodeIds = navigationNode.map(this::getPageNodeId)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .map(pageId -> getNodeByTypeAndId(WEBSITE_WORKSPACE, pageId))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .map(this::getVisibilityNodeIds)
                .orElseGet(Collections::emptyList);

        PageVisibility[] pageVisibilityList = visibilityNodeIds.stream()
                .map(visibilityNodeId -> getNodeByTypeAndId(VISIBILITY_WORKSPACE, visibilityNodeId))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .map(visibilityNode -> {
                    String contractCodeNodeId = PropertyUtil.tryGetStringValue(visibilityNode, PAGE_VISIBILITY_CONTRACT_CODE);
                    String contractTypeNodeId = PropertyUtil.tryGetStringValue(visibilityNode, PAGE_VISIBILITY_CONTRACT_TYPE);
                    String dealerRoleNodeId = PropertyUtil.tryGetStringValue(visibilityNode, PAGE_VISIBILITY_DEALER_ROLE);
                    Optional<Node> contractCode = getNodeByTypeAndId(CATEGORY_WORKSPACE, contractCodeNodeId);
                    Optional<Node> contractType = getNodeByTypeAndId(CATEGORY_WORKSPACE, contractTypeNodeId);
                    Optional<Node> dealerRole = getNodeByTypeAndId(CATEGORY_WORKSPACE, dealerRoleNodeId);
                    return new PageVisibility(
                            contractCode.map(NodeUtil::getName).orElse(Strings.EMPTY),
                            contractType.map(NodeUtil::getName).orElse(Strings.EMPTY),
                            dealerRole.map(NodeUtil::getName).orElse(Strings.EMPTY)
                    );
                })
                .filter(Objects::nonNull)
                .toArray(PageVisibility[]::new);

        return Optional.ofNullable(pageVisibilityList);
    }

    private Optional<String> getPageNodeId(Node navigationNode) {
        return NodeUtil.tryGetDirectNode(navigationNode, LINK_NODE)
                .map(node -> NodeUtil.tryGetDirectNode(node, LINK_NODE)
                        .map(linkNode -> PropertyUtil.tryGetStringValue(linkNode, PAGE_URL_PROPERTY))
                        .orElse(Strings.EMPTY));
    }

    private List<String> getVisibilityNodeIds(Node pageNode) {
        return PropertyUtil.tryGetProperty(pageNode, PAGE_VISIBILITY_PROPERTY)
                .map(PropertyUtil::getStringValues)
                .orElseGet(Collections::emptyList);
    }

    private Optional<Node> getNodeByTypeAndId(String workspace, String nodeId) {
        return MgnlContext.doInSystemContext(() -> {
            try {
                return Optional.of(getJCRSession(workspace).getNodeByIdentifier(nodeId));
            } catch (RepositoryException e) {
                log.warn("Node with id {} not exists in of workspace {}", workspace, nodeId);
                return Optional.empty();
            }
        });
    }
}
