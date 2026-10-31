package com.tbscg.doosan.magnolia.bobcat.external.dynamodb;

import software.amazon.awssdk.services.dynamodb.DynamoDbClient;

import javax.inject.Provider;

/**
 * Factory class for {@link DynamoDbClient}. Creates default client.
 */
public class DefaultAmazonDynamoDBClientProvider implements Provider<DynamoDbClient> {

    @Override
    public DynamoDbClient get() {
        return DynamoDbClient.create();
    }

}
