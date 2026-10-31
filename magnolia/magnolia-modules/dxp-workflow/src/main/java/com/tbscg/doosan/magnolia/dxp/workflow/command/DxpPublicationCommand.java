package com.tbscg.doosan.magnolia.dxp.workflow.command;

import com.tbscg.doosan.magnolia.bobcat.field.filter.NodeFilter;
import com.tbscg.doosan.magnolia.bobcat.ui.contentapp.LockedPagesPublishFilteringPredicate;
import com.tbscg.doosan.magnolia.bobcat.ui.contentapp.ModelFilteringPredicate;
import com.tbscg.doosan.magnolia.dxp.core.ActionType;
import com.tbscg.doosan.magnolia.dxp.workflow.constant.WorkflowConstant;
import com.tbscg.doosan.magnolia.dxp.workflow.service.EventHandlingService;
import com.tbscg.doosan.magnolia.dxp.workflow.service.IndexingService;
import com.tbscg.doosan.magnolia.dxp.workflow.service.configuration.EventHandlingServiceConfiguration;
import com.tbscg.doosan.magnolia.dxp.workflow.service.configuration.IndexingServiceConfiguration;
import com.tbscg.doosan.magnolia.dxp.workflow.service.exception.IndexingException;
import com.tbscg.doosan.magnolia.dxp.workflow.service.relatedNodesFinding.RelatedNodesFinder;
import com.tbscg.doosan.magnolia.dxp.workflow.util.PublicationRequestContext;
import com.tbscg.doosan.magnolia.dxp.workflow.util.PublicationScopes;
import com.tbscg.doosan.magnolia.dxp.workflow.util.WorkflowUtil;
import info.magnolia.cms.core.version.VersionManager;
import info.magnolia.context.Context;
import info.magnolia.context.SystemContext;
import info.magnolia.jcr.predicate.RuleBasedNodePredicate;
import info.magnolia.jcr.util.NodeTypes;
import info.magnolia.jcr.util.NodeUtil;
import info.magnolia.objectfactory.ComponentProvider;
import info.magnolia.objectfactory.Components;
import info.magnolia.publishing.command.PublicationCommand;
import info.magnolia.publishing.exception.PublicationException;
import info.magnolia.publishing.operation.SendOperation;
import info.magnolia.publishing.sender.Sender;
import info.magnolia.publishing.util.PublicationUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import javax.inject.Inject;
import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static com.tbscg.doosan.magnolia.dxp.core.util.LoggingUtil.callAndLogTiming;
import static com.tbscg.doosan.magnolia.dxp.workflow.service.configuration.EventHandlingServiceConfiguration.isEventHandleableNotVersionedWorkspace;
import static com.tbscg.doosan.magnolia.dxp.workflow.util.WorkflowUtil.getVersionTime;
import static com.tbscg.doosan.magnolia.dxp.workflow.util.WorkflowUtil.setVersionTime;

/**
 * DXP publication command with Elasticsearch indexing. Implemented on top of default PublicationCommand.
 * Indexing is executed after page is published. The indexing process is only executed for pages in website workspace
 * and that belong to given list of allowed to index sites. Indexer sends the path of published page to DynamoDB table
 * that triggers an event to Lambda that will parse the page and send it to Elasticsearch to index.
 */
@Slf4j
public class DxpPublicationCommand extends PublicationCommand {

    /**
     * Copy of {@link PublicationCommand#NODES_BOTTOM_UP_COMPARATOR}
     */
    private static final Comparator<? super Map<String, String>> NODES_BOTTOM_UP_COMPARATOR = (o1, o2) -> {
        if (o1.equals(o2)) {
            return 0;
        }

        String path1 = o1.get("path");
        String path2 = o2.get("path");
        if (path1.equals(path2)) {
            return 0;
        }
        if (path2.startsWith(path1)) {
            // o2 is child of o1, say o1 is smaller to get it ordered BEFORE o2
            return -1;
        }
        path1 = StringUtils.removeEnd(path1, "/");
        path2 = StringUtils.removeEnd(path2, "/");
        String parent1 = StringUtils.substringBeforeLast(path1, "/");
        String parent2 = StringUtils.substringBeforeLast(path2, "/");
        if (parent1.equals(parent2)) {
            // siblings ... to reverse order, the higher index value get ordered before lower values index
            int idx1 = Integer.parseInt(o1.get("index"));
            int idx2 = Integer.parseInt(o2.get("index"));
            // index is generated in the loop above and can be never same for 2 items ...
            if (idx1 == idx2) {
                return 0;
            }
            return idx1 < idx2 ? 1 : -1;
        }

        // unrelated entries, the one closer to the root should be returned first
        int dirLevels1 = StringUtils.countMatches(path1, "/");
        int dirLevels2 = StringUtils.countMatches(path2, "/");
        // Since parents are checked above, equality means a different hierarchy
        // of the same depth and is irrelevant to activation order.
        if (dirLevels1 == dirLevels2) {
            return 0;
        }
        return dirLevels1 < dirLevels2 ? -1 : 1;
    };
    private static final String IS_DELETION_CTX_PARAM = "isDeletion";
    /**
     * Workspaces whose apps are configured to set "catalog: versioned" only on non-recursive publication;
     * for those we let the parent flow handle version time stamping and skip our explicit pre-pass.
     */
    private static final Set<String> WORKSPACES_WITHOUT_EXPLICIT_VERSION_TIME_HANDLING = Set.of(
            WorkflowConstant.WEBSITE_WORKSPACE,
            WorkflowConstant.CONCEPTS_WORKSPACE);
    private final IndexingService indexingService;
    private final EventHandlingService eventHandlingService;
    private final RelatedNodesFinder relatedNodesFinder;

