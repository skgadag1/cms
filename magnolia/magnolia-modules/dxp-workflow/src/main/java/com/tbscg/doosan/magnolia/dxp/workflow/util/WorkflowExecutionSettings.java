package com.tbscg.doosan.magnolia.dxp.workflow.util;

/**
 * Shared workflow metadata and batching defaults used across publication, indexing and related-node handling.
 */
public final class WorkflowExecutionSettings {

    public static final String VERSION_TIME_PROPERTY = "versionTime";

    /**
     * Number of JCR nodes whose {@code versionTime} property is dirtied before
     * {@code Session.save()} is called. Batching reduces the number of round-trips to the
     * JCR database during a recursive publish.
     */
    public static final int VERSION_TIME_SESSION_SAVE_BATCH_SIZE = 250;

    /**
     * Maximum number of items per DynamoDB {@code BatchWriteItem} request.
     * DynamoDB hard-limits this to 25; do not raise above that value.
     */
    public static final int DYNAMODB_BATCH_WRITE_SIZE = 25;

    /**
     * Maximum number of UUIDs combined into a single JCR-SQL2 {@code CONTAINS} query using
     * {@code OR} clauses. Used by all related-node and attachment finders.
     */
    public static final int JCR_QUERY_BATCH_SIZE = 100;
    public static final int RELATED_NODE_QUERY_BATCH_SIZE = JCR_QUERY_BATCH_SIZE;
    public static final int RELATED_ATTACHMENT_QUERY_BATCH_SIZE = JCR_QUERY_BATCH_SIZE;

    private WorkflowExecutionSettings() {
    }
}
