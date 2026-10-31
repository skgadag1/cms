package com.tbscg.doosan.magnolia.dxp.workflow.service.indexing;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.tbscg.doosan.magnolia.dxp.core.SitePagePath;
import com.tbscg.doosan.magnolia.dxp.core.util.NodeUtil;
import com.tbscg.doosan.magnolia.dxp.core.util.TracingUtil;
import com.tbscg.doosan.magnolia.dxp.workflow.model.PageIndexedItem;
import com.tbscg.doosan.magnolia.dxp.workflow.model.PageNode;
import com.tbscg.doosan.magnolia.dxp.workflow.model.ddb.DynamoDbItem;
import com.tbscg.doosan.magnolia.dxp.workflow.model.ddb.PrimaryKey;
import com.tbscg.doosan.magnolia.dxp.workflow.service.DynamoDBService;
import com.tbscg.doosan.magnolia.dxp.workflow.service.configuration.IndexingServiceConfiguration;
import com.tbscg.doosan.magnolia.dxp.workflow.service.exception.IndexingException;
import com.tbscg.doosan.magnolia.regions.service.RegionsService;
import info.magnolia.ui.api.message.Message;
import info.magnolia.ui.api.message.MessageType;
import info.magnolia.ui.framework.message.MessagesManager;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;
import software.amazon.awssdk.services.dynamodb.model.QueryRequest;

import javax.inject.Inject;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import static com.tbscg.doosan.magnolia.dxp.workflow.constant.WorkflowConstant.WEBSITE_WORKSPACE;
import static com.tbscg.doosan.magnolia.dxp.workflow.service.configuration.IndexingServiceConfiguration.DEINDEX_ACTION;
import static com.tbscg.doosan.magnolia.dxp.workflow.service.configuration.IndexingServiceConfiguration.INDEX_ACTION;
import static com.tbscg.doosan.magnolia.dxp.workflow.service.configuration.IndexingServiceConfiguration.TABLE_DATE_KEY_NAME;
import static com.tbscg.doosan.magnolia.dxp.workflow.service.configuration.IndexingServiceConfiguration.TABLE_LANG_KEY_NAME;
import static com.tbscg.doosan.magnolia.dxp.workflow.service.configuration.IndexingServiceConfiguration.TABLE_PARTITION_KEY_NAME;
import static com.tbscg.doosan.magnolia.dxp.workflow.service.configuration.IndexingServiceConfiguration.TABLE_PAYLOAD_KEY_NAME;
import static com.tbscg.doosan.magnolia.dxp.workflow.service.configuration.IndexingServiceConfiguration.TABLE_PK_PAGE_TYPE;
import static com.tbscg.doosan.magnolia.dxp.workflow.service.configuration.IndexingServiceConfiguration.TABLE_REGION_KEY_NAME;
import static com.tbscg.doosan.magnolia.dxp.workflow.service.configuration.IndexingServiceConfiguration.TABLE_SORT_KEY_NAME;
import static org.apache.commons.lang3.StringUtils.EMPTY;

/**
 * Page Indexing service responsible for storing page indexing events in DynamoDB table.
 */
@Slf4j
public class PageIndexingService extends BasePageIndexingService {

    private static final ObjectMapper INDEX_ITEM_MAPPER = new ObjectMapper()
            .setSerializationInclusion(JsonInclude.Include.NON_NULL);
    private static final TypeReference<Map<String, Object>> PAYLOAD_TYPE = new TypeReference<>() {
    };

    private final ProductIndexingService productIndexingService;
    private final AttachmentIndexingService attachmentIndexingService;
    private final MessagesManager messagesManager;

    @Inject
    public PageIndexingService(final IndexingServiceConfiguration indexingServiceConfig,
                               final RegionsService regionsService,
                               final DynamoDBService dynamoDBService,
                               final ProductIndexingService productIndexingService,
                               final AttachmentIndexingService attachmentIndexingService,
                               final MessagesManager messagesManager) throws IndexingException {
        super(indexingServiceConfig, regionsService, dynamoDBService);
        this.productIndexingService = productIndexingService;
        this.attachmentIndexingService = attachmentIndexingService;
        this.messagesManager = messagesManager;
    }

