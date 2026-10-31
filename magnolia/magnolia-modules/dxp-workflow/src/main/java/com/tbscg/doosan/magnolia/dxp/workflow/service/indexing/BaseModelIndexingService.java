package com.tbscg.doosan.magnolia.dxp.workflow.service.indexing;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.tbscg.doosan.magnolia.dxp.core.SitePagePath;
import com.tbscg.doosan.magnolia.dxp.core.util.TracingUtil;
import com.tbscg.doosan.magnolia.dxp.workflow.model.ModelIndexedItem;
import com.tbscg.doosan.magnolia.dxp.workflow.model.ddb.DynamoDbItem;
import com.tbscg.doosan.magnolia.dxp.workflow.model.ddb.PrimaryKey;
import com.tbscg.doosan.magnolia.dxp.workflow.service.DynamoDBService;
import com.tbscg.doosan.magnolia.dxp.workflow.service.configuration.IndexingServiceConfiguration;
import com.tbscg.doosan.magnolia.dxp.workflow.service.exception.IndexingException;
import com.tbscg.doosan.magnolia.dxp.workflow.util.IndexedItemUtil;
import com.tbscg.doosan.magnolia.dxp.workflow.util.ModelPageAssociationUtil;
import com.tbscg.doosan.magnolia.dxp.workflow.util.WorkflowUtil;
import com.tbscg.doosan.magnolia.product.catalog.util.ProductCatalogConstants.NodeTypes.Category;
import com.tbscg.doosan.magnolia.product.catalog.util.ProductCatalogConstants.NodeTypes.Product;
import com.tbscg.doosan.magnolia.product.catalog.util.ProductCatalogConstants.NodeTypes.ProductGroup;
import com.tbscg.doosan.magnolia.product.catalog.util.ProductCatalogConstants.NodeTypes.SubCategory;
import com.tbscg.doosan.magnolia.product.catalog.util.ProductCatalogConstants.PageProperties;
import com.tbscg.doosan.magnolia.regions.service.RegionsService;
import info.magnolia.jcr.util.NodeUtil;
import info.magnolia.jcr.util.PropertyUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import software.amazon.awssdk.services.dynamodb.model.AttributeAction;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;
import software.amazon.awssdk.services.dynamodb.model.AttributeValueUpdate;
import software.amazon.awssdk.services.dynamodb.model.QueryRequest;
import software.amazon.awssdk.services.dynamodb.model.UpdateItemRequest;

import javax.jcr.Node;
import javax.jcr.RepositoryException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.tbscg.doosan.magnolia.dxp.workflow.service.configuration.IndexingServiceConfiguration.DEINDEX_ACTION;
import static com.tbscg.doosan.magnolia.dxp.workflow.service.configuration.IndexingServiceConfiguration.INDEX_ACTION;
import static com.tbscg.doosan.magnolia.dxp.workflow.service.configuration.IndexingServiceConfiguration.TABLE_DATE_KEY_NAME;
import static com.tbscg.doosan.magnolia.dxp.workflow.service.configuration.IndexingServiceConfiguration.TABLE_PARTITION_KEY_NAME;
import static com.tbscg.doosan.magnolia.dxp.workflow.service.configuration.IndexingServiceConfiguration.TABLE_PAYLOAD_KEY_NAME;
import static com.tbscg.doosan.magnolia.dxp.workflow.service.configuration.IndexingServiceConfiguration.TABLE_REGION_KEY_NAME;
import static com.tbscg.doosan.magnolia.dxp.workflow.service.configuration.IndexingServiceConfiguration.TABLE_SORT_KEY_NAME;
import static com.tbscg.doosan.magnolia.dxp.workflow.service.configuration.IndexingServiceConfiguration.TABLE_URLS_KEY_NAME;
import static com.tbscg.doosan.magnolia.dxp.workflow.util.WorkflowUtil.collectNodesForPublish;
import static com.tbscg.doosan.magnolia.product.catalog.util.ProductCatalogConstants.ATTACHMENTS_WORKSPACE;

/**
 * Base Product Indexing service containing common methods for storing indexing events in DynamoDB table for product catalog items.
 */