    private NodeFilter nodeFilter;

    @Inject
    public DxpPublicationCommand(final VersionManager versionManager,
                                 final ComponentProvider componentProvider,
                                 final IndexingService indexingService,
                                 final EventHandlingService eventHandlingService,
                                 final RelatedNodesFinder relatedNodesFinder) {
        super(versionManager, componentProvider);

        this.indexingService = indexingService;
        this.eventHandlingService = eventHandlingService;
        this.relatedNodesFinder = relatedNodesFinder;
    }

    private static boolean isDeletion(final Context context) {
        final Object isDeletionParamValue = context.get(IS_DELETION_CTX_PARAM);
        if (isDeletionParamValue instanceof Boolean isDeletion) {
            return isDeletion;
        }
        return isDeletionParamValue instanceof String isDeletion && Boolean.parseBoolean(isDeletion);
    }

    public void setNodeFilterParams(final Map<String, ?> nodeFilterParamsMap) {
        log.debug("setNodeFilterParams({})", nodeFilterParamsMap);
        this.nodeFilter = NodeFilter.fromPropertyMap(nodeFilterParamsMap);
    }

    @Override
    public boolean execute(final Context context) throws Exception {
        try (PublicationRequestContext ignored = PublicationRequestContext.open(getRepository(), getPath(), isRecursive(), PublicationScopes.fromContext(context))) {
            log.info("Executing publish: {}", context);
            final boolean requiresVersionTimeHandling = !WORKSPACES_WITHOUT_EXPLICIT_VERSION_TIME_HANDLING.contains(getRepository());
            if (requiresVersionTimeHandling && (isRecursive() || isEventHandleableNotVersionedWorkspace(getWorkspace()))) {
                callAndLogTiming(() -> {
                    setVersionTime(getPath(), getRepository(), isRecursive());
                    return null;
                }, log, "Version time handling");
            }
            final Node node = getPublicationNode();
            final String publishTime = getVersionTime(node);
            return isDeletion(context) ? handleDeletion(context, publishTime) : handlePublication(context, publishTime);
        }
    }

    NodeFilter getNodeFilter() {
        return nodeFilter;
    }

    private boolean handleDeletion(final Context context, final String publishTime) throws Exception {
        try {
            handleDeletionEvent();
            return deindex(publishTime);
        } catch (final IndexingException e) {
            log.error("Re-indexing error on page delete action. Deindexing aborted.", e);
            return true;
        } finally {
            defaultHandlePublish(context);
        }
    }

    private boolean handlePublication(final Context context, final String publishTime) throws Exception {
        try {
            defaultHandlePublish(context);
            handlePublicationEvent(parseStringDateToEpochSecond(publishTime));
            return index(publishTime);
        } catch (final Exception e) {
            log.error("Error when publishing.", e);
            throw e;
        }
    }

    private static long parseStringDateToEpochSecond(final String publishTime) {
        return Instant.parse(publishTime).getEpochSecond();
    }

    private boolean deindex(final String publishTime) throws Exception {
        final String workspace = getRepository();
        return IndexingServiceConfiguration.isIndexableWorkspace(workspace)
                ? callAndLogTiming(
                        () -> indexingService.deindex(workspace, getPath(), getUuid(), publishTime),
                        log,
                        "Deindex handling")
                : true;
    }

    private void handleDeletionEvent() throws Exception {
        final String workspace = getRepository();
        if (EventHandlingServiceConfiguration.isEventHandleableWorkspace(workspace)) {
            callAndLogTiming(
                    () -> eventHandlingService.handleEvent(ActionType.DELETE, workspace, getPath(), getUuid(), true),
                    log,
                    "Deletion event handling");
        }
    }

