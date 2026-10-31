package com.tbscg.doosan.magnolia.dxp.workflow.service.relatedNodesFinding;

import com.tbscg.doosan.magnolia.dxp.core.util.NodeUtil;
import com.tbscg.doosan.magnolia.dxp.workflow.constant.WorkflowConstant;
import com.tbscg.doosan.magnolia.dxp.workflow.model.PublicationItem;
import com.tbscg.doosan.magnolia.dxp.workflow.util.WorkflowExecutionSettings;
import com.tbscg.doosan.magnolia.dxp.workflow.util.WorkflowUtil;
import info.magnolia.cms.util.QueryUtil;
import info.magnolia.jcr.util.NodeTypes;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.Value;
import java.util.*;
import java.util.stream.Collectors;

import static com.tbscg.doosan.magnolia.product.catalog.util.ProductCatalogConstants.ATTACHMENTS_WORKSPACE;
import static com.tbscg.doosan.magnolia.product.catalog.util.ProductCatalogConstants.PRODUCTS_WORKSPACE;
import static org.apache.commons.lang3.StringUtils.EMPTY;


@Slf4j
public class RelatedNodesFinder {

    private static final String SQL_LANGUAGE = "JCR-SQL2";
    private final Map<String, RelatedNodesFinder> nodeFinderMap = new HashMap<>();
    @Inject
    AttachmentRelatedNodesFinder attachmentRelatedNodesFinder;
    @Inject
    ProductRelatedNodesFinder productRelatedNodesFinder;
    @Inject
    DxpTagsRelatedNodesFinder dxpTagsRelatedNodesFinder;
    @Inject
    FacetRelatedNodesFinder facetRelatedNodesFinder;
    @Inject
    FeaturesRelatedNodesFinder featuresRelatedNodesFinder;
    @Inject
    MarketingCampaignRelatedNodesFinder marketingCampaignRelatedNodesFinder;
    @Inject
    ConceptsRelatedNodesFinder conceptsRelatedNodesFinder;
    @Inject
    FormEmailConfigRelatedNodesFinder formEmailConfigRelatedNodesFinder;
    @Inject
    FormConfigurationRelatedNodesFinder formConfigurationRelatedNodesFinder;

    @PostConstruct
    public void initNodeFinderMap() {
        nodeFinderMap.put(ATTACHMENTS_WORKSPACE, attachmentRelatedNodesFinder);
        nodeFinderMap.put(PRODUCTS_WORKSPACE, productRelatedNodesFinder);
        nodeFinderMap.put(WorkflowConstant.DXP_TAGS_WORKSPACE, dxpTagsRelatedNodesFinder);
        nodeFinderMap.put(WorkflowConstant.FACETS_WORKSPACE, facetRelatedNodesFinder);
        nodeFinderMap.put(WorkflowConstant.FEATURES_WORKSPACE, featuresRelatedNodesFinder);
        nodeFinderMap.put(WorkflowConstant.MARKETING_CAMPAIGNS_WORKSPACE, marketingCampaignRelatedNodesFinder);
        nodeFinderMap.put(WorkflowConstant.CONCEPTS_WORKSPACE, conceptsRelatedNodesFinder);
        nodeFinderMap.put(WorkflowConstant.FORMS_EMAIL_CONF_WORKSPACE, formEmailConfigRelatedNodesFinder);
        nodeFinderMap.put(WorkflowConstant.FORMS_CONFIGURATION_WORKSPACE, formConfigurationRelatedNodesFinder);
    }

    public List<Node> findRelatedNodesForPublication(final String workspace,
                                                     final boolean isRecursive,
                                                     final String path) {
        final RelatedNodesFinder relatedNodesFinder = nodeFinderMap.get(workspace);
        if (relatedNodesFinder == null) {
            return Collections.emptyList();
        }
        final List<String> idsToProcess = getIdsToProcess(workspace, path, isRecursive);
        final List<Node> rawRelatedNodes = relatedNodesFinder.findRelatedNodesForSeeds(idsToProcess, path);
        final List<Node> dedupedRelatedNodes = dedupeNodes(rawRelatedNodes);
        log.info("Related node discovery summary: workspace={}, path={}, recursive={}, seeds={}, rawRelatedNodes={}, duplicateRelatedNodes={}, finalRelatedNodes={}, finder={}",
                workspace,
                path,
                isRecursive,
                idsToProcess.size(),
                rawRelatedNodes.size(),
                rawRelatedNodes.size() - dedupedRelatedNodes.size(),
                dedupedRelatedNodes.size(),
                relatedNodesFinder.getClass().getSimpleName());
        return dedupedRelatedNodes;
    }

    /**
     * Finds all nodes related to the given seed UUIDs.
     *
     * <p><strong>Batching contract:</strong> the default implementation calls
     * {@link #findRelatedNodes(String)} once per UUID — it is <em>not</em> batched.
     * Subclasses that want to issue batched JCR-SQL2 queries (e.g. by delegating to
     * {@link #getNodeList(String, Collection, String[], String)}) <strong>must override
     * this method</strong>. Subclasses that only need per-UUID logic may instead override
     * {@link #findRelatedNodes(String)} and rely on the default sequential dispatch here.
     *
     * @param nodeUuids de-duplicated, non-blank UUIDs from the publication workset
     * @param path      root path of the current publication (for context/logging only)
     * @return mutable list of related nodes; may contain duplicates across calls
     */
    protected List<Node> findRelatedNodesForSeeds(final List<String> nodeUuids,
                                                  final String path) {
        return nodeUuids.stream()
                .map(this::findRelatedNodes)
                .flatMap(Collection::stream)
                .toList();
    }

