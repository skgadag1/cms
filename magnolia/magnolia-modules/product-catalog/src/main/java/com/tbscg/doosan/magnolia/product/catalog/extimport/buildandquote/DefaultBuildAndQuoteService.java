package com.tbscg.doosan.magnolia.product.catalog.extimport.buildandquote;

import com.google.common.base.Suppliers;
import lombok.extern.slf4j.Slf4j;

import javax.inject.Inject;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

import static java.util.stream.Collectors.groupingBy;

@Slf4j
public class DefaultBuildAndQuoteService implements BuildAndQuoteService {

    private static final int CATEGORIES_CACHE_TTL_MS = 300000;

    private final BuildAndQuoteClient buildAndQuoteClient;
    private final Supplier<Map<String, List<BuildAndQuoteCategory>>> categoryMappingSupplier =
            Suppliers.memoizeWithExpiration(
                    this::buildCategoriesMapping,
                    CATEGORIES_CACHE_TTL_MS,
                    TimeUnit.MILLISECONDS);

    @Inject
    public DefaultBuildAndQuoteService(final BuildAndQuoteClient buildAndQuoteClient) {
        if (buildAndQuoteClient == null) {
            throw new IllegalArgumentException("buildAndQuoteClient is null");
        }
        this.buildAndQuoteClient = buildAndQuoteClient;
    }

    @Override
    public Optional<BuildAndQuoteCategory> getCategoryByName(final String categoryName) {
        return Optional.ofNullable(getCategoryMapping().get(categoryName))
                .map(cList -> cList.isEmpty() ? null : cList.get(0));
    }

    private Map<String, List<BuildAndQuoteCategory>> getCategoryMapping() {
        return categoryMappingSupplier.get();
    }

    private Map<String, List<BuildAndQuoteCategory>> buildCategoriesMapping() {
        final Map<String, List<BuildAndQuoteCategory>> map =
                buildAndQuoteClient.getCategoryList().stream()
                        .collect(groupingBy(BuildAndQuoteCategory::getProdCategoryName));

        if (log.isWarnEnabled()) {
            inspectCategoryMapping(map);
        }
        return map;
    }

    private static void inspectCategoryMapping(final Map<String, List<BuildAndQuoteCategory>> categoryMapping) {
        categoryMapping.forEach((key, list) -> {
            if (list.isEmpty()) {
                log.warn("No entries for category name: {}", key);
            } else if (list.size() > 1) {
                log.warn("Category {} has {} entries. First will be picked.", key, list.size());
            }
        });
    }

}