    private void handlePublicationEvent(final long publishTime) throws Exception {
        final String workspace = getRepository();
        if (EventHandlingServiceConfiguration.isEventHandleableWorkspace(workspace)) {
            callAndLogTiming(
                    () -> eventHandlingService.handleEvent(
                            ActionType.PUBLISH,
                            workspace,
                            getPath(),
                            getUuid(),
                            isRecursive(),
                            publishTime),
                    log,
                    "Publication event handling");
        }
        handleRelatedNodesPublishingEvent(publishTime, workspace);
    }

    private boolean index(String publishTime) throws Exception {
        final String workspace = super.getRepository();
        return IndexingServiceConfiguration.isIndexableWorkspace(workspace)
                ? callAndLogTiming(
                        () -> indexingService.index(workspace, getPath(), getUuid(), publishTime, isRecursive()),
                        log,
                        "Indexing handling")
                : true;
    }

    private void defaultHandlePublish(final Context context) throws Exception {
        callAndLogTiming(() -> {
            doPublishNodes(context);
            return null;
        }, log, "Default publish handling");
    }

    /**
     * Copy of {@link PublicationCommand#execute(info.magnolia.context.Context)}
     */
    private void doPublishNodes(final Context ctx) throws Exception {
        final Sender sender = getComponentProvider().newInstance(Sender.class, ctx);
        final Node toPublish = getPublicationNode();

        List<Node> collectedNodes = collect(toPublish, ctx);
        final int initiallyCollectedCount = collectedNodes.size();

        // BEGIN: custom code
        collectedNodes = filterNodes(collectedNodes);
        final int afterNodeFilterCount = collectedNodes.size();
        collectedNodes = handleLockedPagesFiltering(collectedNodes);
        final int afterLockedPageFilterCount = collectedNodes.size();
        collectedNodes = WorkflowUtil.applyCurrentPublicationScope(getWorkspace(), collectedNodes);
        final int finalCount = collectedNodes.size();
        // END: custom code
        log.info(
                "Publish sender scope summary: workspace={}, path={}, recursive={}, collected={}, "
                        + "nodeFilterRemoved={}, lockedRemoved={}, scopeRemoved={}, final={}",
                getWorkspace(),
                getPath(),
                isRecursive(),
                initiallyCollectedCount,
                initiallyCollectedCount - afterNodeFilterCount,
                afterNodeFilterCount - afterLockedPageFilterCount,
                afterLockedPageFilterCount - finalCount,
                finalCount);

        final List<SendOperation.OperationResult> results = sender.publish(collectedNodes, getRule());

        final List<SendOperation.OperationResult> errors = results.stream()
                .filter(result -> !result.isSuccess())
                .toList();

        if (!errors.isEmpty()) {
            errors.forEach(result -> log.error(
                    "Receiver: {}, error: {}",
                    result.getReceiverName(),
                    result.getException().getMessage(),
                    result.getException()));
            throw new PublicationException(PublicationUtil.buildErrorMessage(errors));
        }
    }

    private Node getPublicationNode() throws RepositoryException {
        final Session session = Components.getComponent(SystemContext.class).getJCRSession(getRepository());
        return StringUtils.isNotEmpty(getUuid())
                ? session.getNodeByIdentifier(getUuid())
                : session.getNode(getPath());
    }

    private List<Node> filterNodes(final List<Node> nodes) {
        if (nodeFilter != null) {
            final ModelFilteringPredicate modelFilteringPredicate = new ModelFilteringPredicate(this.nodeFilter);
            return nodes.stream()
                    .filter(modelFilteringPredicate::evaluateTyped)
                    .toList();
        }
        return nodes;
    }

    private List<Node> handleLockedPagesFiltering(final List<Node> nodes) {
        final LockedPagesPublishFilteringPredicate predicate = new LockedPagesPublishFilteringPredicate();
        return nodes.stream()
                .filter(predicate::evaluateTyped)
                .toList();
    }

    /**
     * Copy of {@link PublicationCommand#collect(Node, Context)}
     */
    private List<Node> collect(Node publicationRoot, final Context ctx) throws RepositoryException {
        if (isRecursive()) {
            if (getVersionMap() != null) {
                return collectFromVersionMap(getVersionMap(), ctx);
            } else {
                return collectPublicationRootsRecursively(publicationRoot);
            }
        }

        final List<Node> result = new ArrayList<>();
        if (StringUtils.isNotEmpty(getVersion())) {
            publicationRoot = getVersionManager().getVersion(publicationRoot, getVersion());
        }
        result.add(publicationRoot);
        return result;
    }

