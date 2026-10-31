package com.tbscg.doosan.magnolia.dxp.workflow.service.path.mapping;

import com.tbscg.doosan.magnolia.dxp.core.util.NodeUtil;
import com.tbscg.doosan.magnolia.dxp.core.util.PropertyUtil;
import com.tbscg.doosan.magnolia.dxp.workflow.model.PathMapping;
import com.tbscg.doosan.magnolia.dxp.workflow.model.PathMappingEntry;
import com.tbscg.doosan.magnolia.dxp.workflow.service.exception.CustomPathMappingException;
import info.magnolia.jcr.util.NodeTypes;
import org.apache.commons.lang3.StringUtils;

import javax.jcr.Node;
import javax.jcr.RepositoryException;
import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.Deque;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import static com.tbscg.doosan.magnolia.dxp.core.util.PropertyUtil.tryGetBooleanValue;
import static com.tbscg.doosan.magnolia.dxp.workflow.constant.WorkflowConstant.*;
import static org.apache.commons.lang3.StringUtils.EMPTY;


public class DxpCustomPathResolver implements CustomPathResolver {

    private static final String PATH_DELIMITER = "/";
    private static final String UNDEFINED_DOMAIN_NAME = "undefined";

    @Override
    public PathMapping resolveCustomPaths(final Node node) throws CustomPathMappingException {
        try {
            // languages will be the same across region
            final Set<String> languages = getCustomNameForLangMap(node).keySet();
            final Map<String, PathMappingEntry> lang2MappingMap = new HashMap<>();
            for (final String lang : languages) {
                lang2MappingMap.put(lang, toMappingEntry(node, lang, buildCustomPathFromParent(node, lang)));
            }
            this.updateCustomNameForLangMap(node, lang2MappingMap);
            return PathMapping.builder()
                    .withDomain(resolveDomainName(node))
                    .withLang2MappingMap(lang2MappingMap)
                    .build();
        } catch (Exception e) {
            throw new CustomPathMappingException("Failed to resolve custom paths ", e);
        }
    }

    private void updateCustomNameForLangMap(final Node node, final Map<String, PathMappingEntry> lang2MappingMap) throws RepositoryException {
        final Node customNameForLangNode = node.getNode(CUSTOM_NAME_FOR_LANG);
        final String region = customNameForLangNode.getProperty(REGION_PROPERTY).getString();
        for (final Node langNode : NodeUtil.getDirectNodes(customNameForLangNode)) {
            final String lang = langNode.getName();
            NodeUtil.setOrUpdateProperty(langNode, CUSTOM_LINK_PROPERTY, this.resolveCustomPath(region, lang, lang2MappingMap.get(lang).getCustomPath()));
            langNode.getSession().save();
        }
    }

    private String resolveCustomPath(final String region, final String lang, final String path) {
        final List<String> params = Arrays.asList(region, lang, path);
        if (params.stream().allMatch(StringUtils::isNotEmpty)) {
            final String pathWithoutRegion = path.replaceFirst("/" + region, StringUtils.EMPTY);
            return "/" + region + "/" + lang + pathWithoutRegion;
        }
        throw new CustomPathMappingException("Path not eligible for custom path resolver");
    }

    private static Map<String, String> getCustomNameForLangMap(Node node) throws RepositoryException {
        Node customNameForLangNode = node.getNode(CUSTOM_NAME_FOR_LANG);
        return NodeUtil.getDirectNodes(customNameForLangNode).stream()
                .collect(Collectors.toMap(
                        NodeUtil::getName,
                        customNameNode -> PropertyUtil.tryGetStringValue(customNameNode, CUSTOM_NAME_PROPERTY))
                );
    }

    private static String resolveDomainName(final Node node) {
        final String jcrPath = NodeUtil.getPath(node);
        final String[] split = jcrPath.split(PATH_DELIMITER);
        return split.length > 1 ? split[1] : UNDEFINED_DOMAIN_NAME;
    }

    private static PathMappingEntry toMappingEntry(final Node node, final String lang, final String customPath) throws RepositoryException {
        final boolean isHidden = tryGetBooleanValue(node, COMPLETELY_HIDDEN_PROPERTY);
        final boolean isRemoved = tryGetBooleanValue(node, REMOVE_FROM_URL_PROPERTY);

        return PathMappingEntry.builder()
                .withUuid(node.getIdentifier())
                .withJcrPath(normalizeJcrPath(node.getPath()))
                .withHidden(isHidden)
                .withRemoved(isRemoved)
                .withCustomPath(customPath + evaluateCustomNameForRootNode(node, lang))
                .withActivated(NodeUtil.isActivated(node))
                .build();
    }

    private static String evaluateCustomNameForChildNode(final Node node, final String lang) throws RepositoryException {
        return tryGetBooleanValue(node, REMOVE_FROM_URL_PROPERTY) ? EMPTY : getCustomNamePart(node, lang);
    }

    private static String evaluateCustomNameForRootNode(final Node node, final String lang) throws RepositoryException {
        return tryGetBooleanValue(node, REMOVE_FROM_URL_PROPERTY) ? getCustomNamePart(node, lang) : EMPTY;
    }

    private static String getCustomNamePart(final Node node, final String lang) throws RepositoryException {
        return PATH_DELIMITER + getCustomNameForLangMap(node).get(lang);
    }

    private static String buildCustomPathFromParent(final Node node, final String lang) throws RepositoryException {
        return String.join(EMPTY, getParentCustomPathParts(node, lang));
    }

    private static Deque<String> getParentCustomPathParts(final Node node, final String lang) throws RepositoryException {
        final Deque<String> parts = new ArrayDeque<>();
        Optional<Node> nodeToProcess = Optional.of(node);
        while (nodeToProcess.isPresent()) {
            final Node currentNode = nodeToProcess.get();
            parts.push(evaluateCustomNameForChildNode(currentNode, lang));
            nodeToProcess = NodeUtil.tryGetParentNode(currentNode)
                    .filter(DxpCustomPathResolver::isNotBobcatSite)
                    .filter(parent -> NodeTypes.Page.NAME.equals(NodeUtil.getPrimaryNodeType(parent)));
        }
        return parts;
    }

    private static boolean isNotBobcatSite(final Node node) {
        return !"/bobcat".equals(NodeUtil.getPath(node));
    }

    private static String normalizeJcrPath(final String jcrPath) {
        final String[] split = jcrPath.split(PATH_DELIMITER);
        return split.length > 2 ? PATH_DELIMITER + String.join(PATH_DELIMITER, Arrays.copyOfRange(split, 2, split.length)) : PATH_DELIMITER;
    }

}
