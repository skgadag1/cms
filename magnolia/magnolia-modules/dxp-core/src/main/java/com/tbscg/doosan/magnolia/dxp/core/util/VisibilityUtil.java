package com.tbscg.doosan.magnolia.dxp.core.util;

import com.tbscg.doosan.magnolia.dxp.core.Component;
import com.tbscg.doosan.magnolia.dxp.core.Content;
import com.tbscg.doosan.magnolia.dxp.core.Visibility;
import info.magnolia.jcr.predicate.NodeTypePredicate;
import info.magnolia.jcr.util.SessionUtil;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.util.Strings;

import javax.jcr.Node;
import javax.jcr.RepositoryException;
import java.util.*;
import java.util.stream.Collectors;

import static com.tbscg.doosan.magnolia.dxp.core.util.NodeUtil.*;
import static info.magnolia.jcr.util.NodeUtil.*;

@Slf4j
@UtilityClass
public class VisibilityUtil {

    private static final String PAGE_VISIBILITY_PARAM = "pageVisibility";

    private static final String COMPONENT_VISIBILITY_PARAM = "componentVisibility";

    private static final String NODES_PARAM = "nodes";

    private static final String CONTENT_VISIBILITY_PARAM = "contentVisibility";

    private static final String VISIBILITY_PARAM = "visibility";
    private static final String VISIBILITY_WORKSPACE = "visibility";

    private static final String CATEGORIES_WORKSPACE = "category";
    private static final String COMPONENT_NODE_TYPE = "mgnl:component";
    private static final String PAGE_NODE_TYPE = "mgnl:page";
    private static final String CONTRACT_TYPES_PROPERTY = "contractTypes";
    private static final String CONTRACT_CODES_PROPERTY = "contractCodes";
    private static final String DEALER_ROLE_PROPERTY = "dealerRole";

    private static final String AREA_TYPE = "mgnl:area";

    private static final String COMPONENT_TYPE = "mgnl:component";

    private static final String IS_TAG_SWITCHABLE = "isTagSwitchable";


    public static List<Node> getParentVisibilityNodes(final Node node, final String root) {
        String rootPath = Optional.ofNullable(root).orElse(Strings.EMPTY);
        List<Node> parentNodes = new ArrayList<>();
        Optional<Node> parent = tryGetParent(node);
        while (parent.isPresent() && !rootPath.equals(getPath(parent.get()))) {
            parentNodes.add(parent.get());
            parent = tryGetParent(parent.get());
        }
        return parentNodes.stream()
                .map(p -> PropertyUtil.tryGetProperty(p, PAGE_VISIBILITY_PARAM))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .map(PropertyUtil::getStringValues)
                .flatMap(Collection::stream)
                .map(visibilityUuid -> SessionUtil.getNodeByIdentifier(VISIBILITY_WORKSPACE, visibilityUuid))
                .distinct()
                .collect(Collectors.toList());
    }

    public static List<Node> getVisibilityNodes(final Node node, final String propertyName) {
        return PropertyUtil.tryGetProperty(node, propertyName)
                .map(PropertyUtil::getStringValues)
                .orElseGet(Collections::emptyList).stream()
                .map(visibilityUuid -> SessionUtil.getNodeByIdentifier(VISIBILITY_WORKSPACE, visibilityUuid))
                .collect(Collectors.toList());
    }

    public static Optional<Node> getVisibilityNode(final Node node, final String propertyName) {
        return PropertyUtil.tryGetProperty(node, propertyName)
                .map(PropertyUtil::getStringValue)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .map(visibilityUuid -> SessionUtil.getNodeByIdentifier(VISIBILITY_WORKSPACE, visibilityUuid));
    }

    public static Visibility mapNodeToVisibility(Node node) {
        return Visibility.builder()
                .withContractType(getCategoryNameByIdentifier(node, CONTRACT_TYPES_PROPERTY))
                .withContractCode(getCategoryNameByIdentifier(node, CONTRACT_CODES_PROPERTY))
                .withDealerRole(getCategoryNameByIdentifier(node, DEALER_ROLE_PROPERTY))
                .build();
    }

    public static List<Visibility> getPageVisibility(final String pagePath, final String workspace, final String root) {
        Optional<Node> pageNode = collectDxpNode(pagePath, workspace);
        if (pageNode.isPresent()) {
            Set<Node> visibilityNodes = getVisibilityNodes(pageNode.get(), PAGE_VISIBILITY_PARAM).stream().collect(Collectors.toSet());
            pageNode.map(node -> getParentVisibilityNodes(node, root)).ifPresent(visibilityNodes::addAll);
            return visibilityNodes.stream()
                    .map(VisibilityUtil::mapNodeToVisibility)
                    .collect(Collectors.toList());
        }
        return Collections.emptyList();
    }

