package com.tbscg.doosan.magnolia.dxp.workflow.util;

import com.tbscg.doosan.magnolia.dxp.workflow.service.exception.IndexingException;
import info.magnolia.jcr.util.NodeTypes;
import info.magnolia.jcr.util.NodeUtil;
import info.magnolia.jcr.util.PropertyUtil;
import info.magnolia.jcr.util.SessionUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;

import javax.jcr.*;
import java.util.*;
import java.util.stream.Collectors;

import static com.tbscg.doosan.magnolia.product.catalog.util.ProductCatalogConstants.Lifecycle.CURRENT;
import static com.tbscg.doosan.magnolia.product.catalog.util.ProductCatalogConstants.Lifecycle.HISTORICAL;
import static com.tbscg.doosan.magnolia.product.catalog.util.ProductCatalogConstants.Properties.LIFECYCLE;
import static com.tbscg.doosan.magnolia.product.catalog.util.ProductCatalogConstants.RelatedPage.NODE_NAME;
import static com.tbscg.doosan.magnolia.product.catalog.util.ProductCatalogConstants.RelatedPage.PATH;
import static com.tbscg.doosan.magnolia.product.catalog.util.ProductCatalogConstants.RelatedPage.URL;
import static info.magnolia.jcr.util.NodeTypes.Activatable.ACTIVATION_STATUS_ACTIVATED;

@Slf4j
public class ModelPageAssociationUtil {

    private static final String contentNodeType = "mgnl:contentNode";
    private static final String indexer = "indexer";
    private static final String mgnlTemplate = "mgnl:template";
    private static final String historicalModelPageTemplate = "bobcat:pages/historical-machine-details";
    private static final String modelPageTemplate = "bobcat:pages/machine-details";

    public static void removeDeletedPageAssociation(final Node page,
                                                    final String pagePath,
                                                    final List<Node> modelsToUpdate,
                                                    final String workspace,
                                                    final String nodeType) {
        if (CollectionUtils.isEmpty(modelsToUpdate) && com.tbscg.doosan.magnolia.dxp.core.util.NodeUtil.isDeleted(page)) {
            modelsToUpdate.addAll(IndexedItemUtil.findAssociatedModelsForPage(pagePath, workspace, nodeType));
            log.debug("Removing deleted page association for models: {}", modelsToUpdate.stream().map(NodeUtil::getPathIfPossible).collect(Collectors.joining(", ")));
            modelsToUpdate.forEach(model -> ModelPageAssociationUtil.removePageRelatedNode(model, page, nodeType));
        }
    }

    public static void updateProcessedNodes(final Node page,
                                            final List<Node> modelsToUpdate,
                                            final String nodeType,
                                            final String groupType) throws RepositoryException {
        final List<Session> sessions = new ArrayList<>();
        // Update nodes activation date in order to preserve published status after applying changes
        for (Node node : modelsToUpdate) {
            // Get group children
            final Collection<Node> childNodes = ModelPageAssociationUtil.isGroupType(node, groupType) ?
                    ModelPageAssociationUtil.getNodeCollection(node, nodeType, page) : Collections.singletonList(node);
            for (Node childNode : childNodes) {
                ModelPageAssociationUtil.updateActivationAndSessions(childNode, sessions);
            }
        }
        ModelPageAssociationUtil.updateActivationAndSessions(page, sessions);
        // Save processed nodes sessions
        for (Session session : sessions) {
            session.save();
        }
    }

    public static void removePageRelatedNode(final Node model,
                                             final Node page,
                                             final String nodeType) {
        // Prepare collection of nodes (more than 1 if model group is associated to the page)
        final Collection<Node> modelNodes = ModelPageAssociationUtil.getNodeCollection(model, nodeType, page);
        log.debug("Removing page related nodes size : {} for model {}", modelNodes.size(), NodeUtil.getNodePathIfPossible(model));
        for (Node node : modelNodes) {
            try {
                log.debug("Deleting associated page {} from model {}", NodeUtil.getNodePathIfPossible(page), NodeUtil.getNodePathIfPossible(node));
                com.tbscg.doosan.magnolia.dxp.core.util.NodeUtil.removeChildNode(node, NODE_NAME);
            } catch (RepositoryException e) {
                log.error("Cannot delete model {} associated page {}", NodeUtil.getNodePathIfPossible(node), NodeUtil.getNodePathIfPossible(page), e);
                throw new IndexingException("Cannot delete associated page", e);
            }
        }
    }