    @Override
    public boolean index(final String indexingAction,
                         final String pagePath,
                         final String pageUuid,
                         final String publishTime,
                         final boolean isRecursive) throws IndexingException {
        return TracingUtil.callWithTracing(traceID -> {
            if (StringUtils.isBlank(pagePath) || StringUtils.isBlank(pageUuid)) {
                throw new IndexingException(this.handleExceptionMessage("Page UUID or path is empty"));
            }
            final List<String> failedNodeList = new ArrayList<>();

            final List<String> indexableSites = this.getIndexableSites();
            final SitePagePath rootSitePagePath = new SitePagePath(pagePath);
            if (indexableSites.contains(rootSitePagePath.getSite())) {
                log.info("Executing page {} for: {} (recursive: {})", indexingAction, pagePath, isRecursive);
                final List<PageNode> pagesToIndex = this.getPagesToProcess(pagePath, pageUuid, rootSitePagePath, isRecursive);
                final List<PageNode> successfulPages = new ArrayList<>();
                final List<PageIndexingPlan> pageIndexingPlans = new ArrayList<>();
                for (final PageNode page : pagesToIndex) {
                    if (this.isRegionInvalid(page.path())) {
                        continue;
                    }
                    final List<String> languages = this.getLanguages(page.path().getSite(), page.path().getRegion());
                    try {
                        pageIndexingPlans.add(this.buildIndexingPlan(indexingAction, page, languages, publishTime));
                        successfulPages.add(page);
                    } catch (NullPointerException exception) {
                        log.error("Unexpected null while building indexing plan for page {}", page.path(), exception);
                        failedNodeList.add(String.valueOf(page.path()));
                    }
                }
                this.flushIndexingPlans(pageIndexingPlans);
                final boolean isDeindexAction = DEINDEX_ACTION.equals(indexingAction);
                successfulPages.forEach(page -> this.updateAssociatedModels(page, isDeindexAction));
                if (!failedNodeList.isEmpty()) {
                    sendNotification(failedNodeList);
                }
            }
            return true;
        });
    }

    private PageIndexingPlan buildIndexingPlan(final String indexingAction,
                                               final PageNode page,
                                               final List<String> languages,
                                               final String publishTime) {
        if (indexingAction == null) {
            log.warn("Unsupported page indexing action null for page {}", page.path());
            return new PageIndexingPlan(List.of(), List.of());
        }
        if (page.uuid() == null) {
            log.warn("Skipping page indexing: page has no UUID. action={}, path={}", indexingAction, page.path());
            return new PageIndexingPlan(List.of(), List.of());
        }
        return switch (indexingAction) {
            case DEINDEX_ACTION -> new PageIndexingPlan(List.of(), buildDeletePlan(page, languages));
            case INDEX_ACTION -> buildWritePlan(page, languages, publishTime);
            default -> {
                log.warn("Unsupported page indexing action {} for page {}", indexingAction, page.path());
                yield new PageIndexingPlan(List.of(), List.of());
            }
        };
    }

    private PageIndexingPlan buildWritePlan(final PageNode page,
                                            final List<String> languages,
                                            final String publishTime) {
        final List<DynamoDbItem> itemsToWrite = new ArrayList<>();
        final List<PageIndexKey> primaryKeysToDelete = new ArrayList<>();
        final Map<String, DynamoDbItem> existingItemsByLanguage = getExistingItemsByLanguage(page);
        for (final String language : languages) {
            final DynamoDbItem pageItem = this.indexPage(page, language, publishTime);
            itemsToWrite.add(pageItem);
            collectRenamedItemToDelete(existingItemsByLanguage, language, pageItem.getString(TABLE_SORT_KEY_NAME))
                    .ifPresent(primaryKeysToDelete::add);
        }
        return new PageIndexingPlan(itemsToWrite, primaryKeysToDelete);
    }