@Slf4j
public abstract class BaseModelIndexingService extends BaseIndexingService {

    protected static final String PRODUCT_NODE_TYPE = Product.NAME;
    protected static final String CATEGORY_NODE_TYPE = Category.NAME;
    protected static final String SUB_CATEGORY_NODE_TYPE = SubCategory.NAME;
    protected static final String PRODUCT_GROUP_NODE_TYPE = ProductGroup.NAME;
    private static final ObjectMapper MODEL_ITEM_MAPPER = new ObjectMapper()
            .setSerializationInclusion(JsonInclude.Include.NON_NULL);
    private static final TypeReference<Map<String, Object>> PAYLOAD_TYPE = new TypeReference<>() {
    };

    public BaseModelIndexingService(final IndexingServiceConfiguration indexingServiceConfig,
                                    final RegionsService regionsService,
                                    final DynamoDBService dynamoDBService) throws IndexingException {
        super(indexingServiceConfig, regionsService, dynamoDBService);
    }

    public boolean index(final String action,
                         final String path,
                         final String pageUuid,
                         final String publishTime,
                         final boolean isRecursive) throws IndexingException {
        return index(action, path, publishTime, isRecursive);
    }

    /**
     * Prepares and stores indexing events in DynamoDB table, that are later processed in Lambda functions.
     *
     * @param action      type. Can be INDEX_ACTION or REINDEX
     * @param path        JCR node path.
     * @param publishTime time of publication.
     * @param isRecursive flag saying if recursive publication was used.
     * @return true value if processing was successful, false otherwise.
     * @throws IndexingException if indexing cannot be completed.
     */
    public boolean index(final String action,
                         final String path,
                         final String publishTime,
                         final boolean isRecursive) throws IndexingException {
        return TracingUtil.callWithTracing(traceID -> {
            if (action.equals(INDEX_ACTION)) {
                log.info("Indexing product/attachment for path: {}", path);
                storeIndexEvents(path, publishTime, isRecursive);
            } else if (action.equals(DEINDEX_ACTION)) {
                log.info("De-indexing product/attachment for path: {}", path);
                deleteIndexEvent(path);
            }
            return true;
        });
    }

    /**
     * Updates model node with associated page and friendly urls
     *
     * @param page            node of the page with assigned model
     * @param isDeindexAction flag indicating whether page associations should be removed.
     */
    public void addPageAssociation(final Node page, Boolean isDeindexAction) {
        final String pagePath = com.tbscg.doosan.magnolia.dxp.core.util.NodeUtil.getPath(page);
        log.debug("add page association for page {}", pagePath);

        final String modelUuid = Optional.ofNullable(PropertyUtil.getPropertyOrNull(page, getPageModelProperty()))
                .map(PropertyUtil::getValueString)
                .orElse(null);

        log.debug("add page association for modelUuid {}", modelUuid);

        final Optional<Node> modelNode = com.tbscg.doosan.magnolia.dxp.core.util.NodeUtil.collectDxpNodeByIdentifier(modelUuid, getWorkspace())
                .filter(node -> ModelPageAssociationUtil.isModelType(node, this.getNodeType())
                        || ModelPageAssociationUtil.isGroupType(node, this.getGroupType())
                        || (getCategoryType() != null && ModelPageAssociationUtil.isModelType(node, getCategoryType())));

        final List<Node> modelsToUpdate = modelNode
                .map(node -> new ArrayList<>(this.getModelsToUpdateWithAssociation(page, node, isDeindexAction)))
                .orElseGet(ArrayList::new);
        try {
            ModelPageAssociationUtil.removeDeletedPageAssociation(page, pagePath, modelsToUpdate, this.getWorkspace(), this.getNodeType());
            if (getCategoryType() != null) {
                ModelPageAssociationUtil.removeDeletedPageAssociation(page, pagePath, modelsToUpdate, this.getWorkspace(), getCategoryType());
            }
            final List<Node> publishedModelsToUpdate = modelsToUpdate.stream()
                    .filter(com.tbscg.doosan.magnolia.dxp.core.util.NodeUtil::isActivated)
                    .toList();
            this.updateIndexEvent(publishedModelsToUpdate, page);
            ModelPageAssociationUtil.updateProcessedNodes(page, modelsToUpdate, this.getNodeType(), this.getGroupType());
        } catch (IndexingException | RepositoryException e) {
            log.error("Adding page - model association failed", e);
        }
    }

