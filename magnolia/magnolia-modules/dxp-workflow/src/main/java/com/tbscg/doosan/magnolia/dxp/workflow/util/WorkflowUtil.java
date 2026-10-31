package com.tbscg.doosan.magnolia.dxp.workflow.util;

import com.google.common.base.Preconditions;
import com.tbscg.doosan.magnolia.dxp.core.Component;
import com.tbscg.doosan.magnolia.dxp.workflow.constant.WorkflowConstant;
import com.tbscg.doosan.magnolia.dxp.workflow.model.PublicationItem;
import com.tbscg.doosan.magnolia.dxp.workflow.model.PublicationWorkset;
import com.tbscg.doosan.magnolia.product.catalog.util.ProductCatalogConstants.NodeTypes.AttachmentGroup;
import info.magnolia.cms.util.Rule;
import info.magnolia.context.Context;
import info.magnolia.jcr.predicate.NodeTypePredicate;
import info.magnolia.jcr.util.NodeTypes;
import info.magnolia.jcr.util.NodeUtil;
import info.magnolia.jcr.util.PropertyUtil;
import info.magnolia.jcr.util.VersionUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.kie.api.runtime.process.WorkItem;

import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.Property;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import java.time.Instant;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static com.amazonaws.util.StringUtils.COMMA_SEPARATOR;
import static com.tbscg.doosan.magnolia.dxp.core.util.NodeUtil.*;
import static com.tbscg.doosan.magnolia.dxp.core.util.VisibilityUtil.getComponentNodes;
import static com.tbscg.doosan.magnolia.product.catalog.util.ProductCatalogConstants.ATTACHMENTS_WORKSPACE;
import static com.tbscg.doosan.magnolia.product.catalog.util.ProductCatalogConstants.Lifecycle.CURRENT;
import static com.tbscg.doosan.magnolia.product.catalog.util.ProductCatalogConstants.Lifecycle.HISTORICAL;
import static com.tbscg.doosan.magnolia.product.catalog.util.ProductCatalogConstants.Properties.LIFECYCLE;

/**
 * Utility class used for Magnolia-based BPMN workflows.
 */
@Slf4j
public final class WorkflowUtil {

    private static final Set<String> EVENT_SENDABLE_SITES = Set.of(
            WorkflowConstant.DEALERNET_SITE,
            WorkflowConstant.BOBCAT_SITE);

    private WorkflowUtil() {
    }

    public static Node getNodeFromWorkItem(WorkItem workItem, Context context) throws RepositoryException {
        Preconditions.checkNotNull(context, "Context can't be null!");
        final String nodePath = getNodePathFromWorkItem(workItem);
        final String repository = getJcrRepositoryFromWorkItem(workItem);
        return context.getJCRSession(repository).getNode(nodePath);
    }

    public static String getNodePathFromWorkItem(WorkItem workItem) {
        final Map<String, Object> mgnlData = getMgnlDataFromWorkItem(workItem);
        final String nodePath = (String) mgnlData.get(WorkflowConstant.PATH);
        Preconditions.checkArgument(StringUtils.isNotBlank(nodePath), "Node path can't be blank!");
        return nodePath;
    }

    public static String getJcrRepositoryFromWorkItem(WorkItem workItem) {
        final Map<String, Object> mgnlData = getMgnlDataFromWorkItem(workItem);
        final String repository = (String) mgnlData.get(WorkflowConstant.REPOSITORY);
        Preconditions.checkArgument(StringUtils.isNotBlank(repository), "JCR repository can't be blank!");
        return repository;
    }

    public static Session getSessionFromWorkItem(WorkItem workItem, Context context) throws RepositoryException {
        final String repository = getJcrRepositoryFromWorkItem(workItem);
        return context.getJCRSession(repository);
    }

    public static String getTaskIdFromWorkItem(WorkItem workItem) {
        final String taskId = (String) workItem.getParameter(WorkflowConstant.PARAMETER_TASK_ID);
        Preconditions.checkArgument(StringUtils.isNotBlank(taskId), "Task ID can't be blank!");
        return taskId;
    }

