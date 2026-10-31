package com.tbscg.doosan.magnolia.dxp.workflow.service.indexing;

import com.tbscg.doosan.magnolia.dxp.workflow.model.AttachmentIndexedItem;
import com.tbscg.doosan.magnolia.dxp.workflow.model.AttachmentType;
import com.tbscg.doosan.magnolia.dxp.workflow.model.ModelIndexedItem;
import com.tbscg.doosan.magnolia.dxp.workflow.model.NodeDetails;
import com.tbscg.doosan.magnolia.dxp.workflow.model.ddb.DynamoDbItem;
import com.tbscg.doosan.magnolia.dxp.workflow.service.DynamoDBService;
import com.tbscg.doosan.magnolia.dxp.workflow.service.configuration.IndexingServiceConfiguration;
import com.tbscg.doosan.magnolia.dxp.workflow.service.exception.IndexingException;
import com.tbscg.doosan.magnolia.dxp.workflow.util.IndexedItemUtil;
import com.tbscg.doosan.magnolia.dxp.workflow.util.ModelPageAssociationUtil;
import com.tbscg.doosan.magnolia.dxp.workflow.util.WorkflowUtil;
import com.tbscg.doosan.magnolia.product.catalog.util.ProductCatalogConstants.NodeTypes.Attachment;
import com.tbscg.doosan.magnolia.product.catalog.util.ProductCatalogConstants.NodeTypes.AttachmentCategory;
import com.tbscg.doosan.magnolia.product.catalog.util.ProductCatalogConstants.NodeTypes.AttachmentGroup;
import com.tbscg.doosan.magnolia.product.catalog.util.ProductCatalogConstants.NodeTypes.Category;
import com.tbscg.doosan.magnolia.product.catalog.util.ProductCatalogConstants.NodeTypes.Product;
import com.tbscg.doosan.magnolia.product.catalog.util.ProductCatalogConstants.NodeTypes.ProductGroup;
import com.tbscg.doosan.magnolia.product.catalog.util.ProductCatalogConstants.NodeTypes.SubCategory;
import com.tbscg.doosan.magnolia.product.catalog.util.ProductCatalogConstants.PageProperties;
import com.tbscg.doosan.magnolia.product.catalog.util.ProductCatalogConstants.RelatedPage;
import com.tbscg.doosan.magnolia.regions.service.RegionsService;
import info.magnolia.cms.util.QueryUtil;
import info.magnolia.context.MgnlContext;
import info.magnolia.jcr.predicate.NodeTypePredicate;
import info.magnolia.jcr.util.NodeUtil;
import info.magnolia.jcr.util.PropertyUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.jackrabbit.JcrConstants;

import javax.inject.Inject;
import javax.jcr.ItemNotFoundException;
import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.PathNotFoundException;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.query.InvalidQueryException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import static com.tbscg.doosan.magnolia.dxp.core.util.NodeUtil.asList;
import static com.tbscg.doosan.magnolia.dxp.core.util.NodeUtil.getLastModifiedDate;
import static com.tbscg.doosan.magnolia.dxp.core.util.NodeUtil.getPath;
import static com.tbscg.doosan.magnolia.dxp.core.util.NodeUtil.getPrimaryNodeType;
import static com.tbscg.doosan.magnolia.product.catalog.util.ProductCatalogConstants.ATTACHMENTS_WORKSPACE;
import static com.tbscg.doosan.magnolia.product.catalog.util.ProductCatalogConstants.PRODUCTS_WORKSPACE;
import static java.util.stream.Collectors.toMap;
import static org.apache.jackrabbit.JcrConstants.JCR_UUID;
import static org.apache.jackrabbit.ocm.repository.NodeUtil.PATH_SEPARATOR;

/**
 * Attachment Indexing service responsible for storing attachment indexing events in DynamoDB table.
 */
@Slf4j
public class AttachmentIndexingService extends BaseModelIndexingService {

    private static final String ATTACHMENT_KEY_PREFIX = "bobcat#attachment#";
    private static final Set<String> RELATED_PRODUCT_NODE_TYPES = Set.of(
            Product.NAME, ProductGroup.NAME, SubCategory.NAME, Category.NAME
    );
    private static final Set<String> RELATED_ATTACHMENT_NODE_TYPES = Set.of(
            Attachment.NAME, AttachmentGroup.NAME, AttachmentCategory.NAME
    );

    private static final String UUID = "${UUID}";
    private static final String SQL2_QUERY_STATEMENT =
            "SELECT * FROM [nt:base] WHERE CONTAINS ('" + Attachment.Properties.ATTACHMENT + "', '" + UUID + "')";

    @Inject
    public AttachmentIndexingService(final IndexingServiceConfiguration indexingServiceConfig,
                                     final RegionsService regionsService,
                                     final DynamoDBService dynamoDBService) throws IndexingException {
        super(indexingServiceConfig, regionsService, dynamoDBService);
    }

