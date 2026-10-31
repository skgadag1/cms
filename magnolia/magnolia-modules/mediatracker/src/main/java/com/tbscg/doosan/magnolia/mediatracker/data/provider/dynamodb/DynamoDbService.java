package com.tbscg.doosan.magnolia.mediatracker.data.provider.dynamodb;

import com.tbscg.doosan.magnolia.dxp.core.util.TracingUtil;
import com.tbscg.doosan.magnolia.mediatracker.MediatrackerModule;
import com.tbscg.doosan.magnolia.mediatracker.data.model.VanityDomain;
import com.tbscg.doosan.magnolia.mediatracker.data.model.VanityUrl;
import software.amazon.awssdk.enhanced.dynamodb.*;
import software.amazon.awssdk.enhanced.dynamodb.document.DocumentTableSchema;
import software.amazon.awssdk.enhanced.dynamodb.document.EnhancedDocument;

import javax.inject.Inject;

/**
 * Class exposing DynamoDB operations
 */
public class DynamoDbService {
    private final DynamoDbRepository dynamoDbRepository;
    private final MediatrackerModule mediatrackerModule;
    private final DynamoDbClientProvider clientProvider;

    @Inject
    public DynamoDbService(DynamoDbRepository dynamoDbRepository, MediatrackerModule mediatrackerModule, DynamoDbClientProvider clientProvider) {
        this.dynamoDbRepository = dynamoDbRepository;
        this.mediatrackerModule = mediatrackerModule;
        this.clientProvider = clientProvider;

        initializeSchemas();
    }

    private void initializeSchemas() {
        DocumentTableSchema tableSchema = TableSchema.documentSchemaBuilder()
                .addIndexPartitionKey(TableMetadata.primaryIndexName(), DynamoDbItemMapper.CODE, AttributeValueType.S)
                .attributeConverterProviders(AttributeConverterProvider.defaultProvider())
                .build();
        dynamoDbRepository.addSchema(mediatrackerModule.getDynamoDbConfig().getDomainsTable(), tableSchema);
        dynamoDbRepository.addSchema(mediatrackerModule.getDynamoDbConfig().getUrlsTable(), tableSchema);
    }

    /**
     * Adds new or replaces already existing domain.
     *
     * @param domain domain to add or update
     * @return true if successful or false otherwise
     */
    public boolean add(VanityDomain domain) {
        EnhancedDocument item = DynamoDbItemMapper.fromVanityDomain(domain);
        return TracingUtil.callWithTracing(traceID ->
                dynamoDbRepository.createNewOrReplace(
                        item,
                        mediatrackerModule.getDynamoDbConfig().getDomainsTable(),
                        clientProvider.getClient()
                )
        );
    }

    /**
     * Deletes domain with given code value
     *
     * @param code value of code parameter
     * @return true if successful or false otherwise
     */
    public boolean deleteDomain(String code) {
        Key pk = DynamoDbItemMapper.toPrimaryKey(code);
        return TracingUtil.callWithTracing(traceID ->
                dynamoDbRepository.delete(
                        pk,
                        mediatrackerModule.getDynamoDbConfig().getDomainsTable(),
                        clientProvider.getClient()
                )
        );
    }

    /**
     * Deletes url with given code value
     *
     * @param code value of code parameter
     * @return true if successful or false otherwise
     */
    public boolean deleteUrl(String code) {
        Key pk = DynamoDbItemMapper.toPrimaryKey(code);
        return TracingUtil.callWithTracing(traceID ->
                dynamoDbRepository.delete(
                        pk,
                        mediatrackerModule.getDynamoDbConfig().getUrlsTable(),
                        clientProvider.getClient()
                )
        );
    }

    /**
     * Adds new or replaces already existing url.
     *
     * @param url url to add or update
     * @return true if successful or false otherwise
     */
    public boolean add(VanityUrl url) {
        EnhancedDocument item = DynamoDbItemMapper.fromVanityUrl(url);
        return TracingUtil.callWithTracing(traceID ->
                dynamoDbRepository.createNewOrReplace(
                        item,
                        mediatrackerModule.getDynamoDbConfig().getUrlsTable(),
                        clientProvider.getClient()
                )
        );
    }
}