    /* Method converts List<String> into flat comma separated String
     *  and sends notification to the user of all the nodes that failed.
     */
    public void sendNotification(final List<String> nodes) {
        final String msgBody = String.join(", ", nodes);
        final String subject = "Error during publication, please republish the following pages";
        final Message message = new Message();
        message.setSubject(subject);
        message.setMessage(msgBody);
        message.setType(MessageType.ERROR);
        messagesManager.sendLocalMessage(message);
    }

    private void updateAssociatedModels(final PageNode pageNode, final boolean isDeindexAction) {
        log.debug("update associated models for: {}", pageNode.path());
        NodeUtil.collectDxpNode(pageNode.path().toString(), WEBSITE_WORKSPACE)
                .ifPresent(node -> {
                    log.debug("update associated models collected node {}", NodeUtil.getPath(node));
                    productIndexingService.addPageAssociation(node, isDeindexAction);
                    attachmentIndexingService.addPageAssociation(node, isDeindexAction);
                });
    }

    private DynamoDbItem indexPage(final PageNode page, final String language, final String publishTime) {
        final SitePagePath sitePagePath = page.path();
        final String path = sitePagePath.buildPathForLanguage(language);
        final String friendlyUrl = this.getFriendlyUrlForLang(page, language);
        final PageIndexedItem event = convertToIndexedItem(page, friendlyUrl);
        final String region = Arrays.stream(path.split("/"))
                .filter(StringUtils::isNotEmpty)
                .findFirst()
                .orElse(EMPTY);
        final Map<String, Object> payload = INDEX_ITEM_MAPPER.convertValue(event, PAYLOAD_TYPE);
        return new DynamoDbItem()
                .withPrimaryKey(buildPrimaryKey(sitePagePath.getSite(), event.getUuid(), path))
                .withString(TABLE_REGION_KEY_NAME, region)
                .withString(TABLE_LANG_KEY_NAME, language)
                .withMap(TABLE_PAYLOAD_KEY_NAME, payload)
                .withString(TABLE_DATE_KEY_NAME, publishTime);
    }

    private Optional<PageIndexKey> collectRenamedItemToDelete(final Map<String, DynamoDbItem> existingItemsByLanguage,
                                                              final String language,
                                                              final String targetPath) {
        final DynamoDbItem existingItem = existingItemsByLanguage.get(language);
        if (existingItem == null) {
            return Optional.empty();
        }
        final String pk = existingItem.getString(TABLE_PARTITION_KEY_NAME);
        final String sk = existingItem.getString(TABLE_SORT_KEY_NAME);
        if (sk.equals(targetPath)) {
            return Optional.empty();
        }
        log.info("Scheduling renamed page deletion from index pk: {}, sk: {}", pk, sk);
        return Optional.of(new PageIndexKey(pk, sk));
    }

    private List<PageIndexKey> buildDeletePlan(final PageNode page, final List<String> languages) {
        return languages.stream()
                .map(language -> deindexPage(page, language))
                .toList();
    }

    private PageIndexKey deindexPage(final PageNode page, final String language) {
        final SitePagePath sitePagePath = page.path();
        final String path = sitePagePath.buildPathForLanguage(language);
        return new PageIndexKey(buildPk(sitePagePath.getSite(), page.uuid()), path);
    }

    private void flushIndexingPlans(final List<PageIndexingPlan> plans) throws IndexingException {
        final List<DynamoDbItem> itemsToWrite = dedupeItems(plans.stream()
                .flatMap(plan -> plan.itemsToWrite().stream())
                .toList());
        final List<PrimaryKey> primaryKeysToDelete = dedupePrimaryKeys(plans.stream()
                .flatMap(plan -> plan.primaryKeysToDelete().stream())
                .toList()).stream()
                .map(PageIndexKey::toPrimaryKey)
                .toList();
        log.info("Page indexing batch summary: plans={}, writes={}, deletes={}",
                plans.size(), itemsToWrite.size(), primaryKeysToDelete.size());
        try {
            if (!itemsToWrite.isEmpty() && !primaryKeysToDelete.isEmpty()) {
                dynamoDBService.batchWriteDeleteItems(indexerTableName, itemsToWrite, primaryKeysToDelete);
            } else if (!itemsToWrite.isEmpty()) {
                dynamoDBService.batchWriteItem(indexerTableName, itemsToWrite);
            } else if (!primaryKeysToDelete.isEmpty()) {
                dynamoDBService.batchDeleteItem(indexerTableName, primaryKeysToDelete);
            }
        } catch (Exception e) {
            final String message = this.handleExceptionMessage(String.format("Error while flushing page indexing batch: %s", e.getMessage()));
            throw new IndexingException(message, e);
        }
    }

