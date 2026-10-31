package com.tbscg.doosan.magnolia.mediatracker.data.provider;

import com.tbscg.doosan.magnolia.mediatracker.data.model.Division;
import com.tbscg.doosan.magnolia.mediatracker.data.model.DomainFilters;
import com.tbscg.doosan.magnolia.mediatracker.data.model.VanityDataSearchResult;
import com.tbscg.doosan.magnolia.mediatracker.data.model.VanityDomain;
import com.tbscg.doosan.magnolia.mediatracker.data.provider.dynamodb.DynamoDbService;
import com.tbscg.doosan.magnolia.mediatracker.data.provider.elasticsearch.ElasticsearchService;
import com.vaadin.data.provider.Query;
import lombok.extern.slf4j.Slf4j;
import org.apache.logging.log4j.util.Strings;

import javax.inject.Inject;
import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;

/**
 * Class implementing back end data providers for Domain
 */
@Slf4j
public class DomainServiceImpl extends BaseMediatrackerDataProvider<VanityDomain, DomainFilters> implements VanityDomainService {

    @Inject
    public DomainServiceImpl(DynamoDbService dynamoDbService, ElasticsearchService elasticsearchService) {
        super(elasticsearchService, dynamoDbService);
    }

    /**
     * Gets domain by code
     *
     * @param code code parameter
     * @return
     */
    @Override
    public VanityDomain getDomain(String code) {
        if (Strings.isNotBlank(code)) {
            log.debug("Get domain with Code: {}", code);
            VanityDomain domain = elasticsearchService.getDomain(code);
            return domain;
        } else {
            log.error("Code parameter cant be empty");
        }
        return null;
    }

    /**
     * Checks if domain name is unique
     *
     * @param domain domain name to check
     * @return true if is unique false otherwise
     */
    public boolean isDomainNameNotTaken(VanityDomain domain) {
        VanityDomain existingDomain = elasticsearchService.getDomainByDomainName(domain.getFullDomainName());
        return existingDomain == null || existingDomain.getCode().equals(domain.getCode());
    }

    /**
     * Get domains matching division
     *
     * @param division division to match
     * @return matching domains
     */
    @Override
    public List<VanityDomain> getDomains(Division division) {
        if (division != null) {
            log.debug("Get domains with division: {}", division);
            return elasticsearchService.getDomains(division);
        } else {
            return Collections.emptyList();
        }
    }

    @Override
    public void saveDomain(VanityDomain domain) {
        log.debug("DOMAIN SAVE: {} / {}", domain.getFullDomainName(), domain.getCode());
        dynamoDbService.add(domain);
    }

    @Override
    public void deleteDomain(String code) {
        log.debug("DOMAIN DELETE: {}", code);
        dynamoDbService.deleteDomain(code);
    }

    @Override
    protected Stream<VanityDomain> fetchFromBackEnd(Query<VanityDomain, DomainFilters> query) {
        if (query.getFilter().isPresent()) {
            log.debug("fetching domains from backend");
            List<VanityDomain> results = elasticsearchService.getDomains(query.getFilter().get());
            return results.stream();
        }
        return Stream.empty();
    }

    @Override
    protected VanityDataSearchResult<VanityDomain> fetchFromBackendWithPagination(Query<VanityDomain, DomainFilters> query) {
        if (query.getFilter().isPresent()) {
            log.debug("fetching domains from backend with pagination");
            return elasticsearchService.getDomainsWithPagination(query.getFilter().get());
        }
        return emptyResults;
    }
}
