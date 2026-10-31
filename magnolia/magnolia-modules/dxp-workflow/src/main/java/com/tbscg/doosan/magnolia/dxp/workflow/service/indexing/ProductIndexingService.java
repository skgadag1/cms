package com.tbscg.doosan.magnolia.dxp.workflow.service.indexing;

import com.tbscg.doosan.magnolia.dxp.core.util.NodeUtil;
import com.tbscg.doosan.magnolia.dxp.workflow.model.AttachmentIndexedItem;
import com.tbscg.doosan.magnolia.dxp.workflow.model.ModelIndexedItem;
import com.tbscg.doosan.magnolia.dxp.workflow.model.ProductIndexedItem;
import com.tbscg.doosan.magnolia.dxp.workflow.model.ddb.DynamoDbItem;
import com.tbscg.doosan.magnolia.dxp.workflow.service.DynamoDBService;
import com.tbscg.doosan.magnolia.dxp.workflow.service.configuration.IndexingServiceConfiguration;
import com.tbscg.doosan.magnolia.dxp.workflow.service.exception.IndexingException;
import com.tbscg.doosan.magnolia.dxp.workflow.util.WorkflowExecutionSettings;
import com.tbscg.doosan.magnolia.dxp.workflow.util.WorkflowUtil;
import com.tbscg.doosan.magnolia.product.catalog.util.ProductCatalogConstants.NodeTypes.Attachment;
import com.tbscg.doosan.magnolia.product.catalog.util.ProductCatalogConstants.PageProperties;
import com.tbscg.doosan.magnolia.regions.service.RegionsService;
import info.magnolia.cms.util.QueryUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import javax.inject.Inject;
import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.RepositoryException;
import javax.jcr.query.InvalidQueryException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import static com.tbscg.doosan.magnolia.dxp.workflow.util.WorkflowUtil.collectPublicationWorkset;
import static com.tbscg.doosan.magnolia.product.catalog.util.ProductCatalogConstants.ATTACHMENTS_WORKSPACE;
import static com.tbscg.doosan.magnolia.product.catalog.util.ProductCatalogConstants.PRODUCTS_WORKSPACE;
import static java.util.stream.Collectors.toMap;
import static org.apache.commons.lang3.StringUtils.EMPTY;
import static org.apache.jackrabbit.ocm.repository.NodeUtil.PATH_SEPARATOR;

/**
 * Product Indexing service responsible for storing product indexing events in DynamoDB table.
 */
@Slf4j
public class ProductIndexingService extends BaseModelIndexingService {

    private static final String PRODUCT_KEY_PREFIX = "bobcat#product#";
    private static final String UUID_PLACEHOLDER = "${UUID}";
    private static final String SQL_LANGUAGE = "JCR-SQL2";
    private static final String RELATED_ATTACHMENT_QUERY_PREFIX = "SELECT [" + Attachment.NAME + "] FROM [nt:base] WHERE ";
    private static final String RELATED_ATTACHMENT_CONTAINS_CLAUSE =
            "CONTAINS ('" + Attachment.Properties.PRODUCT + "', '" + UUID_PLACEHOLDER + "')";

    private final AttachmentIndexingService attachmentIndexingService;

    @Inject
    public ProductIndexingService(final IndexingServiceConfiguration indexingServiceConfig,
                                  final RegionsService regionsService,
                                  final DynamoDBService dynamoDBService,
                                  final AttachmentIndexingService attachmentIndexingService) throws IndexingException {
        super(indexingServiceConfig, regionsService, dynamoDBService);
        this.attachmentIndexingService = attachmentIndexingService;
    }

    @Override
    public String getWorkspace() {
        return PRODUCTS_WORKSPACE;
    }

    @Override
    protected String getKeyPrefix() {
        return PRODUCT_KEY_PREFIX;
    }

    @Override
    protected String getNodeType() {
        return PRODUCT_NODE_TYPE;
    }

