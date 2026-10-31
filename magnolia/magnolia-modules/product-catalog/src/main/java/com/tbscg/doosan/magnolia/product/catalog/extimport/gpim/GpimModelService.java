package com.tbscg.doosan.magnolia.product.catalog.extimport.gpim;

import com.google.common.collect.ImmutableList;
import com.tbscg.doosan.magnolia.product.catalog.extimport.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.text.WordUtils;

import javax.inject.Inject;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.Collections.singleton;
import static java.util.stream.Collectors.groupingBy;

@Slf4j
public class GpimModelService implements PimModelService {
    private static final String ROOT_PRODUCTS_CATEGORY_NAME = "compactNAPublic_BaseMachines";
    private static final String ROOT_ATTACHMENTS_CATEGORY_NAME = "compactNAPublic_Attachments";
    private static final String GPIM_UNNAMED = "no_gpim_name";
    private final GpimClient gpimClient;

    @Inject
    public GpimModelService(final GpimClientFactory gpimClientFactory) {
        this.gpimClient = gpimClientFactory.createGpimClient();
    }

    private static GpimProduct mapVariantCompatibleCarriers(final GpimProduct variant, final GpimProduct model) {
        if (variant.getCompatibleCarriers() == null || variant.getCompatibleCarriers().isEmpty()) {
            variant.setCompatibleCarriers(model.getCompatibleCarriers());
        }

        return variant;
    }

    private static boolean filterModelByHistorical(final GpimProduct product, final PimFilter filter) {
        boolean isSelectable = filter.isIncludeHistorical() || product.isActivePublic();
        log.debug("Filtering model by historical: {}, {} => {}", filter.isIncludeHistorical(), product.isActivePublic(), isSelectable);
        return isSelectable;
    }

    private static Stream<GpimProduct> flatMapVariants(GpimProduct product) {
        if (product == null) {
            return Stream.empty();
        }
        return Stream.concat(
                Stream.of(product),
                product.hasVariants() ?
                        product.getVariants().stream().map(v -> mapVariantCompatibleCarriers(v, product)) :
                        Stream.empty()
        );
    }

    @Override
    public PimCatalog getCatalog(final PimFilter filter, String locale) throws PimServiceException {
        log.debug("START: fetching GPim {} catalog (include historical: {}).", filter.getModelType(), filter.isIncludeHistorical());
        try {
            final String categoryName = filter.getModelType() == PimModelType.PRODUCT ?
                    ROOT_PRODUCTS_CATEGORY_NAME :
                    ROOT_ATTACHMENTS_CATEGORY_NAME;
            final GpimCategory rootCategory = getRootCategory(categoryName, locale);
            return GpimCatalog.builder()
                    .categories(
                            singleton(
                                    GpimCategory.builder()
                                            .name("Imported")
                                            .description("Temporary catalog for imported categories.")
                                            .subCategories(rootCategory.getSubCategories().stream()
                                                    .map(c -> this.getFullCategoryInfo(c, filter, locale))
                                                    .collect(Collectors.toList()))
                                            .build()))
                    .build();
        } catch (final Exception e) {
            throw new PimServiceException("Unhandled error when getting GPIM catalog information", e);
        } finally {
            log.debug("DONE: fetching GPim {} catalog.", filter.getModelType());
        }
    }

    private GpimCategory getRootCategory(String categoryName, String locale) throws PimServiceException {
        try {
            return gpimClient.getCategory(categoryName, locale);
        } catch (final GpimClientException e) {
            throw new PimServiceException("Failed to get category: '" + categoryName + "'", e);
        }
    }

    private GpimCategory getFullCategoryInfo(final GpimCategory gpimCategory, final PimFilter filter, String locale) {
        try {
            log.debug("START: fetching full category {}", gpimCategory.getCode());

            final GpimCategory fullCategory = gpimClient.getCategory(gpimCategory.getCode(), locale);

            log.debug("Defining product groups");
            final GpimCategory enhancedCategory = enhanceCategory(fullCategory, filter, locale);

            log.debug("DONE: fetching full category {}", gpimCategory.getCode());
            return enhancedCategory;
        } catch (final GpimClientException e) {
            log.error("Failed to fetch '{}' category", gpimCategory.getCode(), e);
            return gpimCategory;
        }
    }

