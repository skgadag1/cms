package com.tbscg.doosan.magnolia.dxp.workflow.service;

import com.tbscg.doosan.magnolia.dxp.workflow.model.ddb.DynamoDbItem;
import com.tbscg.doosan.magnolia.dxp.workflow.model.ddb.PrimaryKey;
import com.tbscg.doosan.magnolia.dxp.workflow.util.WorkflowExecutionSettings;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.ListUtils;
import software.amazon.awssdk.services.dynamodb.model.BatchWriteItemRequest;
import software.amazon.awssdk.services.dynamodb.model.BatchWriteItemResponse;
import software.amazon.awssdk.services.dynamodb.model.DeleteItemRequest;
import software.amazon.awssdk.services.dynamodb.model.DeleteItemResponse;
import software.amazon.awssdk.services.dynamodb.model.PutItemRequest;
import software.amazon.awssdk.services.dynamodb.model.PutItemResponse;
import software.amazon.awssdk.services.dynamodb.model.QueryRequest;
import software.amazon.awssdk.services.dynamodb.model.UpdateItemRequest;
import software.amazon.awssdk.services.dynamodb.model.UpdateItemResponse;
import software.amazon.awssdk.services.dynamodb.model.WriteRequest;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

/**
 * AWS DynamoDB Service
 */
@Slf4j
public class DynamoDBService {

    private static final int MAX_BATCH_RETRY_ATTEMPTS = 5;
    private static final long BATCH_RETRY_BASE_DELAY_MS = 100;

    private final DynamoDBClientFactoryImpl clientFactory;

    @Inject
    public DynamoDBService(final DynamoDBClientFactoryImpl dynamoClientFactory) {
        this.clientFactory = dynamoClientFactory;
    }

    /**
     * Puts an item into given DynamoDB table.
     *
     * @param tableName table name to put item into
     * @param item      item to put
     * @return the outcome of put item action
     */
    public PutItemResponse putItem(final String tableName, final DynamoDbItem item) {
        return clientFactory.getAmazonDynamoDB()
                .putItem(PutItemRequest.builder()
                        .tableName(tableName)
                        .item(item.getValueMap())
                        .build());
    }

    /**
     * Update an item into given DynamoDB table.
     *
     * @param tableName table name to update item in
     * @param item      item to update
     * @return the outcome of update item action
     */
    public UpdateItemResponse updateItem(final String tableName, final UpdateItemRequest item) {
        return clientFactory.getAmazonDynamoDB()
                .updateItem(item.toBuilder().tableName(tableName).build());
    }

    /**
     * Puts an item into given DynamoDB table.
     *
     * @param tableName  table name to remove item from
     * @param primaryKey primary key representation of the item to remove
     * @return the outcome of delete item action
     */
    public DeleteItemResponse deleteItem(final String tableName, final PrimaryKey primaryKey) {
        return clientFactory.getAmazonDynamoDB()
                .deleteItem(DeleteItemRequest.builder()
                        .tableName(tableName)
                        .key(primaryKey.getValueMap())
                        .build());
    }

    /**
     * Write multiple items in single batch into given DynamoDB table.
     *
     * @param tableName table name to put item into
     * @param items     list of items to put
     */
    public void batchWriteItem(final String tableName, final List<DynamoDbItem> items) {
        ListUtils.partition(items, WorkflowExecutionSettings.DYNAMODB_BATCH_WRITE_SIZE)
                .forEach(batch -> executeBatchWrite(tableName, batch));
    }

    public List<DynamoDbItem> queryItems(final String tableName, final QueryRequest spec) {
        return clientFactory.getAmazonDynamoDB()
                .query(spec.toBuilder().tableName(tableName).build())
                .items()
                .stream()
                .map(DynamoDbItem::fromValueMap)
                .toList();
    }

    public List<DynamoDbItem> queryItems(final String tableName, final String indexName, final QueryRequest spec) {
        return clientFactory.getAmazonDynamoDB()
                .query(spec.toBuilder().tableName(tableName).indexName(indexName).build())
                .items()
                .stream()
                .map(DynamoDbItem::fromValueMap)
                .toList();
    }

    /**
     * Delete multiple items in single batch into given DynamoDB table.
     *
     * @param tableName   table name to put item into
     * @param primaryKeys list of primary keys to put
     */
    public void batchDeleteItem(final String tableName, final List<PrimaryKey> primaryKeys) {
        ListUtils.partition(primaryKeys, WorkflowExecutionSettings.DYNAMODB_BATCH_WRITE_SIZE)
                .forEach(batch -> executeBatchDelete(tableName, batch));
    }