    @Override
    public String getWorkspace() {
        return ATTACHMENTS_WORKSPACE;
    }

    @Override
    public boolean index(final String action,
                         final String path,
                         final String pageUuid,
                         final String publishTime,
                         final boolean isRecursive) throws IndexingException {
        super.index(action, path, pageUuid, publishTime, isRecursive);
        try {
            final String attachmentGroupPath = WorkflowUtil.getAttachmentGroupPath(path);
            if (attachmentGroupPath != null) {
                storeIndexEvents(attachmentGroupPath, publishTime, false);
            }
        } catch (RepositoryException e) {
            log.error("Cannot index attachment for path {}", path);
            throw new IndexingException("Cannot index attachment for path " + path, e);
        }
        return true;
    }

    @Override
    protected List<ModelIndexedItem> prepareIndexEvents(final String path, final boolean isRecursive) {
        final List<Node> nodes = WorkflowUtil.applyCurrentPublicationScope(
                getWorkspace(),
                WorkflowUtil.collectNodesForPublish(path, getWorkspace(), isRecursive));
        return nodes.stream()
                .filter(node -> ModelPageAssociationUtil.isModelType(node, this.getNodeType())
                        || ModelPageAssociationUtil.isGroupType(node, this.getGroupType())
                        || isAttachmentCategory(node))
                .map(this::convertToModelIndexedItem)
                .filter(modelIndexedItem -> !("N/A").equals(modelIndexedItem.getUuid()))
                .collect(Collectors.toList());
    }

    @Override
    protected ModelIndexedItem convertToModelIndexedItem(final Node node) {
        try {
            final List<Node> modelNodes = resolveProductNodes(node);
            final List<ModelIndexedItem> models = modelNodes.stream()
                    .flatMap(productNode -> this.convertToProductModelIndexedItem(productNode, true).stream())
                    .filter(model -> !("N/A").equals(model.getUuid()))
                    .collect(Collectors.toList());

            final List<Node> relatedAttachmentNodes = getRelatedAttachmentNodes(node);
            final List<ModelIndexedItem> relatedAttachmentItems = relatedAttachmentNodes.stream()
                    .flatMap(attachmentNode -> this.convertToAttachmentModelIndexedItem(attachmentNode).stream())
                    .filter(relatedAttachment -> !("N/A").equals(relatedAttachment.getUuid()))
                    .collect(Collectors.toList());

            return buildAttachmentIndexedItem(node,
                    models.isEmpty() ? null : models,
                    relatedAttachmentItems.isEmpty() ? null : relatedAttachmentItems);
        } catch (RepositoryException e) {
            log.error("Cannot create IndexEvent for node {}", NodeUtil.getNodePathIfPossible(node));
            return getEmptyModelIndexedItem();
        }
    }

    private List<Node> getProductNodes(final Node node) throws RepositoryException {
        final List<Node> products = new ArrayList<>();
        final List<String> productIds = new ArrayList<>();
        final Node productsNode;
        try {
            productsNode = node.getNode(Attachment.Properties.PRODUCTS);
        } catch (PathNotFoundException e) {
            return Collections.emptyList();
        }
        final NodeIterator nodeIterator = productsNode.getNodes();
        while (nodeIterator.hasNext()) {
            Optional.ofNullable(PropertyUtil.getString(nodeIterator.nextNode(), Attachment.Properties.PRODUCT))
                    .ifPresent(productIds::add);
        }
        if (!productIds.isEmpty()) {
            final Session session = MgnlContext.getJCRSession(PRODUCTS_WORKSPACE);
            for (final String id : productIds) {
                try {
                    final Node productNode = session.getNodeByIdentifier(id);
                    products.add(productNode);
                } catch (ItemNotFoundException e) {
                    log.warn("Cannot find product with id {}", id);
                }
            }
        }
        return products.stream()
                .distinct()
                .filter(product -> RELATED_PRODUCT_NODE_TYPES.contains(getPrimaryNodeType(product)))
                .sorted(IndexedItemUtil.nodesTypeComparator.reversed())
                .reduce(new ArrayList<>(), IndexedItemUtil.nodesAccumulator, IndexedItemUtil.nodeCombiner);
    }

    private List<Node> getProductNodesFromGroupMembers(final Node node) {
        try {
            final List<Node> products = new ArrayList<>();
            final List<Node> groupMembers = NodeUtil.asList(NodeUtil.collectAllChildren(node, new NodeTypePredicate(Attachment.NAME)));
            for (final Node member : groupMembers) {
                products.addAll(getProductNodes(member));
            }
            return products.stream()
                    .filter(IndexedItemUtil.distinctByKey(com.tbscg.doosan.magnolia.dxp.core.util.NodeUtil::getPath))
                    .toList();
        } catch (RepositoryException error) {
            log.warn("Error while trying to get product nodes from attachment group members", error);
            return Collections.emptyList();
        }
    }

