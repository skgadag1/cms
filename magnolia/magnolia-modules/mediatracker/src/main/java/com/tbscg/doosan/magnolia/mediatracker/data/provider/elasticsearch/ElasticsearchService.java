package com.tbscg.doosan.magnolia.mediatracker.data.provider.elasticsearch;

import com.tbscg.doosan.magnolia.mediatracker.MediatrackerModule;
import com.tbscg.doosan.magnolia.mediatracker.data.model.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

import static com.tbscg.doosan.magnolia.dxp.core.util.LoggingUtil.sanitizeMessage;

/**
 * Class exposing Elasticsearch operations
 */
@Slf4j
public class ElasticsearchService {

    private ElasticsearchHttpService elasticsearchHttpService;
    private MediatrackerModule mediatrackerModule;

    @Inject
    public ElasticsearchService(ElasticsearchHttpService elasticsearchHttpService, MediatrackerModule mediatrackerModule) {
        this.elasticsearchHttpService = elasticsearchHttpService;
        this.mediatrackerModule = mediatrackerModule;
    }

    /**
     * Returns domain with given code value (code is primary key in table)
     *
     * @param code domain's code value
     * @return matching domain
     */
    public VanityDomain getDomain(String code) {
        ElasticsearchQueryParams elasticsearchQueryParams =
                ElasticsearchQueryParams.fromCode(code, mediatrackerModule.getElasticsearchConfig().getDomainsTable());
        String queryResult = getQueryResult(elasticsearchQueryParams);
        return ElasticsearchMapper.resultsToDomainsList(queryResult).get(0);
    }

    /**
     * Returns list of domains matching search criteria
     *
     * @param filters object aggregating search criteria
     * @return domains matching search criteria
     */
    public List<VanityDomain> getDomains(DomainFilters filters) {
        return this.getVanityDataByFilters(
                filters.isFetchAll(),
                ElasticsearchQueryParams.fromDomainFilters(filters, mediatrackerModule.getElasticsearchConfig().getDomainsTable()),
                ElasticsearchMapper::resultsToDomainsList);
    }


    /**
     * Returns list of domains with pagination matching search criteria
     *
     * @param filters object aggregating search criteria
     * @return domains with pagination matching search criteria
     */
    public VanityDataSearchResult<VanityDomain> getDomainsWithPagination(DomainFilters filters) {
        return this.getVanityDataWithPaginationByFilters(
                filters.isFetchAll(),
                ElasticsearchQueryParams.fromDomainFilters(filters, mediatrackerModule.getElasticsearchConfig().getDomainsTable()),
                ElasticsearchMapper::resultsToDomainsList);
    }

    /**
     * Returns list of domains matching provided division
     *
     * @param division division to match
     * @return list of matching domains
     */
    public List<VanityDomain> getDomains(Division division) {
        ElasticsearchQueryParams elasticsearchQueryParams =
                ElasticsearchQueryParams.fromDivision(division, mediatrackerModule.getElasticsearchConfig().getDomainsTable());
        String queryResult = getQueryResult(elasticsearchQueryParams);
        return ElasticsearchMapper.resultsToDomainsList(queryResult);
    }

    /**
     * Returns domain with given domainName value
     *
     * @param domainName value of domainName parameter
     * @return matching domain
     */
    public VanityDomain getDomainByDomainName(String domainName) {
        if (domainName != null) {
            log.debug("Get domain with domainName: {}", domainName);
            ElasticsearchQueryParams elasticsearchQueryParams =
                    ElasticsearchQueryParams.fromDomainName(domainName, mediatrackerModule.getElasticsearchConfig().getDomainsTable());
            String queryResult = getQueryResult(elasticsearchQueryParams);
            return ElasticsearchMapper.resultsToDomainsList(queryResult).stream().findFirst().orElse(null);
        } else {
            log.warn("Domain name can't be empty");
            return null;
        }
    }


    //----------------URL-------------------------//

    /**
     * Gets url by code parameter.
     *
     * @param code value of code parameter
     * @return matching url
     */
    public VanityUrl getUrl(String code) {
        ElasticsearchQueryParams elasticsearchQueryParams =
                ElasticsearchQueryParams.fromCode(code, mediatrackerModule.getElasticsearchConfig().getUrlsTable());
        String queryResult = getQueryResult(elasticsearchQueryParams);
        return ElasticsearchMapper.resultsToUrlsList(queryResult).get(0);
    }

    /**
     * Gets all urls assigned to given domain.
     *
     * @param domainCode value of domain code
     * @return list of matching urls
     */
    public List<VanityUrl> getUrlsByDomainCode(String domainCode) {
        ElasticsearchQueryParams elasticsearchQueryParams =
                ElasticsearchQueryParams.fromVanityDomain(domainCode, mediatrackerModule.getElasticsearchConfig().getUrlsTable());
        String queryResult = getQueryResult(elasticsearchQueryParams);
        return ElasticsearchMapper.resultsToUrlsList(queryResult);
    }

    public List<VanityUrl> getUrls(UrlFilters filters) {
        return this.getVanityDataByFilters(
                filters.isFetchAll(),
                ElasticsearchQueryParams.fromUrlFilters(filters, mediatrackerModule.getElasticsearchConfig().getUrlsTable()),
                ElasticsearchMapper::resultsToUrlsList);
    }

