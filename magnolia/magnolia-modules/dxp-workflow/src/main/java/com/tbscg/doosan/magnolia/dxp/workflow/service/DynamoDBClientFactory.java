package com.tbscg.doosan.magnolia.dxp.workflow.service;

import software.amazon.awssdk.services.dynamodb.DynamoDbClient;

public interface DynamoDBClientFactory {
    DynamoDbClient getAmazonDynamoDB();
}
