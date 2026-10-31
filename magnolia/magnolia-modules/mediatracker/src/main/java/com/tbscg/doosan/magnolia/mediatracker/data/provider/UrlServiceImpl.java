package com.tbscg.doosan.magnolia.mediatracker.data.provider;

import com.tbscg.doosan.magnolia.mediatracker.data.model.UrlFilters;
import com.tbscg.doosan.magnolia.mediatracker.data.model.VanityDataSearchResult;
import com.tbscg.doosan.magnolia.mediatracker.data.model.VanityDomain;
import com.tbscg.doosan.magnolia.mediatracker.data.model.VanityUrl;
import com.tbscg.doosan.magnolia.mediatracker.data.provider.dynamodb.DynamoDbService;
import com.tbscg.doosan.magnolia.mediatracker.data.provider.elasticsearch.ElasticsearchService;
import com.vaadin.data.provider.Query;
import lombok.extern.slf4j.Slf4j;
import org.apache.logging.log4j.util.Strings;

import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

/**
 * Class implementing back end data providers for Url
 */
@Slf4j
public class UrlServiceImpl extends BaseMediatrackerDataProvider<VanityUrl, UrlFilters> implements VanityUrlService {

    public UrlServiceImpl(ElasticsearchService elasticsearchService, DynamoDbService dynamoDbService) {
        super(elasticsearchService, dynamoDbService);
    }

    /**
     * Gets url by code
     *
     * @param code value of code parameter
     * @return matching url
     */
    @Override
    public VanityUrl getUrl(String code) {
        if (Strings.isNotBlank(code)) {
            log.debug("Get url with Code: {}", code);
            VanityUrl url = elasticsearchService.getUrl(code);
            return url;
        } else {
            log.error("Code parameter cant be empty");
        }
        return null;
    }

    /**
     * Gets url by domain code
     *
     * @param domainCode value of domain code parameter
     * @return matching list of urls
     */
    @Override
    public List<VanityUrl> getUrlsByDomainCode(String domainCode) {
        if (Strings.isNotBlank(domainCode)) {
            log.debug("Get urls with domainCode: {}", domainCode);
            List<VanityUrl> urls = elasticsearchService.getUrlsByDomainCode(domainCode);
            return urls;
        } else {
            log.error("Domain code parameter cant be empty");
        }
        return null;
    }

    /**
     * Checks if there already exist url with given path
     *
     * @param url url to check
     * @return false if url with given path has already been defined
     */
    @Override
    public boolean isUrlPathNotTaken(VanityUrl url) {
        String domainCode = Optional.ofNullable(url.getVanityDomain())
                .orElse(new VanityDomain()).getCode();
        VanityUrl existingUrl = elasticsearchService.getUrlByPath(url.getPath(), domainCode, true);
        return existingUrl == null || existingUrl.getCode().equals(url.getCode());
    }

    /**
     * Creates new or replaces if already exists given URL
     *
     * @param url url object to save
     */
    @Override
    public void createNewOrReplace(VanityUrl url) {
        log.debug("URL SAVE: {} / {}", url.getPath(), url.getCode());
        dynamoDbService.add(url);
    }

    /**
     * Deletes url by code
     * @param code value of code parameter
     */
    @Override
    public void deleteUrl(String code) {
        log.debug("URL DELETE: {}", code);
        dynamoDbService.deleteUrl(code);
    }

    @Override
    protected Stream<VanityUrl> fetchFromBackEnd(Query<VanityUrl, UrlFilters> query) {
        log.debug("fetching urls from backend");
        if (query.getFilter().isPresent()) {
            List<VanityUrl> results = elasticsearchService.getUrls(query.getFilter().get());
            return results.stream();
        }
        return null;
    }

    @Override
    protected VanityDataSearchResult<VanityUrl> fetchFromBackendWithPagination(Query<VanityUrl, UrlFilters> query) {
        if (query.getFilter().isPresent()) {
            log.debug("fetching domains from backend with pagination");
            return elasticsearchService.getUrlsWithPagination(query.getFilter().get());
        }
        return emptyResults;
    }
}