    /**
     * Returns list of urls with pagination matching search criteria
     *
     * @param filters object aggregating search criteria
     * @return urls with pagination matching search criteria
     */
    public VanityDataSearchResult<VanityUrl> getUrlsWithPagination(UrlFilters filters) {
        return this.getVanityDataWithPaginationByFilters(
                filters.isFetchAll(),
                ElasticsearchQueryParams.fromUrlFilters(filters, mediatrackerModule.getElasticsearchConfig().getUrlsTable()),
                ElasticsearchMapper::resultsToUrlsList);
    }

    private <T extends BaseVanityData> List<T> getVanityDataByFilters(
            boolean isFetchAll,
            ElasticsearchQueryParams elasticsearchQueryParams,
            Function<String, List<T>> mapper) {
        List<T> vanityData = new ArrayList<>();
        String queryResult;
        String[] sortOfLast;
        String[] sortOfFirst;
        do {
            queryResult = getQueryResult(elasticsearchQueryParams);
            sortOfLast = ElasticsearchMapper.getSortOfLast(queryResult);
            sortOfFirst = ElasticsearchMapper.getSortOfFirst(queryResult);
            elasticsearchQueryParams.setSearchAfter(sortOfLast);
            vanityData.addAll(mapper.apply(queryResult));
        } while (isFetchAll && sortOfLast.length > 0 && sortOfFirst.length > 0);
        return vanityData;
    }

    private <T extends BaseVanityData> VanityDataSearchResult<T> getVanityDataWithPaginationByFilters(
            boolean isFetchAll,
            ElasticsearchQueryParams elasticsearchQueryParams,
            Function<String, List<T>> mapper) {
        List<T> vanityData = new ArrayList<>();
        Integer total;
        String queryResult;
        String[] sortOfLast;
        String[] sortOfFirst;
        String[] searchAfterSort;
        String[] searchBeforeSort;
        elasticsearchQueryParams.setTrackTotalHits(true);
        do {
            queryResult = getQueryResult(elasticsearchQueryParams);
            sortOfLast = ElasticsearchMapper.getSortOfLast(queryResult);
            sortOfFirst = ElasticsearchMapper.getSortOfFirst(queryResult);
            total = ElasticsearchMapper.resultsToTotalHitsValue(queryResult);
            elasticsearchQueryParams.setSearchAfter(sortOfLast);
            vanityData.addAll(mapper.apply(queryResult));
        } while (isFetchAll && sortOfLast.length > 0 && sortOfFirst.length > 0);

        if (SortOrder.DESC.name().equalsIgnoreCase(elasticsearchQueryParams.getSort().getSortOrder())) {
            searchAfterSort = sortOfLast;
            searchBeforeSort = sortOfFirst;
        } else {
            searchAfterSort = sortOfFirst;
            searchBeforeSort = sortOfLast;
        }
        return new VanityDataSearchResult<>(vanityData, total, searchAfterSort, searchBeforeSort);
    }

    /**
     * Returns url with given path value
     *
     * @param path       value of path parameter
     * @param domainCode value of vanity domain's code
     * @param isStrict   is strict search
     * @return matching url
     */
    public VanityUrl getUrlByPath(String path, String domainCode, boolean isStrict) {
        if (StringUtils.isNotEmpty(path)) {
            log.debug("Get url with path: {}", path);
            ElasticsearchQueryParams elasticsearchQueryParams =
                    ElasticsearchQueryParams.fromUrlPath(path, domainCode, mediatrackerModule.getElasticsearchConfig().getUrlsTable(), isStrict);
            String queryResult = getQueryResult(elasticsearchQueryParams);
            return ElasticsearchMapper.resultsToUrlsList(queryResult).stream().findFirst().orElse(null);
        } else {
            log.warn("Url path can't be empty");
            return null;
        }
    }

    /**
     * Returns url with given path value
     *
     * @param path       value of path parameter
     * @param domainCode value of vanity domain's code
     * @return matching url
     */
    public VanityUrl getUrlByPath(String path, String domainCode) {
        return getUrlByPath(path, domainCode, false);
    }

    /**
     * Gets total number of items found matching given search criteria
     *
     * @param filters search criteria
     * @return total number of matching items
     */
    public int getTotalHitsValue(MediatrackerFilters filters) {
        ElasticsearchQueryParams elasticsearchQueryParams;
        if (filters instanceof UrlFilters) {
            elasticsearchQueryParams = ElasticsearchQueryParams
                    .fromUrlFilters((UrlFilters) filters, mediatrackerModule.getElasticsearchConfig().getUrlsTable());
        } else if (filters instanceof DomainFilters) {
            elasticsearchQueryParams = ElasticsearchQueryParams
                    .fromDomainFilters((DomainFilters) filters, mediatrackerModule.getElasticsearchConfig().getDomainsTable());
        } else {
            log.error("Unsupported type of filters object");
            return -1;
        }
        elasticsearchQueryParams.setSkipResults(true);
        elasticsearchQueryParams.setTrackTotalHits(true);
        String queryResult = getQueryResult(elasticsearchQueryParams);
        return ElasticsearchMapper.resultsToTotalHitsValue(queryResult);
    }


    private String getQueryResult(ElasticsearchQueryParams q) {
        String jsonQuery = ElasticsearchMapper.toJsonQueryString(q);
        log.debug("Query is: {}", jsonQuery);

        final String document = elasticsearchHttpService.getDocument(jsonQuery);
        log.debug("Document is: {}", sanitizeMessage(document));

        return document;
    }
}