    private List<Node> getModelsToUpdateWithAssociation(final Node page,
                                                        final Node modelNode,
                                                        final Boolean isDeindexAction) {
        final String modelPath = com.tbscg.doosan.magnolia.dxp.core.util.NodeUtil.getPath(modelNode);
        log.info("Adding associated page {} for model {}", NodeUtil.getNodePathIfPossible(page), modelPath);
        try {
            return Stream.of(
                            this.updatePageWithModelAssociation(page, modelPath),
                            this.updateModelWithPageAssociation(modelNode, page, isDeindexAction))
                    .distinct()
                    .map(path -> com.tbscg.doosan.magnolia.dxp.core.util.NodeUtil.collectDxpNode(path, getWorkspace()))
                    .flatMap(Optional::stream)
                    .toList();
        } catch (IndexingException e) {
            log.error("Error while collecting nodes for page - model association", e);
        }
        return Collections.emptyList();
    }

    private String updatePageWithModelAssociation(final Node page, final String model) {
        String currentModel = model;
        try {
            if (page.hasProperty(PageProperties.CURRENT_MODEL)) {
                currentModel = page.getProperty(PageProperties.CURRENT_MODEL).getString();
                page.getProperty(PageProperties.CURRENT_MODEL).setValue(model);
            } else {
                page.setProperty(PageProperties.CURRENT_MODEL, model);
            }
            if (!currentModel.equals(model)) {
                com.tbscg.doosan.magnolia.dxp.core.util.NodeUtil.collectDxpNode(currentModel, this.getWorkspace())
                        .ifPresent(node -> {
                            if (getCategoryType() != null && ModelPageAssociationUtil.isModelType(node, getCategoryType())) {
                                ModelPageAssociationUtil.removePageRelatedNode(node, page, getCategoryType());
                            } else {
                                ModelPageAssociationUtil.removePageRelatedNode(node, page, this.getNodeType());
                            }
                        });
            }
        } catch (RepositoryException e) {
            log.error("Cannot update page {} with associated model {}", NodeUtil.getNodePathIfPossible(page), model, e);
            throw new IndexingException("Cannot update page with associated model", e);
        }
        return currentModel;
    }

    private String updateModelWithPageAssociation(final Node model,
                                                  final Node page,
                                                  final boolean isDeindexAction) {
        final String modelPath = NodeUtil.getNodePathIfPossible(model);
        final boolean isCategoryModel = getCategoryType() != null && ModelPageAssociationUtil.isModelType(model, getCategoryType());
        if (!isDeindexAction && com.tbscg.doosan.magnolia.dxp.core.util.NodeUtil.isActivated(page)) {
            try {
                final SitePagePath pagePath = new SitePagePath(page.getPath());
                log.debug("Updating model {} with associated page {}", modelPath, pagePath);
                final List<String> languages = this.getLanguages(pagePath.getSite(), pagePath.getRegion());
                final Map<String, String> pathsForLang = languages.stream()
                        .collect(Collectors.toMap(
                                Function.identity(),
                                lang -> this.getFriendlyUrlForLang(page, pagePath, lang)));
                if (isCategoryModel) {
                    ModelPageAssociationUtil.createRelatedPageNode(model, page, pathsForLang, getCategoryType());
                } else {
                    ModelPageAssociationUtil.createRelatedPageNode(model, page, pathsForLang, this.getNodeType());
                    ModelPageAssociationUtil.createRelatedPageNode(model, page, pathsForLang, this.getGroupType());
                }
            } catch (RepositoryException e) {
                log.error("Cannot update model {} with associated page", modelPath, e);
                throw new IndexingException("Cannot update model with associated page", e);
            }
        } else {
            if (isCategoryModel) {
                ModelPageAssociationUtil.removePageRelatedNode(model, page, getCategoryType());
            } else {
                ModelPageAssociationUtil.removePageRelatedNode(model, page, this.getNodeType());
                ModelPageAssociationUtil.removePageRelatedNode(model, page, this.getGroupType());
            }
        }
        return modelPath;
    }

