package com.tbscg.doosan.magnolia.mediatracker.data.provider;

import com.tbscg.doosan.magnolia.mediatracker.data.model.BaseVanityData;
import com.tbscg.doosan.magnolia.mediatracker.data.model.MediatrackerFilters;
import com.tbscg.doosan.magnolia.mediatracker.data.model.StatusChanged;
import com.tbscg.doosan.magnolia.mediatracker.data.model.VanityDataSearchResult;
import com.tbscg.doosan.magnolia.mediatracker.data.provider.dynamodb.DynamoDbService;
import com.tbscg.doosan.magnolia.mediatracker.data.provider.elasticsearch.ElasticsearchService;
import com.vaadin.data.provider.AbstractBackEndDataProvider;
import com.vaadin.data.provider.Query;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Base class for implementing back end data providers for Url and Domain
 *
 * @param <T> data provider data type extending BaseVanityData
 * @param <F> data provider filter type extending MediatrackerFilters
 */
@Slf4j
public abstract class BaseMediatrackerDataProvider<T extends BaseVanityData, F extends MediatrackerFilters> extends AbstractBackEndDataProvider<T, F> {

    private static final long MAX_RETRIES = 15;

    protected ElasticsearchService elasticsearchService;
    protected DynamoDbService dynamoDbService;

    public BaseMediatrackerDataProvider(ElasticsearchService elasticsearchService, DynamoDbService dynamoDbService) {
        this.elasticsearchService = elasticsearchService;
        this.dynamoDbService = dynamoDbService;
    }

    protected abstract VanityDataSearchResult<T> fetchFromBackendWithPagination(Query<T, F> query);

    protected final VanityDataSearchResult<T> emptyResults = new VanityDataSearchResult<>(Collections.emptyList(), 0);
    @Override
    protected int sizeInBackEnd(Query<T, F> query) {
        int size = elasticsearchService.getTotalHitsValue(query.getFilter().get());
        log.debug("Items total: {}", size);
        return size;
    }

    @Override
    public Stream<T> fetch(Query<T, F> query) {
        Optional<F> filters = query.getFilter();
        if (filters.isPresent() && filters.get().getStatusChanged() != null) {
            return retry(query,
                    MAX_RETRIES,
                    res -> createRetryPredicates(res, filters.get().getStatusChanged()).test(res),
                    (q -> this.fetchFromBackEnd(q).collect(Collectors.toList()))).stream();
        } else {
            return super.fetch(query);
        }
    }

    public VanityDataSearchResult<T> fetchWithPagination(Query<T, F> query) {
        Optional<F> filters = query.getFilter();
        if (filters.isPresent() && filters.get().getStatusChanged() != null) {
            return retry(query,
                    MAX_RETRIES,
                    res -> createRetryPredicates(res.getItems(), filters.get().getStatusChanged()).test(res.getItems()),
                    q -> fetchFromBackendWithPagination(q));
        } else {
            return this.fetchFromBackendWithPagination(query);
        }
    }

    private <S> S retry(Query<T, F> query, Long maxRetries, Predicate<S> condition, Function<Query<T, F>, S> fn) {
        S result;
        Long current = maxRetries;
        Long currentDelay;
        do {
            currentDelay = (maxRetries - current) * 200 + 500;
            try {
                Thread.sleep(currentDelay);
            } catch (InterruptedException e) {
                log.error("Error in retry: ", e.getMessage());
            }
            result = fn.apply(query);
            current--;
        } while (condition.test(result) && current > 0);

        return result;
    }

    private Predicate<List<T>> createRetryPredicates(List<T> list, StatusChanged statusChanged) {
        Predicate<List<T>> conditionForDeleted = res -> list.stream()
                .filter(obj -> StringUtils.equals(obj.getCode(), statusChanged.getCode()))
                .count() > 0;

        Predicate<List<T>> conditionForSaved = res -> list.stream()
                .filter(obj -> StringUtils.equals(obj.getCode(), statusChanged.getCode()))
                .count() == 0;

        Predicate<List<T>> conditionForUpdated = res -> list.stream()
                .filter(obj -> StringUtils.equals(obj.getCode(), statusChanged.getCode()))
                .findFirst()
                .filter(obj -> obj.getModifiedTime() != null && obj.getModifiedTime().isBefore(statusChanged.getLastUpdatedTime()))
                .isPresent();
        return statusChanged.isDeleted() ? conditionForDeleted : conditionForSaved.or(conditionForUpdated);
    }
}