    public static String getPublisherFromWorkItem(WorkItem workItem) {
        final Map<String, Object> mgnlData = getMgnlDataFromWorkItem(workItem);
        final String publisher = (String) mgnlData.get(WorkflowConstant.PUBLISHER);
        Preconditions.checkArgument(StringUtils.isNotBlank(publisher), "Publisher can't be blank!");
        return publisher;
    }

    public static String createVersionTime() {
        return Instant.now().toString();
    }

    public static String getVersionTime(final Node node) {
        final String versionTime = com.tbscg.doosan.magnolia.dxp.core.util.PropertyUtil.tryGetStringValue(
                node, WorkflowExecutionSettings.VERSION_TIME_PROPERTY, null);
        if (StringUtils.isNotBlank(versionTime)) {
            return versionTime;
        }
        final String lastModified = com.tbscg.doosan.magnolia.dxp.core.util.PropertyUtil.tryGetStringValue(
                node, NodeTypes.LastModified.LAST_MODIFIED, null);
        return StringUtils.isNotBlank(lastModified) ? lastModified : createVersionTime();
    }

    public static void setVersionTime(final Node node, final String versionTime) throws RepositoryException {
        setOrUpdateProperty(node, WorkflowExecutionSettings.VERSION_TIME_PROPERTY, versionTime);
    }

    public static void setVersionTime(final String path, final String workspace, final boolean isRecursive) throws RepositoryException {
        final String versionTime = createVersionTime();
        final List<Node> nodes = applyCurrentPublicationScope(workspace, collectPublicationWorkset(path, workspace, isRecursive).resolveNodes());
        Session sessionToSave = null;
        int dirtyNodesForSession = 0;
        int sessionSaveCount = 0;
        for (Node node : nodes) {
            final Session nodeSession = node.getSession();
            if (sessionToSave != null && !sessionToSave.equals(nodeSession) && dirtyNodesForSession > 0) {
                sessionToSave.save();
                sessionSaveCount++;
                dirtyNodesForSession = 0;
            }
            sessionToSave = nodeSession;
            setVersionTime(node, versionTime);
            dirtyNodesForSession++;
            if (dirtyNodesForSession >= WorkflowExecutionSettings.VERSION_TIME_SESSION_SAVE_BATCH_SIZE) {
                sessionToSave.save();
                sessionSaveCount++;
                dirtyNodesForSession = 0;
            }
        }
        if (sessionToSave != null && dirtyNodesForSession > 0) {
            sessionToSave.save();
            sessionSaveCount++;
        }
        final int totalSessionSaveCount = sessionSaveCount;
        PublicationRequestContext.current().ifPresent(context -> context.addVersionSessionSaves(totalSessionSaveCount));
        log.info("Version time stamping summary: workspace={}, path={}, recursive={}, nodes={}, sessionSaves={}",
                workspace, path, isRecursive, nodes.size(), sessionSaveCount);
    }

    /**
     * Collects the raw publication nodes. Scoped publish flows should pass the result through
     * {@link #applyCurrentPublicationScope(String, List)} before using it for counts, events, or writes.
     */
    public static List<Node> collectNodesForPublish(final String path, final String workspace, final boolean isRecursive) {
        return collectPublicationWorkset(path, workspace, isRecursive).resolveNodes();
    }

    public static List<Node> collectNodesForPublish(final Node node, final boolean isRecursive) throws RepositoryException {
        return collectNodesForPublish(node.getPath(), node.getSession().getWorkspace().getName(), isRecursive);
    }

    public static List<Node> applyCurrentPublicationScope(final String workspace, final List<Node> nodes) {
        return PublicationRequestContext.current()
                .map(context -> context.filterNodes(workspace, nodes))
                .orElse(nodes);
    }

    public static boolean isInCurrentPublicationScope(final String workspace, final PublicationItem item) {
        return PublicationRequestContext.current()
                .map(context -> context.includes(workspace, item))
                .orElse(true);
    }