    private List<Node> getRelatedAttachmentNodes(final Node node) throws RepositoryException {
        final List<Node> relatedAttachments = new ArrayList<>();
        final List<String> relatedAttachmentIds = new ArrayList<>();
        final Node relatedAttachmentsNode;
        try {
            relatedAttachmentsNode = node.getNode(Attachment.Properties.RELATED_ATTACHMENTS);
        } catch (PathNotFoundException e) {
            return Collections.emptyList();
        }
        final NodeIterator nodeIterator = relatedAttachmentsNode.getNodes();
        while (nodeIterator.hasNext()) {
            Optional.ofNullable(PropertyUtil.getString(nodeIterator.nextNode(), Attachment.Properties.ATTACHMENT))
                    .ifPresent(relatedAttachmentIds::add);
        }
        if (!relatedAttachmentIds.isEmpty()) {
            final Session session = MgnlContext.getJCRSession(ATTACHMENTS_WORKSPACE);
            for (final String id : relatedAttachmentIds) {
                try {
                    final Node attachmentNode = session.getNodeByIdentifier(id);
                    relatedAttachments.add(attachmentNode);
                } catch (ItemNotFoundException e) {
                    log.warn("Cannot find attachment with id {}", id);
                }
            }
        }
        return relatedAttachments.stream()
                .distinct()
                .filter(attachment -> RELATED_ATTACHMENT_NODE_TYPES.contains(getPrimaryNodeType(attachment)))
                .sorted(IndexedItemUtil.nodesTypeComparator.reversed())
                .reduce(new ArrayList<>(), IndexedItemUtil.nodesAccumulator, IndexedItemUtil.nodeCombiner);
    }

    private List<ModelIndexedItem> convertToAttachmentModelIndexedItem(final Node node) {
        try {
            return List.of(buildAttachmentIndexedItem(node, null, null));
        } catch (RepositoryException e) {
            log.error("Cannot create IndexEvent for attachment node {}", NodeUtil.getNodePathIfPossible(node));
            return Collections.emptyList();
        }
    }

    @Override
    protected String getNodeType() {
        return Attachment.NAME;
    }

    @Override
    protected String getGroupType() {
        return AttachmentGroup.NAME;
    }

    @Override
    protected String getCategoryType() {
        return AttachmentCategory.NAME;
    }

    @Override
    protected String getPageModelProperty() {
        return PageProperties.ATTACHMENT;
    }

    @Override
    protected ModelIndexedItem getEmptyModelIndexedItem() {
        return new AttachmentIndexedItem("N/A", "N/A", 0, null, null, null, null, null, null);
    }

    @Override
    protected String getKeyPrefix() {
        return ATTACHMENT_KEY_PREFIX;
    }

    private boolean isAttachmentGroup(final Node node) {
        return AttachmentGroup.NAME.equals(getPrimaryNodeType(node));
    }

    private boolean isAttachmentCategory(final Node node) {
        return AttachmentCategory.NAME.equals(getPrimaryNodeType(node));
    }

    /**
     * Walks the node hierarchy to resolve group/category hierarchy for the given attachment node.
     */
    private AttachmentHierarchy resolveAttachmentHierarchy(final Node node, Node parent) throws RepositoryException {
        return switch (getPrimaryNodeType(node)) {
            case AttachmentCategory.NAME -> new AttachmentHierarchy(null, IndexedItemUtil.getNodeDetails(node));
            case AttachmentGroup.NAME -> new AttachmentHierarchy(
                    IndexedItemUtil.getNodeDetails(node),
                    isAttachmentCategory(parent) ? IndexedItemUtil.getNodeDetails(parent) : null);
            default -> {
                NodeDetails groupDetails = null;
                if (isAttachmentGroup(parent)) {
                    groupDetails = IndexedItemUtil.getNodeDetails(parent);
                    parent = parent.getParent();
                }
                final NodeDetails categoryDetails = isAttachmentCategory(parent)
                        ? IndexedItemUtil.getNodeDetails(parent)
                        : null;
                yield new AttachmentHierarchy(groupDetails, categoryDetails);
            }
        };
    }

    /**
     * Resolves the product nodes associated with the given attachment node, based on its type.
     */
    private List<Node> resolveProductNodes(final Node node) throws RepositoryException {
        if (isAttachmentCategory(node)) {
            return Collections.emptyList();
        }
        if (isAttachmentGroup(node)) {
            return getProductNodesFromGroupMembers(node);
        }
        return getProductNodes(node);
    }

