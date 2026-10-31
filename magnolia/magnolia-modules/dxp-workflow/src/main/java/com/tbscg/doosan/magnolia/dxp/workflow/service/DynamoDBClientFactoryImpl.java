package com.tbscg.doosan.magnolia.dxp.workflow.service;

import com.google.common.base.Supplier;
import com.google.common.base.Suppliers;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;

public class DynamoDBClientFactoryImpl implements DynamoDBClientFactory {

    private final Supplier<DynamoDbClient> amazonDynamoDBSupplier = Suppliers.memoize(this::createAmazonDynamoDB);

    @Override
    public DynamoDbClient getAmazonDynamoDB() {
        return amazonDynamoDBSupplier.get();
    }

    DynamoDbClient createAmazonDynamoDB() {
        return DynamoDbClient.builder().build();
    }
}