    private Map<String, DynamoDbItem> getExistingItemsByLanguage(final PageNode page) {
        final QueryRequest querySpec = QueryRequest.builder()
                .keyConditionExpression(String.format("%s = :id", TABLE_PARTITION_KEY_NAME))
                .expressionAttributeValues(Map.of(":id", AttributeValue.fromS(buildPk(page.path().getSite(), page.uuid()))))
                .build();
        return dynamoDBService.queryItems(indexerTableName, querySpec).stream()
                .filter(item -> item.get(TABLE_LANG_KEY_NAME) != null)
                .collect(Collectors.toMap(
                        item -> String.valueOf(item.get(TABLE_LANG_KEY_NAME)),
                        item -> item,
                        (left, right) -> left,
                        LinkedHashMap::new));
    }

    private List<DynamoDbItem> dedupeItems(final List<DynamoDbItem> items) {
        return new ArrayList<>(items.stream()
                .collect(Collectors.toMap(
                        PageIndexKey::from,
                        item -> item,
                        (left, right) -> left,
                        LinkedHashMap::new))
                .values());
    }

    private List<PageIndexKey> dedupePrimaryKeys(final List<PageIndexKey> primaryKeys) {
        return primaryKeys.stream().distinct().toList();
    }

    private PrimaryKey buildPrimaryKey(final String siteName, final String uuid, final String pagePath) {
        return new PrimaryKey(TABLE_PARTITION_KEY_NAME, buildPk(siteName, uuid), TABLE_SORT_KEY_NAME, pagePath);
    }

    private String buildPk(final String siteName, final String uuid) {
        return String.format("%s#%s#%s", siteName, TABLE_PK_PAGE_TYPE, uuid);
    }

    @Override
    protected String getExceptionMessageTitle() {
        return PAGE_NOT_INDEXED_MSG;
    }

    private PageIndexedItem convertToIndexedItem(final PageNode page, final String path) {
        final String uuid = page.uuid();
        if (uuid == null) {
            log.error("BUG: convertToIndexedItem called with null UUID for path={} — using 'N/A' as fallback pk", page.path());
        }
        final String resolvedUuid = uuid != null ? uuid : "N/A";
        final long lastModified = Optional.ofNullable(page.lastModifiedDate())
                .map(Instant::toEpochMilli)
                .orElse(0L);
        return new PageIndexedItem(resolvedUuid, path, lastModified);
    }

    private record PageIndexingPlan(List<DynamoDbItem> itemsToWrite, List<PageIndexKey> primaryKeysToDelete) {

        private PageIndexingPlan(final List<DynamoDbItem> itemsToWrite, final List<PageIndexKey> primaryKeysToDelete) {
            this.itemsToWrite = List.copyOf(itemsToWrite);
            this.primaryKeysToDelete = List.copyOf(primaryKeysToDelete);
        }
    }

    private record PageIndexKey(String partitionKey, String sortKey) {

        private static PageIndexKey from(final DynamoDbItem item) {
            return new PageIndexKey(item.getString(TABLE_PARTITION_KEY_NAME), item.getString(TABLE_SORT_KEY_NAME));
        }

        private PrimaryKey toPrimaryKey() {
            return new PrimaryKey(TABLE_PARTITION_KEY_NAME, partitionKey, TABLE_SORT_KEY_NAME, sortKey);
        }
    }
}