    @Override
    protected String getGroupType() {
        return PRODUCT_GROUP_NODE_TYPE;
    }

    @Override
    protected String getPageModelProperty() {
        return PageProperties.PRODUCT;
    }

    @Override
    protected ModelIndexedItem getEmptyModelIndexedItem() {
        return new ProductIndexedItem("N/A", "N/A", "N/A", 0, null, null, null, null);
    }

    @Override
    protected ModelIndexedItem convertToModelIndexedItem(final Node node) {
        return convertToProductModelIndexedItem(node);
    }

    @Override
    protected List<DynamoDbItem> getRelatedItemsForStore(final List<String> storedUuids) {
        return getRelatedAttachmentsToUpdate(storedUuids, false);
    }

    @Override
    protected List<DynamoDbItem> collectItemsForStore(final String path,
                                                      final String publishTime,
                                                      final boolean isRecursive) {
        final boolean shouldIndexChildProductsOnly = !isRecursive && getRootNodeType(path)
                .filter(this::isCategoryOrSubCategory)
                .isPresent();
        final List<ModelIndexedItem> indexedItems = shouldIndexChildProductsOnly
                ? new ArrayList<>()
                : new ArrayList<>(prepareIndexEvents(path, isRecursive));
        if (shouldIndexChildProductsOnly) {
            indexedItems.addAll(prepareChildProductIndexEvents(path));
        }
        return collectItemsForStore(indexedItems, publishTime);
    }

    @Override
    protected List<DynamoDbItem> getRelatedItemsForDeletion(final List<String> deletedUuids) {
        return getRelatedAttachmentsToUpdate(deletedUuids, true);
    }

    private Optional<String> getRootNodeType(final String path) {
        return collectPublicationWorkset(path, getWorkspace(), false).items().stream()
                .findFirst()
                .map(com.tbscg.doosan.magnolia.dxp.workflow.model.PublicationItem::nodeType);
    }

    private boolean isCategoryOrSubCategory(final String nodeType) {
        return CATEGORY_NODE_TYPE.equals(nodeType) || SUB_CATEGORY_NODE_TYPE.equals(nodeType);
    }

    private List<ModelIndexedItem> prepareChildProductIndexEvents(final String path) {
        final List<Node> childProducts = collectPublicationWorkset(path, getWorkspace(), true)
                .resolveNodes(item -> PRODUCT_NODE_TYPE.equals(item.nodeType()))
                .stream()
                .filter(NodeUtil::isActivated)
                .toList();
        log.debug("Child product reindex scope: path={}, childProducts={}", path, childProducts.size());
        return prepareIndexEvents(childProducts);
    }

    private List<DynamoDbItem> getRelatedAttachmentsToUpdate(final List<String> productUuids, final boolean isDelete) {
        return new ArrayList<>(collectRelatedAttachments(productUuids).stream()
                .filter(NodeUtil::isActivated)
                .filter(attachmentNode -> !PATH_SEPARATOR.equals(NodeUtil.getPath(attachmentNode)))
                .map(attachmentNode ->
                        attachmentIndexingService.prepareIndexEvents(NodeUtil.getPath(attachmentNode), false).stream()
                                .map(item -> {
                                    if (isDelete && item instanceof AttachmentIndexedItem attachmentItem) {
                                        return new AttachmentIndexedItem(item.getUuid(),
                                                item.getPath(),
                                                item.getLastModified(),
                                                item.getLinks(),
                                                filterDeletedProducts(attachmentItem, productUuids),
                                                attachmentItem.getRelatedAttachments(),
                                                attachmentItem.getCategory(),
                                                attachmentItem.getSubCategory(),
                                                attachmentItem.getAttachmentType());
                                    }
                                    return item;
                                })
                                .map(item -> attachmentIndexingService.createDynamoDBItem(item, WorkflowUtil.getVersionTime(attachmentNode)))
                                .collect(Collectors.toList()))
                .flatMap(Collection::stream)
                .collect(toMap(attachment ->
                                attachment.getString("pk") + attachment.getString("sk"),
                        item -> item,
                        (left, right) -> left, LinkedHashMap::new))
                .values());
    }

