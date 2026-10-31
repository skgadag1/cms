package com.tbscg.doosan.magnolia.dealernet.util;

import com.tbscg.doosan.magnolia.dxp.core.util.NodeUtil;
import com.tbscg.doosan.magnolia.dxp.core.util.PropertyUtil;
import info.magnolia.jcr.util.SessionUtil;
import info.magnolia.ui.datasource.optionlist.Option;
import org.apache.logging.log4j.util.Strings;

import javax.jcr.Node;
import javax.jcr.Property;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.tbscg.doosan.magnolia.dxp.core.util.VisibilityUtil.getParentVisibilityNodes;

public final class DealernetUtils {

    private static final String COMPONENT_NODE_TYPE = "mgnl:component";
    private static final String VISIBILITY_WORKSPACE = "visibility";
    private static final String VISIBILITY_PARAM = "visibility";
    private static final String COMPONENT_VISIBILITY_PARAM = "componentVisibility";
    private static final String CONTENT_VISIBILITY_PARAM = "contentVisibility";
    private static final String NODES = "nodes";

    private DealernetUtils() {
    }

    public static List<Node> getComponentVisibilityNodes(final Node node, final String root) {
        String rootPath = Optional.ofNullable(root).orElse(Strings.EMPTY);
        List<Node> parentNodes = getParentVisibilityNodes(node, rootPath);
        List<Node> componentNodes = new ArrayList<>();
        Optional<Property> componentVisibility = NodeUtil.tryGetParentNodeWithType(node, COMPONENT_NODE_TYPE)
                .map(parent -> PropertyUtil.tryGetProperty(parent, DealernetUtils.COMPONENT_VISIBILITY_PARAM))
                .orElse(Optional.empty());
        componentVisibility.ifPresent(visibility ->
                componentNodes.addAll(PropertyUtil.getStringValues(visibility).stream()
                        .map(visibilityUuid -> SessionUtil.getNodeByIdentifier(VISIBILITY_WORKSPACE, visibilityUuid))
                        .collect(Collectors.toList())));
        return Stream.concat(parentNodes.stream(), componentNodes.stream())
                .distinct()
                .collect(Collectors.toList());
    }

    public static List<Node> getContentNodeVisibilityNodes(final Node node, final String fieldName) {
        Optional<Node> componentNode = NodeUtil.tryGetParentNodeWithType(node, COMPONENT_NODE_TYPE);
        if (componentNode.isPresent()) {
            return NodeUtil.tryGetDirectNode(componentNode.get(), CONTENT_VISIBILITY_PARAM)
                    .map(NodeUtil::tryGetDirectNodes)
                    .orElseGet(Collections::emptyList).stream()
                    .filter(contentVisibility -> PropertyUtil.tryGetProperty(contentVisibility, NODES)
                            .map(PropertyUtil::getStringValues)
                            .orElseGet(Collections::emptyList).contains(fieldName))
                    .map(contentVisibility -> PropertyUtil.tryGetProperty(contentVisibility, VISIBILITY_PARAM))
                    .filter(Optional::isPresent)
                    .map(Optional::get)
                    .map(PropertyUtil::getStringValue)
                    .filter(Optional::isPresent)
                    .map(Optional::get)
                    .map(visibilityUuid -> SessionUtil.getNodeByIdentifier(VISIBILITY_WORKSPACE, visibilityUuid))
                    .distinct()
                    .collect(Collectors.toList());
        }
        return Collections.emptyList();
    }

    public static Option mapNodeToOption(Node node) {
        Option option = new Option();
        option.setName(info.magnolia.jcr.util.NodeUtil.getName(node));
        option.setLabel(info.magnolia.jcr.util.NodeUtil.getName(node));
        option.setValue(info.magnolia.jcr.util.NodeUtil.getName(node));
        return option;
    }
}