    /**
     * Write and delete multiple items in single batch into given DynamoDB table.
     *
     * @param tableName           table name to update
     * @param itemsToWrite        list of items to put
     * @param primaryKeysToDelete list of primary keys to delete
     */
    public void batchWriteDeleteItems(final String tableName,
                                      final List<DynamoDbItem> itemsToWrite,
                                      final List<PrimaryKey> primaryKeysToDelete) {
        final List<BatchWriteOperation> operations = new ArrayList<>();
        itemsToWrite.stream()
                .map(BatchWriteOperation::put)
                .forEach(operations::add);
        primaryKeysToDelete.stream()
                .map(BatchWriteOperation::delete)
                .forEach(operations::add);

        ListUtils.partition(operations, WorkflowExecutionSettings.DYNAMODB_BATCH_WRITE_SIZE)
                .forEach(batch -> executeBatchWriteDelete(tableName, batch));
    }

    private void executeBatchWrite(final String tableName, final Collection<DynamoDbItem> items) {
        if (items.isEmpty()) {
            return;
        }
        final List<WriteRequest> requests = items.stream()
                .map(item -> WriteRequest.builder()
                        .putRequest(request -> request.item(item.getValueMap()))
                        .build())
                .toList();
        executeBatchWriteWithRetry(tableName, BatchWriteItemRequest.builder()
                .requestItems(Map.of(tableName, requests))
                .build());
    }

    private void executeBatchDelete(final String tableName, final List<PrimaryKey> primaryKeys) {
        if (primaryKeys.isEmpty()) {
            return;
        }
        final List<WriteRequest> requests = primaryKeys.stream()
                .map(key -> WriteRequest.builder()
                        .deleteRequest(request -> request.key(key.getValueMap()))
                        .build())
                .toList();
        executeBatchWriteWithRetry(tableName, BatchWriteItemRequest.builder()
                .requestItems(Map.of(tableName, requests))
                .build());
    }

    private void executeBatchWriteDelete(final String tableName, final List<BatchWriteOperation> operations) {
        final List<WriteRequest> requests = operations.stream()
                .map(BatchWriteOperation::toWriteRequest)
                .filter(Objects::nonNull)
                .toList();
        if (requests.isEmpty()) {
            return;
        }
        executeBatchWriteWithRetry(tableName, BatchWriteItemRequest.builder()
                .requestItems(Map.of(tableName, requests))
                .build());
    }

    /**
     * Retries a DynamoDB BatchWriteItem request for any unprocessed items using truncated
     * exponential backoff (100 ms * 2^attempt, capped at 3200 ms) up to a maximum of
     * {@value #MAX_BATCH_RETRY_ATTEMPTS} additional attempts. If items are still unprocessed
     * after all retries, an {@link IllegalStateException} is thrown so callers can surface
     * the failure rather than silently dropping writes.
     */
    private void executeBatchWriteWithRetry(final String tableName, final BatchWriteItemRequest request) {
        BatchWriteItemResponse outcome = clientFactory.getAmazonDynamoDB().batchWriteItem(request);
        int attempt = 0;
        while (!outcome.unprocessedItems().isEmpty()) {
            attempt++;
            if (attempt > MAX_BATCH_RETRY_ATTEMPTS) {
                final int remaining = outcome.unprocessedItems().values().stream()
                        .mapToInt(List::size)
                        .sum();
                log.error("BatchWrite for table {} still has {} unprocessed items after {} retries — aborting",
                        tableName, remaining, MAX_BATCH_RETRY_ATTEMPTS);
                throw new IllegalStateException(String.format(
                        "DynamoDB BatchWrite for table '%s' could not process %d item(s) after %d retries",
                        tableName, remaining, MAX_BATCH_RETRY_ATTEMPTS));
            }
            final long delayMs = Math.min(BATCH_RETRY_BASE_DELAY_MS << (attempt - 1), 3200);
            log.warn("BatchWrite for table {} has unprocessed items on attempt {}; retrying in {} ms",
                    tableName, attempt, delayMs);
            try {
                TimeUnit.MILLISECONDS.sleep(delayMs);
            } catch (InterruptedException ie) {
                Thread.currentThread().interrupt();
                throw new IllegalStateException("DynamoDB BatchWrite retry interrupted", ie);
            }
            outcome = clientFactory.getAmazonDynamoDB()
                    .batchWriteItem(BatchWriteItemRequest.builder()
                            .requestItems(outcome.unprocessedItems())
                            .build());
        }
    }

    private record BatchWriteOperation(DynamoDbItem itemToPut, PrimaryKey primaryKeyToDelete) {

        private static BatchWriteOperation put(final DynamoDbItem item) {
            return new BatchWriteOperation(item, null);
        }

        private static BatchWriteOperation delete(final PrimaryKey primaryKey) {
            return new BatchWriteOperation(null, primaryKey);
        }

        private WriteRequest toWriteRequest() {
            if (itemToPut != null) {
                return WriteRequest.builder()
                        .putRequest(request -> request.item(itemToPut.getValueMap()))
                        .build();
            }
            if (primaryKeyToDelete != null) {
                return WriteRequest.builder()
                        .deleteRequest(request -> request.key(primaryKeyToDelete.getValueMap()))
                        .build();
            }
            return null;
        }
    }
}