    private void updateIndexEvent(final List<Node> models, final Node page) {
        models.stream()
                .flatMap(model -> {
                    if (getCategoryType() != null && ModelPageAssociationUtil.isModelType(model, getCategoryType())) {
                        return ModelPageAssociationUtil.getNodeCollection(model, getCategoryType(), page).stream();
                    }
                    return ModelPageAssociationUtil.getNodeCollection(model, getNodeType(), page).stream();
                })
                .map(this::mapNodeToPageAssociationUpdateItem)
                .flatMap(Optional::stream)
                .forEach(item -> dynamoDBService.updateItem(indexerTableName, item));
    }

    private Optional<UpdateItemRequest> mapNodeToPageAssociationUpdateItem(final Node node) {
        final String pk = getKeyPrefix() + NodeUtil.getNodeIdentifierIfPossible(node);
        final List<DynamoDbItem> items = getItemsByPk(pk);
        if (items.size() == 1) {
            final DynamoDbItem item = items.get(0);
            final String sk = item.getString(TABLE_SORT_KEY_NAME);
            final Map<String, AttributeValueUpdate> urlsUpdate = Collections.singletonMap(
                    TABLE_URLS_KEY_NAME,
                    AttributeValueUpdate.builder()
                            .action(AttributeAction.PUT)
                            .value(DynamoDbItem.toAttributeValue(ModelPageAssociationUtil.extractUrls(node)))
                            .build());
            return Optional.of(UpdateItemRequest.builder()
                    .key(Map.of(TABLE_PARTITION_KEY_NAME, AttributeValue.fromS(pk), TABLE_SORT_KEY_NAME, AttributeValue.fromS(sk)))
                    .attributeUpdates(urlsUpdate)
                    .build());
        }
        return Optional.empty();
    }

    protected void storeIndexEvents(final String path, final String publishTime, final boolean isRecursive) {
        final List<DynamoDbItem> items = collectItemsForStore(path, publishTime, isRecursive);
        if (CollectionUtils.isNotEmpty(items)) {
            log.info("Model indexing batch summary: workspace={}, path={}, recursive={}, writes={}",
                    getWorkspace(), path, isRecursive, items.size());
            dynamoDBService.batchWriteItem(indexerTableName, items);
        }
    }

    /**
     * Builds the full list of items to store: prepares index events, handles renames,
     * and appends related items supplied by subclasses via {@link #getRelatedItemsForStore}.
     * Subclasses may override to append additional items (e.g. child products).
     */
    protected List<DynamoDbItem> collectItemsForStore(final String path,
                                                      final String publishTime,
                                                      final boolean isRecursive) {
        return collectItemsForStore(prepareIndexEvents(path, isRecursive), publishTime);
    }

    protected List<DynamoDbItem> collectItemsForStore(final List<ModelIndexedItem> indexedItems, final String publishTime) {
        final List<DynamoDbItem> items = indexedItems.stream()
                .map(item -> this.createDynamoDBItem(item, publishTime))
                .peek(item -> {
                    final String pk = item.getString(TABLE_PARTITION_KEY_NAME);
                    final String sk = item.getString(TABLE_SORT_KEY_NAME);
                    deleteIfRenamed(pk, sk);
                })
                .collect(Collectors.toCollection(ArrayList::new));
        final List<String> storedUuids = collectStoredUuids(items);
        try {
            items.addAll(getRelatedItemsForStore(storedUuids));
        } catch (Exception exception) {
            log.warn("Error while reindexing related items of stored item", exception);
        }
        return items;
    }

    protected List<String> collectStoredUuids(final List<DynamoDbItem> items) {
        return items.stream()
                .map(item -> item.getMap(TABLE_PAYLOAD_KEY_NAME))
                .filter(payload -> payload != null && payload.get("uuid") != null)
                .map(payload -> String.valueOf(payload.get("uuid")))
                .collect(Collectors.toList());
    }