    public static List<Component> getPageComponentsWithVisibiility(final String pagePath, final String workspace) {
        List<Node> componentNodes = getComponentNodes(pagePath, workspace);
        return componentNodes.stream().map(node -> {
                    Set<Node> visibilityNodes = getVisibilityNodes(node, COMPONENT_VISIBILITY_PARAM).stream().collect(Collectors.toSet());
                    List<Visibility> componentVisibility = visibilityNodes.stream()
                            .map(VisibilityUtil::mapNodeToVisibility)
                            .collect(Collectors.toList());
                    return Component.builder()
                            .withPath(getNodePathIfPossible(node))
                            .withId(getNodeIdentifierIfPossible(node))
                            .withVisibility(componentVisibility)
                            .withContent(getContentVisibility(node))
                            .build();
                })
                .collect(Collectors.toList());
    }

    public static List<Node> getComponentNodes(String pagePath, String workspace) {
        Optional<Node> pageNode = collectDxpNode(pagePath, workspace);
        List<Node> componentNodes = new ArrayList<>();
        pageNode.ifPresent(node -> {
            try {
                Iterable<Node> areas = info.magnolia.jcr.util.NodeUtil.getNodes(node, AREA_TYPE);
                for (Node area : areas) {
                    collectAllChildren(area, new NodeTypePredicate(COMPONENT_TYPE)).forEach(componentNodes::add);
                }
            } catch (RepositoryException e) {
                log.warn("Error while fetching component nodes for page {}", pagePath);
            }
        });
        return componentNodes;
    }

    private static List<Content> getContentVisibility(final Node componentNode) {
        Map<String, List<Visibility>> contentVisibilityMap = new HashMap<>();
        Optional<Node> contentVisibilityNode = tryGetDirectNode(componentNode, CONTENT_VISIBILITY_PARAM);
        Collection<Node> contentVisibilityNodes = contentVisibilityNode.map(NodeUtil::tryGetDirectNodes).orElseGet(Collections::emptyList);
        for (Node contentVisibility : contentVisibilityNodes) {
            List<String> nodes = PropertyUtil.tryGetProperty(contentVisibility, NODES_PARAM)
                    .map(PropertyUtil::getStringValues)
                    .orElseGet(Collections::emptyList);
            Optional<Visibility> visibilityNode = getVisibilityNode(contentVisibility, VISIBILITY_PARAM).map(VisibilityUtil::mapNodeToVisibility);
            if (visibilityNode.isPresent()) {
                nodes.forEach(node -> {
                    if (!contentVisibilityMap.containsKey(node)) {
                        contentVisibilityMap.put(node, new ArrayList<>(Arrays.asList(visibilityNode.get())));
                    } else {
                        contentVisibilityMap.get(node).add(visibilityNode.get());
                    }
                });
            }
        }
        NodeUtil.tryGetDirectNodes(componentNode).stream()
                .filter(node -> PropertyUtil.tryGetProperty(node, IS_TAG_SWITCHABLE).isPresent())
                .map(NodeUtil::getName)
                .filter(StringUtils::isNotBlank)
                .forEach(contentNode -> {
                    if (!contentVisibilityMap.containsKey(contentNode)) {
                        contentVisibilityMap.put(contentNode, Collections.emptyList());
                    }
                });
        return contentVisibilityMap.entrySet().stream()
                .map(entry -> {
                    Optional<Node> contentNode = tryGetDirectNode(componentNode, entry.getKey());
                    return contentNode.map(content -> Content.builder()
                            .withPath(getNodePathIfPossible(content))
                            .withId(getNodeIdentifierIfPossible(content))
                            .withVisibility(entry.getValue())
                            .build()).orElse(null);
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    private static Optional<Node> tryGetParent(Node item) {
        return tryGetParentNodeWithTypes(item, Arrays.asList(PAGE_NODE_TYPE, COMPONENT_NODE_TYPE));
    }

    private static String getCategoryNameByIdentifier(Node node, String path) {
        return NodeUtil.getName(SessionUtil.getNodeByIdentifier(CATEGORIES_WORKSPACE, PropertyUtil.tryGetStringValue(node, path)));
    }
}
