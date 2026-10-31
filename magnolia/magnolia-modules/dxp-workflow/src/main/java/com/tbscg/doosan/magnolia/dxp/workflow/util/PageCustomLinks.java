package com.tbscg.doosan.magnolia.dxp.workflow.util;

import com.tbscg.doosan.magnolia.dxp.workflow.constant.WorkflowConstant;
import info.magnolia.jcr.util.PropertyUtil;
import lombok.extern.slf4j.Slf4j;

import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.RepositoryException;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

@Slf4j
public final class PageCustomLinks {

    private PageCustomLinks() {
    }

    public static Map<String, String> byLanguage(final Node node) {
        try {
            if (!node.hasNode(WorkflowConstant.CUSTOM_NAME_FOR_LANG)) {
                return Collections.emptyMap();
            }
            return readCustomLinks(node.getNode(WorkflowConstant.CUSTOM_NAME_FOR_LANG));
        } catch (RepositoryException e) {
            log.debug("Cannot capture custom links for node {}", info.magnolia.jcr.util.NodeUtil.getNodePathIfPossible(node), e);
            return Collections.emptyMap();
        }
    }

    private static Map<String, String> readCustomLinks(final Node languagesNode) throws RepositoryException {
        final Map<String, String> customLinksByLanguage = new LinkedHashMap<>();
        final NodeIterator iterator = languagesNode.getNodes();
        while (iterator.hasNext()) {
            final Node languageNode = iterator.nextNode();
            customLinksByLanguage.put(
                    languageNode.getName(),
                    PropertyUtil.getString(languageNode, WorkflowConstant.CUSTOM_LINK_PROPERTY, null));
        }
        return customLinksByLanguage;
    }
}