    private List<ModelIndexedItem> filterDeletedProducts(final AttachmentIndexedItem attachmentItem,
                                                         final List<String> deletedProductUuids) {
        if (attachmentItem.getProducts() == null) {
            return null;
        }
        return attachmentItem.getProducts().stream()
                .filter(product -> !deletedProductUuids.contains(product.getUuid()))
                .toList();
    }

    private List<Node> collectRelatedAttachments(final Collection<String> productUuids) {
        try {
            final List<String> statements = buildRelatedAttachmentQueries(productUuids);
            if (statements.isEmpty()) {
                return List.of();
            }
            final Map<String, Node> relatedAttachments = new LinkedHashMap<>();
            for (final String statement : statements) {
                final NodeIterator queryResult = getQueryResult(statement);
                if (queryResult == null) {
                    continue;
                }
                NodeUtil.asList(queryResult).forEach(node -> relatedAttachments.putIfAbsent(buildRelatedAttachmentNodeKey(node), node));
            }
            return new ArrayList<>(relatedAttachments.values());
        } catch (Exception e) {
            log.error("Error while fetching related items : {}", e.getMessage(), e);
        }
        return List.of();
    }

    List<String> buildRelatedAttachmentQueries(final Collection<String> productUuids) {
        final List<String> normalizedProductUuids = productUuids == null
                ? List.of()
                : productUuids.stream()
                .filter(StringUtils::isNotBlank)
                .distinct()
                .toList();
        if (normalizedProductUuids.isEmpty()) {
            return List.of();
        }
        final List<String> queries = new ArrayList<>();
        for (int start = 0; start < normalizedProductUuids.size(); start += WorkflowExecutionSettings.RELATED_ATTACHMENT_QUERY_BATCH_SIZE) {
            final int end = Math.min(start + WorkflowExecutionSettings.RELATED_ATTACHMENT_QUERY_BATCH_SIZE, normalizedProductUuids.size());
            queries.add(buildRelatedAttachmentQuery(normalizedProductUuids.subList(start, end)));
        }
        return queries;
    }

    private String buildRelatedAttachmentQuery(final List<String> productUuids) {
        final String whereClause = productUuids.stream()
                .filter(StringUtils::isNotBlank)
                .map(uuid -> StringUtils.replace(RELATED_ATTACHMENT_CONTAINS_CLAUSE, UUID_PLACEHOLDER, uuid))
                .collect(Collectors.joining(" OR "));
        return StringUtils.isBlank(whereClause)
                ? EMPTY
                : RELATED_ATTACHMENT_QUERY_PREFIX + whereClause;
    }

    private String buildRelatedAttachmentNodeKey(final Node node) {
        final String identifier = info.magnolia.jcr.util.NodeUtil.getNodeIdentifierIfPossible(node);
        return StringUtils.isNotBlank(identifier)
                ? identifier
                : info.magnolia.jcr.util.NodeUtil.getNodePathIfPossible(node);
    }

    private NodeIterator getQueryResult(final String statement) {
        if (StringUtils.isBlank(statement)) {
            log.warn("Skipping attachment query: statement is blank");
            return null;
        }
        NodeIterator result = null;
        try {
            log.debug("Executing query: {}", statement);
            result = QueryUtil.search(ATTACHMENTS_WORKSPACE, statement, SQL_LANGUAGE, Attachment.NAME);
        } catch (InvalidQueryException e) {
            log.error("Error while querying items: {}", e.getMessage(), e);
        } catch (RepositoryException e) {
            log.error("Repository error: {}", e.getMessage(), e);
        }
        return result;
    }
}