    public static PublicationWorkset collectPublicationWorkset(final String path, final String workspace, final boolean isRecursive) {
        return PublicationRequestContext.current()
                .map(context -> context.getOrCreateWorkset(workspace, path, isRecursive, () -> buildPublicationWorkset(path, workspace, isRecursive)))
                .orElseGet(() -> buildPublicationWorkset(path, workspace, isRecursive));
    }

    public static List<Node> filterOutLockedNodes(List<Node> nodes) {
        return nodes.stream()
                .filter(node -> !isNodeLocked(node))
                .toList();
    }


    /**
     * Retrieves Magnolia-specific contextual data for a given work item.
     *
     * @param workItem BPMN work item to get data from.
     * @return map with contextual data.
     */
    @SuppressWarnings("unchecked")
    public static Map<String, Object> getMgnlDataFromWorkItem(WorkItem workItem) {
        Preconditions.checkNotNull(workItem, "Work item can't be null");
        return (Map<String, Object>) workItem.getParameter(WorkflowConstant.PARAMETER_MGNL_DATA);
    }

    /**
     * Creates a default rule for node versioning within workflows.
     *
     * @param node node to create rule from.
     * @return default rule.
     * @throws RepositoryException if any problem occurs when operating with the repository.
     */
    public static Rule getDefaultRule(Node node) throws RepositoryException {
        final Rule rule = new Rule(VersionUtil.getNodeTypeName(node) + COMMA_SEPARATOR + NodeTypes.System.NAME, COMMA_SEPARATOR);
        rule.reverse();
        return rule;
    }

    public static boolean isEventSendableSite(String site) {
        return EVENT_SENDABLE_SITES.contains(site);
    }

    public static List<Component> getPageComponents(final String pagePath, final String workspace) {
        List<Node> componentNodes = getComponentNodes(pagePath, workspace);
        return componentNodes.stream()
                .map(component -> Component.builder()
                        .withId(NodeUtil.getNodeIdentifierIfPossible(component))
                        .withPath(NodeUtil.getPathIfPossible(component))
                        .build())
                .collect(Collectors.toList());
    }

    public static List<Node> filterProductsByLifecycle(final List<Node> nodes, final String lifecycle) {
        if (isUnsupportedProductLifecycle(lifecycle)) {
            return nodes;
        }
        return nodes.stream()
                .filter(node -> matchesProductLifecycle(lifecycle, getProductLifecycle(node)))
                .toList();
    }

    public static boolean matchesProductLifecycle(final String lifecycle, final String nodeLifecycle) {
        return isUnsupportedProductLifecycle(lifecycle)
                || nodeLifecycle == null
                || lifecycle.equals(nodeLifecycle);
    }

    public static boolean isUnsupportedProductLifecycle(final String lifecycle) {
        return !CURRENT.equals(lifecycle) && !HISTORICAL.equals(lifecycle);
    }

    // Retrieves the path of an attachment group that the attachment belongs to.
    public static String getAttachmentGroupPath(String path) throws RepositoryException {
        final Node node = com.tbscg.doosan.magnolia.dxp.core.util.NodeUtil
                .collectDxpNode(path, ATTACHMENTS_WORKSPACE)
                .orElseThrow(RepositoryException::new);
        if (isAttachmentGroup(node)) {
            return null;
        }
        Node parent = node.getParent();
        if (isAttachmentGroup(parent)) {
            return parent.getPath();
        }
        return null;
    }

    public static boolean isAttachmentGroup(Node node) {
        return AttachmentGroup.NAME.equals(getPrimaryNodeType(node));
    }

    private static PublicationWorkset buildPublicationWorkset(final String path, final String workspace, final boolean isRecursive) {
        final List<PublicationItem> items = new ArrayList<>();
        try {
            final Node rootNode = collectDxpNode(path, workspace).orElseThrow(RepositoryException::new);
            if (!isRecursive) {
                addPublicationItem(items, rootNode, workspace);
            } else {
                collectRecursivePublicationItems(rootNode, path, workspace, items);
            }
        } catch (RepositoryException e) {
            log.warn("Cannot collect nodes for publish for workspace={} path={}: {}", workspace, path, e.getMessage());
            log.debug("Cannot collect nodes for publish for workspace={} path={}", workspace, path, e);
        }
        log.info("Publication workset summary: workspace={}, path={}, recursive={}, items={}", workspace, path, isRecursive, items.size());
        return new PublicationWorkset(workspace, path, isRecursive, items);
    }

