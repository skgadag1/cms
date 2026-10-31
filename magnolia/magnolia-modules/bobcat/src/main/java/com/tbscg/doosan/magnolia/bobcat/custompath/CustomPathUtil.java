package com.tbscg.doosan.magnolia.bobcat.custompath;

import com.tbscg.doosan.magnolia.dxp.core.util.NodeUtil;
import com.tbscg.doosan.magnolia.dxp.core.util.PropertyUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ArrayUtils;

import javax.jcr.Node;
import javax.jcr.RepositoryException;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
public final class CustomPathUtil {
    private static final String customNameForLang = "customNameForLang";
    private static final String customLinkProperty = "customLink";

    public static String toCustomLink(final boolean isCustomPathResolutionEnabled, final Node node, final String lang) {
        final String path = NodeUtil.getPath(node);
        log.debug("toCustomLink({}, {})", path, lang);
        try {
            return isCustomPathResolutionEnabled ? CustomPathUtil.getCustomLinkFromNode(node, path, lang) : ParsedPath.toParsedPath(path, lang).toPathWithLang();
        } catch (Exception ex) {
            log.error("Cannot resolve custom path for path: {} and lang: {}", path, lang, ex);
            return "";
        }
    }

    public static String getCustomLinkFromNode(final Node node, final String path, final String lang) {
        try {
            log.debug("Get customLink from node ", path);
            final Node langNode = node.getNode(customNameForLang).getNode(lang);
            final String customLink = PropertyUtil.tryGetStringValue(langNode, customLinkProperty);
            log.debug("customLink from jcr = {}", customLink);
            return customLink;
        } catch (RepositoryException e) {
            log.error("Error while getting customLink from jcr = {}", path);
            return ParsedPath.toParsedPath(path, lang).toPathWithLang();
        }
    }

    public static List<String> getAllCustomLinksFromNode(final Node node) {
        try{
            String path = info.magnolia.jcr.util.NodeUtil.getNodePathIfPossible(node);
            log.debug("Get all custom links from node {}", path);
            Node customNamesForLangNode = node.getNode(customNameForLang);
            Collection<Node> langNodes = NodeUtil.getNodes(customNamesForLangNode);
            return langNodes
                    .stream()
                    .map(langNode -> PropertyUtil.tryGetStringValue(langNode, customLinkProperty))
                    .collect(Collectors.toList());
        } catch (RepositoryException e){
            log.error("Error while getting custom links", e);
            return Collections.emptyList();
        }
    }


    public static RootPathAndNodeName splitToRootPathAndNodeName(String path) {
        final String[] pathParts = path.split("/");
        final String[] basePathParts = ArrayUtils.subarray(pathParts, 0, pathParts.length - 1);

        return RootPathAndNodeName.builder()
                .withRootPath(String.join("/", basePathParts))
                .withNodeName(pathParts[pathParts.length - 1])
                .build();
    }
}