    /**
     * Finds nodes related to a single seed UUID. Override this method when per-UUID logic
     * is sufficient. For batched queries override {@link #findRelatedNodesForSeeds} instead.
     *
     * @param nodeUuid a single non-blank UUID
     * @return related nodes; empty list by default
     */
    protected List<Node> findRelatedNodes(final String nodeUuid) {
        return Collections.emptyList();
    }

    List<String> getIdsToProcess(final String workspace,
                                 final String path,
                                 final boolean isRecursive) {
        return WorkflowUtil.collectPublicationWorkset(path, workspace, isRecursive)
                .items()
                .stream()
                .filter(item -> WorkflowUtil.isInCurrentPublicationScope(workspace, item))
                .map(PublicationItem::uuid)
                .filter(StringUtils::isNotBlank)
                .toList();
    }


    String getStringOfValueNoThrow(Value value) {
        try {
            return value.getString();
        } catch (Exception e) {
            return null;
        }
    }

    Node getParentPageNode(Node node) {
        try {
            Node parent = node.getParent();
            if (parent.isNodeType(NodeTypes.Page.NAME)) {
                return parent;
            } else {
                return getParentPageNode(parent);
            }
        } catch (Exception e) {
            log.error("Error while searching for parent page node", e);
            return null;
        }
    }

    List<Node> getParentPageNodeList(List<Node> nodes) {
        return nodes.stream()
                .map(this::getParentPageNode)
                .filter(Objects::nonNull)
                .toList();
    }

    List<Node> getNodeList(String workspace, Collection<String> nodeUuids, String[] returnTypes, String searchedProperty) {
        final List<String> queries = buildQueries(nodeUuids, returnTypes, searchedProperty);
        if (queries.isEmpty()) {
            return Collections.emptyList();
        }
        final List<Node> relatedNodes = new ArrayList<>();
        for (final String query : queries) {
            try {
                final NodeIterator nodeIterator = QueryUtil.search(workspace, query, SQL_LANGUAGE);
                relatedNodes.addAll(NodeUtil.asList(nodeIterator));
            } catch (Exception e) {
                log.error("Error while searching for related nodes", e);
            }
        }
        return relatedNodes;
    }

    List<Node> getNodeList(String workspace, Collection<String> nodeUuids, String returnType, String searchedProperty) {
        String[] returnTypesAsList = {returnType};
        return getNodeList(workspace, nodeUuids, returnTypesAsList, searchedProperty);
    }

    public String buildQuery(Collection<String> nodeUuids, String[] returnTypes, String searchedProperty) {
        final List<String> normalizedNodeUuids = normalizeNodeUuids(nodeUuids);
        if (normalizedNodeUuids.isEmpty()) {
            return EMPTY;
        }
        final String returnTypesString = formatReturnTypes(returnTypes);
        final String whereClause = normalizedNodeUuids.stream()
                .map(nodeUuid -> formatContainsClause(searchedProperty, nodeUuid))
                .collect(Collectors.joining(" OR "));
        return String.format("SELECT %s FROM [nt:base] WHERE %s", returnTypesString, whereClause);
    }

    private String formatReturnTypes(final String[] returnTypes) {
        return Arrays.stream(returnTypes)
                .map(type -> "[" + type + "]")
                .collect(Collectors.joining(", "));
    }

    private String formatContainsClause(final String searchedProperty, final String nodeUuid) {
        return String.format("CONTAINS ('%s','%s')", searchedProperty, nodeUuid);
    }

    List<String> buildQueries(final Collection<String> nodeUuids, final String[] returnTypes, final String searchedProperty) {
        final List<String> normalizedNodeUuids = normalizeNodeUuids(nodeUuids);
        if (normalizedNodeUuids.isEmpty()) {
            return Collections.emptyList();
        }
        final List<String> queries = new ArrayList<>();
        for (int start = 0; start < normalizedNodeUuids.size(); start += WorkflowExecutionSettings.RELATED_NODE_QUERY_BATCH_SIZE) {
            final int end = Math.min(start + WorkflowExecutionSettings.RELATED_NODE_QUERY_BATCH_SIZE, normalizedNodeUuids.size());
            queries.add(buildQuery(normalizedNodeUuids.subList(start, end), returnTypes, searchedProperty));
        }
        return queries;
    }

    protected int getQueryBatchCount(final Collection<String> nodeUuids) {
        final List<String> normalizedNodeUuids = normalizeNodeUuids(nodeUuids);
        if (normalizedNodeUuids.isEmpty()) {
            return 0;
        }
        return (normalizedNodeUuids.size() + WorkflowExecutionSettings.RELATED_NODE_QUERY_BATCH_SIZE - 1)
                / WorkflowExecutionSettings.RELATED_NODE_QUERY_BATCH_SIZE;
    }

    private List<Node> dedupeNodes(final List<Node> nodes) {
        final Map<String, Node> dedupedNodes = new LinkedHashMap<>();
        nodes.stream()
                .filter(Objects::nonNull)
                .forEach(node -> dedupedNodes.putIfAbsent(buildStableNodeKey(node), node));
        return new ArrayList<>(dedupedNodes.values());
    }

    private String buildStableNodeKey(final Node node) {
        try {
            return node.getSession().getWorkspace().getName() + "|" + node.getIdentifier();
        } catch (Exception e) {
            final String path = info.magnolia.jcr.util.NodeUtil.getNodePathIfPossible(node);
            return StringUtils.defaultIfBlank(path, String.valueOf(System.identityHashCode(node)));
        }
    }

    private List<String> normalizeNodeUuids(final Collection<String> nodeUuids) {
        if (nodeUuids == null || nodeUuids.isEmpty()) {
            return Collections.emptyList();
        }
        return nodeUuids.stream()
                .filter(StringUtils::isNotBlank)
                .distinct()
                .toList();
    }

}