    /**
     * Hook for subclasses to supply related items that need to be updated when items are stored.
     * Returns an empty list by default.
     */
    protected List<DynamoDbItem> getRelatedItemsForStore(final List<String> storedUuids) {
        return Collections.emptyList();
    }

    protected void deleteIndexEvent(final String path) {
        final List<ModelIndexedItem> items = prepareIndexEvents(path, true);
        final List<PrimaryKey> primaryKeys = items.stream()
                .map(event -> {
                    final String pk = getKeyPrefix() + event.getUuid();
                    final String sk = event.getPath();
                    return new PrimaryKey(TABLE_PARTITION_KEY_NAME, pk, TABLE_SORT_KEY_NAME, sk);
                })
                .collect(Collectors.toList());
        final List<String> deletedUuids = items.stream()
                .map(ModelIndexedItem::getUuid)
                .collect(Collectors.toList());
        final List<DynamoDbItem> relatedItemsToUpdate;
        try {
            relatedItemsToUpdate = getRelatedItemsForDeletion(deletedUuids);
        } catch (Exception exception) {
            log.warn("Error while reindexing related items of deleted item", exception);
            log.info("Model deindex batch summary: workspace={}, path={}, deletes={}, relatedWrites={}",
                    getWorkspace(), path, primaryKeys.size(), 0);
            dynamoDBService.batchDeleteItem(indexerTableName, primaryKeys);
            return;
        }
        log.info("Model deindex batch summary: workspace={}, path={}, deletes={}, relatedWrites={}",
                getWorkspace(), path, primaryKeys.size(), relatedItemsToUpdate.size());
        dynamoDBService.batchWriteDeleteItems(indexerTableName, relatedItemsToUpdate, primaryKeys);
    }

    /**
     * Hook for subclasses to supply related items that need to be updated when items are deleted.
     * Returns an empty list by default.
     */
    protected List<DynamoDbItem> getRelatedItemsForDeletion(final List<String> deletedUuids) {
        return Collections.emptyList();
    }

    protected List<ModelIndexedItem> prepareIndexEvents(final String path, final boolean isRecursive) {
        return prepareIndexEvents(collectNodesForPublish(path, getWorkspace(), isRecursive));
    }

    protected List<ModelIndexedItem> prepareIndexEvents(final Collection<Node> nodes) {
        final List<Node> nodesToProcess = WorkflowUtil.applyCurrentPublicationScope(getWorkspace(), new ArrayList<>(nodes));
        return nodesToProcess.stream()
                .filter(node -> ModelPageAssociationUtil.isModelType(node, this.getNodeType())
                        || isIndexableAttachmentGroup(node))
                .map(this::convertToModelIndexedItem)
                .filter(modelIndexedItem -> !("N/A").equals(modelIndexedItem.getUuid()))
                .collect(Collectors.toList());
    }

    private boolean isIndexableAttachmentGroup(final Node node) {
        return ATTACHMENTS_WORKSPACE.equals(getWorkspace())
                && ModelPageAssociationUtil.isGroupType(node, this.getGroupType());
    }

    protected DynamoDbItem createDynamoDBItem(final ModelIndexedItem event, final String publishTime) {
        final String pk = getKeyPrefix() + event.getUuid();
        final String sk = event.getPath();
        final String region = event.getPath().split("/")[2];
        final Map<String, Object> payload = MODEL_ITEM_MAPPER.convertValue(event, PAYLOAD_TYPE);
        final Map<String, String> urls = event.getLinks();
        final DynamoDbItem result = new DynamoDbItem().withString(TABLE_PARTITION_KEY_NAME, pk)
                .withString(TABLE_SORT_KEY_NAME, sk)
                .withString(TABLE_REGION_KEY_NAME, region)
                .withMap(TABLE_PAYLOAD_KEY_NAME, payload)
                .withString(TABLE_DATE_KEY_NAME, publishTime);
        if (!MapUtils.isEmpty(urls)) {
            result.withMap(TABLE_URLS_KEY_NAME, urls.entrySet().stream()
                    .collect(Collectors.toMap(Map.Entry::getKey, entry -> (Object) entry.getValue())));
        }
        return result;
    }