    /**
     * Builds an AttachmentIndexedItem from a node, extracting common fields and attaching the given products and related attachments.
     */
    private AttachmentIndexedItem buildAttachmentIndexedItem(final Node node,
                                                             final List<ModelIndexedItem> products,
                                                             final List<ModelIndexedItem> relatedAttachments) throws RepositoryException {
        final String uuid = PropertyUtil.getString(node, JCR_UUID);
        final String path = NodeUtil.getNodePathIfPossible(node);
        final long lastModified = getLastModifiedDate(node).getTimeInMillis();
        Map<String, String> urls = null;
        if (node.hasNode(RelatedPage.NODE_NAME)) {
            urls = ModelPageAssociationUtil.extractUrls(node);
        }
        final AttachmentHierarchy hierarchy = resolveAttachmentHierarchy(node, node.getParent());
        final AttachmentType attachmentType = resolveAttachmentType(node);
        return new AttachmentIndexedItem(uuid, path, lastModified, urls, products, relatedAttachments,
                hierarchy.categoryDetails(), hierarchy.groupDetails(), attachmentType);
    }

    private AttachmentType resolveAttachmentType(final Node node) {
        return switch (getPrimaryNodeType(node)) {
            case AttachmentCategory.NAME -> AttachmentType.CATEGORY;
            case AttachmentGroup.NAME -> AttachmentType.GROUP;
            default -> AttachmentType.ATTACHMENT;
        };
    }

    private record AttachmentHierarchy(NodeDetails groupDetails, NodeDetails categoryDetails) {
    }

    @Override
    protected List<DynamoDbItem> getRelatedItemsForStore(final List<String> storedUuids) {
        return getRelatedAttachmentsToUpdate(storedUuids, false);
    }

    @Override
    protected List<DynamoDbItem> getRelatedItemsForDeletion(final List<String> deletedUuids) {
        return getRelatedAttachmentsToUpdate(deletedUuids, true);
    }

    private List<DynamoDbItem> getRelatedAttachmentsToUpdate(final List<String> attachmentUuids, final boolean isDelete) {
        return new ArrayList<>(attachmentUuids.stream()
                .map(this::collectRelatedAttachments)
                .flatMap(Collection::stream)
                .filter(com.tbscg.doosan.magnolia.dxp.core.util.NodeUtil::isActivated)
                .filter(attachmentNode -> !PATH_SEPARATOR.equals(getPath(attachmentNode)))
                .filter(attachmentNode -> !attachmentUuids.contains(NodeUtil.getNodeIdentifierIfPossible(attachmentNode)))
                .map(attachmentNode ->
                        prepareIndexEvents(getPath(attachmentNode), false).stream()
                                .map(item -> {
                                    if (isDelete && item instanceof AttachmentIndexedItem attachmentItem) {
                                        final List<ModelIndexedItem> filteredRelatedAttachments = attachmentItem.getRelatedAttachments() != null
                                                ? attachmentItem.getRelatedAttachments().stream()
                                                .filter(attachment -> !attachmentUuids.contains(attachment.getUuid()))
                                                .collect(Collectors.toList())
                                                : null;
                                        return new AttachmentIndexedItem(item.getUuid(),
                                                item.getPath(),
                                                item.getLastModified(),
                                                item.getLinks(),
                                                attachmentItem.getProducts(),
                                                filteredRelatedAttachments,
                                                attachmentItem.getCategory(),
                                                attachmentItem.getSubCategory(),
                                                attachmentItem.getAttachmentType());
                                    }
                                    return item;
                                })
                                .map(item -> createDynamoDBItem(item, WorkflowUtil.getVersionTime(attachmentNode)))
                                .collect(Collectors.toList()))
                .flatMap(Collection::stream)
                .collect(toMap(attachment ->
                                attachment.getString("pk") + attachment.getString("sk"),
                        item -> item,
                        (left, right) -> left, LinkedHashMap::new))
                .values());
    }

    private List<Node> collectRelatedAttachments(final String uuid) {
        try {
            final String statement = StringUtils.replace(SQL2_QUERY_STATEMENT, UUID, uuid);
            final NodeIterator iterator = getQueryResult(statement);
            return iterator != null ? asList(iterator) : Collections.emptyList();
        } catch (Exception e) {
            log.error("Error while fetching related attachments : {}", e.getMessage(), e);
        }
        return Collections.emptyList();
    }

    private NodeIterator getQueryResult(final String statement) {
        NodeIterator result = null;
        try {
            log.debug("Executing query: {}", statement);
            result = QueryUtil.search(ATTACHMENTS_WORKSPACE, statement, "JCR-SQL2", JcrConstants.NT_BASE);
        } catch (InvalidQueryException e) {
            log.error("Error while querying items: {}", e.getMessage(), e);
        } catch (RepositoryException e) {
            log.error("Repository error: {}", e.getMessage(), e);
        }
        return result;
    }
}