    private Function<? super GpimProduct, String> getGroupingFunctionByType(PimFilter filter) {
        Function<String, String> reduceName = (name) -> {
            if (filter.getModelType() == PimModelType.PRODUCT) {
                return name.replaceAll("Bobcat ", "") // Remove Bobcat
                        .split(" ")[0]; // Get the first word
            }

            String reducedName = name
                    .replaceAll("^Bobcat ", "") // Remove Bobcat from the beginning
                    .replaceAll("\\b\\(.*\\)\\b", "") // Remove anything inside parenthesis
                    .replaceAll("[^A-Za-z0-9\\s,]", "") // Remove any other character but letters, numbers, spaces or commas
                    .replaceAll(",.*", "") // Remove anything after a comma
                    .replaceAll("\\b(\\S*\\d\\S*)\\b", "") // Remove any word containing a number
                    .replaceAll("\\s+", " ") // Collapse multiple spaces into single one
                    .trim();
            // There are cases where the full name is just a simple word with numbers and letters, and with the previous
            // filtering they get reduced to an empty string. To prevent that, just defaults to the original product name.
            return WordUtils.capitalizeFully(
                    StringUtils.defaultIfEmpty(reducedName, name));
        };

        return (product) -> {
            if (product.getName() != null) {
                return reduceName.apply(product.getName());
            }
            return GPIM_UNNAMED + "#" + product.getCode();
        };
    }

    private GpimCategory enhanceCategory(final GpimCategory gpimCategory, final PimFilter filter, String locale) {
        log.debug("Enhancing category {}", gpimCategory.getName());
        ImmutableList.Builder<GpimProduct> productsBuilder = ImmutableList.builder();
        ImmutableList.Builder<GpimProductGroup> groupsBuilder = ImmutableList.builder();

        log.debug("Defining a map with the different variants");
        Map<String, List<GpimProduct>> gpimProductsMap = gpimCategory.getProducts().stream()
                .filter(p -> filterModelByHistorical(p, filter))
                .flatMap(GpimModelService::flatMapVariants)
                .collect(groupingBy(getGroupingFunctionByType(filter)));

        log.debug("Trying to generate the product groups for products with more than one variant");
        gpimProductsMap.forEach((groupName, products) -> {
            if (filter.getModelType() == PimModelType.ATTACHMENT) {
                products.stream().forEach(product -> {
                    try {
                        if (product.getCompatibleCarriers() != null && !product.getCompatibleCarriers().isEmpty())
                            return;
                        product.setCompatibleCarriers(gpimClient.getProduct(product.getCode()).getCompatibleCarriers());
                    } catch (Exception e) {
                        log.error("Failed to fetch compatible product for '{}'", product.getCode());
                    }
                });
            }

            switch (products.size()) {
                case 0:// Do nothing as there are no valid products
                    break;
                case 1: // Only one product
                    productsBuilder.add(products.get(0));
                    break;
                default: // More than one product, so group them

                    log.debug("Creating new product group: " + groupName);
                    groupsBuilder.add(GpimProductGroup.builder()
                            .groupName(groupName)
                            .products(products)
                            .build());
            }
        });

        List<GpimCategory> enhancedSubcategories = CollectionUtils.isEmpty(gpimCategory.getSubCategories()) ?
                Collections.emptyList() :
                gpimCategory.getSubCategories().stream()
                        .map(c -> this.getFullCategoryInfo(c, filter, locale))
                        .collect(Collectors.toList());

        return gpimCategory.toBuilder()
                .products(productsBuilder.build())
                .productGroups(groupsBuilder.build())
                .subCategories(enhancedSubcategories)
                .build();
    }
}