    private static void collectRecursivePublicationItems(final Node rootNode,
                                                         final String path,
                                                         final String workspace,
                                                         final List<PublicationItem> items) throws RepositoryException {
        switch (workspace) {
            case WorkflowConstant.WEBSITE_WORKSPACE -> collectWebsiteItems(rootNode, workspace, items);
            case WorkflowConstant.CONCEPTS_WORKSPACE -> collectConceptItems(rootNode, workspace, items, true);
            default -> collectGenericItems(path, workspace, items);
        }
    }

    private static void collectWebsiteItems(final Node rootNode,
                                            final String workspace,
                                            final List<PublicationItem> items) throws RepositoryException {
        addPublicationItem(items, rootNode, workspace);
        filterOutLockedNodes(NodeUtil.asList(NodeUtil.collectAllChildren(rootNode, new NodeTypePredicate(NodeTypes.Page.NAME))))
                .forEach(node -> addPublicationItem(items, node, workspace));
    }

    private static void collectGenericItems(final String path,
                                            final String workspace,
                                            final List<PublicationItem> items) {
        filterOutLockedNodes(collectAllDxpNodes(path, workspace))
                .forEach(node -> addPublicationItem(items, node, workspace));
    }

    /**
     * Iterative BFS traversal of the concepts workspace tree. An explicit stack avoids
     * StackOverflowError on deep taxonomy trees that would exhaust the call stack under
     * the old recursive approach.
     */
    private static void collectConceptItems(final Node rootNode,
                                            final String workspace,
                                            final List<PublicationItem> items,
                                            final boolean includeRoot) throws RepositoryException {
        final ArrayDeque<Object[]> stack = new ArrayDeque<>();
        stack.push(new Object[]{rootNode, includeRoot});
        while (!stack.isEmpty()) {
            final Object[] entry = stack.pop();
            final Node node = (Node) entry[0];
            final boolean include = (boolean) entry[1];
            if (include) {
                addPublicationItem(items, node, workspace);
            }
            final NodeIterator iterator = node.getNodes();
            while (iterator.hasNext()) {
                final Node child = iterator.nextNode();
                if (!isNodeLocked(child)) {
                    final boolean includeChild = child.isNodeType(NodeTypes.Folder.NAME)
                            || child.isNodeType(NodeTypes.Content.NAME);
                    stack.push(new Object[]{child, includeChild});
                }
            }
        }
    }

    private static void addPublicationItem(final List<PublicationItem> items, final Node node, final String workspace) {
        try {
            if (isNodeLocked(node)) {
                return;
            }
            items.add(new PublicationItem(
                    workspace,
                    node.getPath(),
                    node.getIdentifier(),
                    getParentNodeId(node),
                    getPrimaryNodeType(node),
                    toInstant(getLastModifiedDate(node)),
                    WorkflowConstant.WEBSITE_WORKSPACE.equals(workspace) ? PageCustomLinks.byLanguage(node) : Collections.emptyMap(),
                    getProductLifecycle(node)
            ));
        } catch (RepositoryException e) {
            log.warn("Cannot capture publication item for workspace={}", workspace, e);
        }
    }

    private static String getProductLifecycle(final Node node) {
        final Property lifecycle = PropertyUtil.getPropertyOrNull(node, LIFECYCLE);
        return lifecycle == null ? null : PropertyUtil.getValueString(lifecycle);
    }

    private static Instant toInstant(final Calendar calendar) {
        return calendar == null ? null : calendar.toInstant();
    }

    private static boolean isNodeLocked(final Node node) {
        try {
            return node.isLocked();
        } catch (Exception e) {
            log.debug("Could not determine lock status for node {}", info.magnolia.jcr.util.NodeUtil.getNodePathIfPossible(node), e);
            return false;
        }
    }
}