    public static void createRelatedPageNode(final Node model,
                                             final Node page,
                                             final Map<String, String> pathsForLang,
                                             final String nodeType) throws RepositoryException {
        // Prepare collection of nodes (more than 1 if model group is associated to the page)
        final Collection<Node> modelNodes = ModelPageAssociationUtil.getNodeCollection(model, nodeType, page);
        for (Node node : modelNodes) {
            // Create pageRelated node containing related page data
            NodeUtil.createPath(node, NODE_NAME, contentNodeType, false);
            final Node pageRelated = node.getNode(NODE_NAME);
            pageRelated.setProperty(PATH, NodeUtil.getPathIfPossible(page));
            for (Map.Entry<String, String> entry : pathsForLang.entrySet()) {
                log.debug("Adding url node {} for lang {} for model {}", entry.getValue(), entry.getKey(), NodeUtil.getNodePathIfPossible(node));
                // Create lang node containing localised url
                NodeUtil.createPath(pageRelated, entry.getKey(), contentNodeType, false);
                final Node langNode = pageRelated.getNode(entry.getKey());
                langNode.setProperty(URL, entry.getValue());
            }
        }
    }

    public static Map<String, String> extractUrls(final Node node) {
        Map<String, String> urls = new HashMap<>();
        try {
            if (node.hasNode(NODE_NAME)) {
                NodeIterator pageRelatedNodes = node.getNode(NODE_NAME).getNodes();
                NodeUtil.getCollectionFromNodeIterator(pageRelatedNodes)
                        .forEach(n -> {
                            Optional<Property> urlProperty = Optional.ofNullable(PropertyUtil.getPropertyOrNull(n, URL));
                            urlProperty.ifPresent(url -> urls.put(
                                    com.tbscg.doosan.magnolia.dxp.core.util.NodeUtil.getName(n),
                                    PropertyUtil.getValueString(url)
                            ));
                        });
            }
        } catch (RepositoryException e) {
            log.error("Cannot extract urls from model node {}", NodeUtil.getNodePathIfPossible(node));
        }
        return urls;
    }

    public static Collection<Node> getNodeCollection(final Node node, final String nodeType, final Node page) {
        Collection<Node> nodes = getNodeCollection(node, nodeType);
        String pageTemplate = PropertyUtil.getString(page, mgnlTemplate);

        return switch (pageTemplate) {
            case modelPageTemplate -> filterNodesByLifecycle(nodes, CURRENT);
            case historicalModelPageTemplate -> filterNodesByLifecycle(nodes, HISTORICAL);
            default -> nodes;
        };
    }

    public static boolean isGroupType(final Node node, final String groupType) {
        return com.tbscg.doosan.magnolia.dxp.core.util.NodeUtil.getPrimaryNodeType(node).equals(groupType);
    }

    public static boolean isModelType(final Node node, final String nodeType) {
        return com.tbscg.doosan.magnolia.dxp.core.util.NodeUtil.getPrimaryNodeType(node).equals(nodeType);
    }

    private static void updateActivationAndSessions(final Node node,
        final List<Session> sessions) throws RepositoryException {
        final int activationStatus = NodeTypes.Activatable.getActivationStatus(node);
        if (ACTIVATION_STATUS_ACTIVATED == activationStatus) {
            NodeTypes.Activatable.update(node, indexer, true);
        }
        final Session nodeSession = node.getSession();
        final boolean sessionNotYetStored = sessions.stream()
            .noneMatch(session -> SessionUtil.hasSameUnderlyingSession(session, nodeSession));
        if (sessionNotYetStored) {
            sessions.add(nodeSession);
        }
    }

    private static Collection<Node> filterNodesByLifecycle(final Collection<Node> nodes, final String lifecycle) {
        return nodes.stream()
            .filter(model -> lifecycle.equals(PropertyUtil.getString(model, LIFECYCLE, null)))
            .collect(Collectors.toList());
    }

    private static Collection<Node> getNodeCollection(final Node node, final String nodeType) {
        if (ModelPageAssociationUtil.isModelType(node, nodeType)) {
            return Collections.singletonList(node);
        } else {
            try {
                return com.tbscg.doosan.magnolia.dxp.core.util.NodeUtil.getDirectNodes(node, nodeType);
            } catch (RepositoryException e) {
                log.error("Cannot get model nodes from group while adding page association.", e);
            }
        }
        return Collections.emptyList();
    }
}