    /**
     * Copy of {@link PublicationCommand#collectPublicationRootsRecursively(Node)}
     */
    private List<Node> collectPublicationRootsRecursively(final Node node) throws RepositoryException {
        final List<Node> result = new ArrayList<>();

        if (!isModifiedOnly() || NodeTypes.Activatable.getActivationStatus(node) != NodeTypes.Activatable.ACTIVATION_STATUS_ACTIVATED) {
            result.add(node);
        }

        // Apply the inverted rule to pick up the filtered out "folder"-type nodes for publication.
        // This way (as far as it is possible to judge from the legacy code) the recursiveness is achieved.
        final List<Node> childRoots = NodeUtil.asList(NodeUtil.getNodes(node, new RuleBasedNodePredicate(getRule()) {
            @Override
            public boolean evaluateTyped(final Node content) {
                return !super.evaluateTyped(content);
            }
        }));

        // FYI: need to reverse order of child publication since content ordering info is also bottom-up.
        // If changing this, don't forget to also change other collectFromVersionMap() method and most importantly
        // ensure that ordering of siblings in PublicationOperation in receiver is done in same direction!
        Collections.reverse(childRoots);
        for (final Node child : childRoots) {
            result.addAll(collectPublicationRootsRecursively(child));
        }

        return result;
    }

    /**
     * Copy of {@link PublicationCommand#collectFromVersionMap(List, Context)}
     */
    private List<Node> collectFromVersionMap(final List<Map<String, String>> versionMap, final Context context) throws RepositoryException {
        int index = 0;
        for (final Iterator<Map<String, String>> iterator = versionMap.iterator(); iterator.hasNext(); ) {
            final Map<String, String> entry = iterator.next();
            final String uuid = entry.get("uuid");
            try {
                final Node node = NodeUtil.getNodeByIdentifier(getWorkspace(), uuid);
                entry.put("path", node.getPath() + "/");
                entry.put("index", String.valueOf(index++));
            } catch (final RepositoryException re) {
                log.error("Failed to activate node with UUID: {}", uuid, re);
                iterator.remove();
            }
        }

        // versionMap is a flat list of all activated content. Ensure that content
        // is ordered top-down and siblings are activated bottom-up.
        versionMap.sort(NODES_BOTTOM_UP_COMPARATOR);

        final List<Node> nodes = new ArrayList<>();
        final Session session = context.getJCRSession(getWorkspace());

        for (final Map<String, String> entry : versionMap) {
            final String uuid = entry.get("uuid");
            final String versionNumber = entry.get("version");
            try {
                Node node = session.getNodeByIdentifier(uuid);
                if (StringUtils.isNotEmpty(versionNumber)) {
                    node = getVersionManager().getVersion(node, versionNumber);
                }
                if (!isModifiedOnly()
                        || NodeTypes.Activatable.getActivationStatus(node) != NodeTypes.Activatable.ACTIVATION_STATUS_ACTIVATED) {
                    nodes.add(node);
                }
            } catch (final RepositoryException e) {
                log.debug("Failed to collect node with uuid [{}].", uuid, e);
            }
        }

        return nodes;
    }

    /**
     * Copy of {@link PublicationCommand#getWorkspace()}
     *
     * @return current workspace name.
     */
    private String getWorkspace() {
        return getRepository();
    }

    private void handleRelatedNodesPublishingEvent(final long publishTime, final String workspace) throws Exception {
        final List<Node> relatedNodes = callAndLogTiming(
                () -> this.relatedNodesFinder.findRelatedNodesForPublication(workspace, isRecursive(), getPath()),
                log,
                "Related node discovery");
        log.debug("Related node publication scope: workspace={}, path={}, recursive={}, relatedNodes={}",
                workspace, getPath(), isRecursive(), relatedNodes.size());
        relatedNodes.forEach(relatedNode -> {
            final String relatedNodePath = NodeUtil.getPathIfPossible(relatedNode);
            try {
                final String relatedNodeUUID = relatedNode.getIdentifier();
                final String relatedNodeWorkspace = relatedNode.getSession().getWorkspace().getName();
                callAndLogTiming(
                        () -> eventHandlingService.handleEvent(
                                ActionType.PUBLISH,
                                relatedNodeWorkspace,
                                relatedNodePath,
                                relatedNodeUUID,
                                false,
                                publishTime),
                        log,
                        "Publication event handling");
            } catch (final Exception e) {
                log.error("Error when publishing related node {}.", relatedNode, e);
            }
        });
    }
}
