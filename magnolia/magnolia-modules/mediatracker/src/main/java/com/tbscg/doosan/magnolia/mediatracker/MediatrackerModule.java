package com.tbscg.doosan.magnolia.mediatracker;


import info.magnolia.module.ModuleLifecycle;
import info.magnolia.module.ModuleLifecycleContext;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

/**
 * Configuration class for Mediatracker module.
 */
@Getter
@Setter
@Slf4j
public class MediatrackerModule implements ModuleLifecycle {
    private String divisions;
    private ElasticsearchConfig elasticsearchConfig;
    private DynamoDbConfig dynamoDbConfig;

    @Override
    public void start(ModuleLifecycleContext moduleLifecycleContext) {
        log.debug("START mediatracker module");
    }

    @Override
    public void stop(ModuleLifecycleContext moduleLifecycleContext) {
        log.debug("STOP mediatracker module");
    }


    @Getter
    @Setter
    public class ElasticsearchConfig {
        private String indexName;
        private String endpoint;
        private String serviceName;
        private String region;
        private String searchSuffix;
        private String domainsTable;
        private String urlsTable;
    }

    @Getter
    @Setter
    public class DynamoDbConfig {
        private String domainsTable;
        private String urlsTable;
    }

}