    protected void deleteIfRenamed(final String primaryKey, final String secondaryKey) {
        final List<DynamoDbItem> items = getItemsByPk(primaryKey);
        if (items.size() == 1) {
            final DynamoDbItem item = items.get(0);
            final String pk = item.getString(TABLE_PARTITION_KEY_NAME);
            final String sk = item.getString(TABLE_SORT_KEY_NAME);
            if (!sk.equals(secondaryKey)) {
                log.info("Deleting renamed product from index pk: {}, sk: {}", pk, sk);
                final PrimaryKey compoundKey = new PrimaryKey(TABLE_PARTITION_KEY_NAME, pk, TABLE_SORT_KEY_NAME, sk);
                dynamoDBService.deleteItem(indexerTableName, compoundKey);
            }
        } else {
            final List<DynamoDbItem> matchingItems = items.stream()
                    .filter(item -> item.getString(TABLE_SORT_KEY_NAME).equals(secondaryKey))
                    .toList();
            if (items.size() == 2 && matchingItems.size() == 1) {
                final DynamoDbItem toBeDeleted = items.stream()
                        .filter(item -> !item.getString(TABLE_SORT_KEY_NAME).equals(secondaryKey))
                        .findFirst()
                        .orElseThrow();
                final String pk = toBeDeleted.getString(TABLE_PARTITION_KEY_NAME);
                final String sk = toBeDeleted.getString(TABLE_SORT_KEY_NAME);
                log.info("Deleting unexpected renamed product from index pk: {}, sk: {}", pk, sk);
                final PrimaryKey compoundKey = new PrimaryKey(TABLE_PARTITION_KEY_NAME, pk, TABLE_SORT_KEY_NAME, sk);
                dynamoDBService.deleteItem(indexerTableName, compoundKey);
            } else if (items.size() > 1) {
                final String errorMsg = String.format("Wrong number of items defined for pk: %s", primaryKey);
                log.error(errorMsg);
                throw new IndexingException(errorMsg);
            }
        }
    }

    private List<DynamoDbItem> getItemsByPk(final String primaryKey) {
        final QueryRequest querySpec = QueryRequest.builder()
                .keyConditionExpression(String.format("%s = :id", TABLE_PARTITION_KEY_NAME))
                .expressionAttributeValues(Collections.singletonMap(":id", AttributeValue.fromS(primaryKey)))
                .build();
        return dynamoDBService.queryItems(indexerTableName, querySpec);
    }

    protected ModelIndexedItem convertToProductModelIndexedItem(final Node node) {
        return convertToProductModelIndexedItem(node, false).stream()
                .findFirst()
                .orElse(getEmptyModelIndexedItem());
    }

    protected List<ModelIndexedItem> convertToProductModelIndexedItem(final Node node, final Boolean isInnerProduct) {
        final List<ModelIndexedItem> result = new ArrayList<>();
        try {
            final Collection<Node> productNodes = new ArrayList<>();
            final String nodeType = node.getPrimaryNodeType().getName();
            if (PRODUCT_NODE_TYPE.equals(nodeType)) {
                productNodes.add(node);
            } else {
                productNodes.addAll(com.tbscg.doosan.magnolia.dxp.core.util.NodeUtil.getNodes(node, PRODUCT_NODE_TYPE));
            }
            for (final Node productNode : productNodes) {
                result.add(IndexedItemUtil.convertProductToIndexedItem(productNode, isInnerProduct, ModelPageAssociationUtil::extractUrls));
            }
            return result;
        } catch (RepositoryException e) {
            log.error("Cannot create IndexEvent for node {}", NodeUtil.getNodePathIfPossible(node));
            return Collections.singletonList(getEmptyModelIndexedItem());
        }
    }

    protected abstract String getGroupType();

    protected String getCategoryType() {
        return null;
    }

    protected abstract String getKeyPrefix();

    protected abstract String getNodeType();

    protected abstract String getPageModelProperty();

    protected abstract ModelIndexedItem getEmptyModelIndexedItem();

    protected abstract ModelIndexedItem convertToModelIndexedItem(final Node node);
}
