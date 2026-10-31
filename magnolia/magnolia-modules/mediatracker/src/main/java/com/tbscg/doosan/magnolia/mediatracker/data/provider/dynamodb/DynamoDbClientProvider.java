package com.tbscg.doosan.magnolia.mediatracker.data.provider.dynamodb;

import lombok.Getter;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;

@Getter
public class DynamoDbClientProvider {

    private final DynamoDbEnhancedClient client;

    public DynamoDbClientProvider() {
        this.client = DynamoDbEnhancedClient.builder()
                .dynamoDbClient(DynamoDbClient.builder().build())
                .build();
    }

}
