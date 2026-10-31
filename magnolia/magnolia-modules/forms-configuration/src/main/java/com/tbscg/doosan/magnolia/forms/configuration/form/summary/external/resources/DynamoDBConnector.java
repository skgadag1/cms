package com.tbscg.doosan.magnolia.forms.configuration.form.summary.external.resources;

import com.tbscg.doosan.magnolia.dxp.core.util.TracingUtil;
import com.tbscg.doosan.magnolia.forms.configuration.FormsConfigurationModule;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import software.amazon.awssdk.enhanced.dynamodb.*;
import software.amazon.awssdk.enhanced.dynamodb.document.DocumentTableSchema;
import software.amazon.awssdk.enhanced.dynamodb.document.EnhancedDocument;
import software.amazon.awssdk.enhanced.dynamodb.model.QueryConditional;
import software.amazon.awssdk.enhanced.dynamodb.model.QueryEnhancedRequest;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

@Singleton
@Slf4j
public class DynamoDBConnector implements TableConnector<EnhancedDocument> {
    @Setter
    @Getter
    private DynamoDbTable<EnhancedDocument> table;

    @Inject
    private FormsConfigurationModule formsConfigurationModule;
    @Inject
    private DynamoDbClient amazonDynamoDB;

    @PostConstruct
    void initTable() {
        DynamoDbEnhancedClient dynamoDBClient = DynamoDbEnhancedClient.builder()
                .dynamoDbClient(amazonDynamoDB)
                .build();
        String tableName = formsConfigurationModule.getDownloadFormSummaryConfig().getDynamoDBTableName();
        DocumentTableSchema tableSchema = TableSchema.documentSchemaBuilder()
                .addIndexPartitionKey(TableMetadata.primaryIndexName(), "pk", AttributeValueType.S)
                .attributeConverterProviders(AttributeConverterProvider.defaultProvider())
                .build();
        table = dynamoDBClient.table(tableName, tableSchema);
    }

    public Stream<EnhancedDocument> getRowsByKey(String key) {
        QueryEnhancedRequest queryRequest = QueryEnhancedRequest.builder()
                .queryConditional(QueryConditional.keyEqualTo(Key.builder().partitionValue(key).build()))
                .build();

        return TracingUtil.callWithTracing(traceID -> StreamSupport
                .stream(table.query(queryRequest).spliterator(), false)
                .flatMap(page -> page.items().stream()));

    }
}
