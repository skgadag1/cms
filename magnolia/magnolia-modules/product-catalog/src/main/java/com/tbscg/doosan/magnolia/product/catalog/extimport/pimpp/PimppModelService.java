package com.tbscg.doosan.magnolia.product.catalog.extimport.pimpp;

import com.google.common.collect.ImmutableList;
import com.tbscg.doosan.magnolia.product.catalog.extimport.PimCatalog;
import com.tbscg.doosan.magnolia.product.catalog.extimport.PimFilter;
import com.tbscg.doosan.magnolia.product.catalog.extimport.PimModelService;
import com.tbscg.doosan.magnolia.product.catalog.extimport.PimServiceException;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Slf4j
public class PimppModelService implements PimModelService {

    public static final String CURRENT_STATUS = "current";
    public static final String ATTACHMENT_TYPE = "attachment";
    private final PimppClient pimppClient;

    @Inject
    public PimppModelService(final PimppClientFactory pimppClientFactory) {
        this.pimppClient = pimppClientFactory.createPimppClient();
    }

    @Override
    public PimCatalog getCatalog(final PimFilter filter, final String locale) throws PimServiceException {
        try {
            final PimppCatalog catalog = pimppClient.getCatalog();
            List<PimppCategory> enhancedCategories = enhaceCategories(
                    () -> catalog.getCategories().stream()
                            .filter(c -> this.filterCategoryByType(c, filter))
                            .collect(Collectors.toList()),
                    filter)
                    .peek(c -> c.setName("Imported-" + c.getName()))
                    .collect(Collectors.toList());
            return catalog.toBuilder()
                    .categories(enhancedCategories)
                    .build();
        } catch (final PimppClientException e) {
            throw new PimServiceException("Failed to fetch data from PIM++.", e);
        }
    }

    private Stream<PimppCategory> enhaceCategories(
            final Supplier<List<PimppCategory>> supplier,
            final PimFilter filter) {
        log.debug("Enhancing categories...");
        List<PimppCategory> categories = supplier.get();
        if (CollectionUtils.isEmpty(categories)) {
            return Stream.of();
        }
        return categories.stream()
                .map(c -> this.enhanceCategory(c, filter))
                .filter(this::filterEmptyCategories);
    }

    private boolean filterEmptyCategories(final PimppCategory category) {
        boolean isEmpty = category == null ||
                CollectionUtils.isEmpty(category.getSubCategories()) &&
                        CollectionUtils.isEmpty(category.getProducts()) &&
                        CollectionUtils.isEmpty(category.getProductGroups());
        log.debug("Filtering empty category {}: isEmpty = {} ==> include = {}",
                Optional.ofNullable(category).map(PimppCategory::getName).orElse("<null>"),
                isEmpty,
                !isEmpty);
        return !isEmpty;
    }

    private void ensureVariantExists(PimppModel model) {
        if (!model.getType().equals(ATTACHMENT_TYPE) || (model.getVariants() != null && !model.getVariants().isEmpty())) {
            return;
        }

        final PimppModelVariant variant = new PimppModelVariant();
        variant.setId(model.getId());
        variant.setName(model.getName());
        variant.setStatus(model.getStatus());

        model.setVariants(List.of(variant));
    }

    private PimppCategory enhanceCategory(final PimppCategory category, final PimFilter filter) {
        log.debug("Enhancing category {}", category.getName());
        ImmutableList.Builder<PimppProduct> productsBuilder = ImmutableList.builder();
        ImmutableList.Builder<PimppProductGroup> groupsBuilder = ImmutableList.builder();

        if (category.getModels() != null) {
            log.debug("Procesing models for category {}", category.getName());
            category.getModels().stream()
                    .filter(m -> this.filterByHistorical(m, filter))
                    .forEach(model -> {
                        ensureVariantExists(model);

                        List<PimppModelVariant> variants = model.getVariants().stream()
                                .filter(v -> this.filterByHistorical(v, filter))
                                .collect(Collectors.toList());
                        log.debug("Variants found for model {}: {}", model.getName(), variants.size());
                        switch (variants.size()) {
                            case 0: // Do nothing as there are no valid variants
                                break;
                            case 1: // Only one variant, so add it as a product by itself
                                variants.stream()
                                        .map(v -> this.generateProductFromModelAndVariant(model, v))
                                        .findFirst()
                                        .ifPresent(productsBuilder::add);
                                break;
                            default: // More than one variant, so create a group
                                log.debug("Product group to be created for model {}", model.getName());
                                groupsBuilder.add(PimppProductGroup.builder()
                                        .groupName(model.getName())
                                        .products(variants.stream()
                                                .map(v -> this.generateProductFromModelAndVariant(model, v))
                                                .collect(Collectors.toList()))
                                        .build());

                        }
                    });
        }

        List<PimppCategory> enhacedSucategories = enhaceCategories(() -> category.getSubCategories(), filter)
                .collect(Collectors.toList());
        return category.toBuilder()
                .products(productsBuilder.build())
                .productGroups(groupsBuilder.build())
                .subCategories(enhacedSucategories)
                .build();
    }

    private PimppProduct generateProductFromModelAndVariant(final PimppModel model, final PimppModelVariant variant) {
        log.debug("Generating product for model [{}] - variant [{}]", model.getName(), variant.getName());
        return PimppProduct.builder()
                .name(model.getName())
                .status(variant.getStatus())
                .code(PimppUtils.parsePMI(variant.getName(), variant.getId()))
                .build();
    }

    private boolean filterByHistorical(final PimppModel model, final PimFilter filter) {
        if (model == null) {
            return false;
        }
        boolean include = filter.isIncludeHistorical() || CURRENT_STATUS.equalsIgnoreCase(model.getStatus());
        log.debug("Filtering model {} by historical (include = {}) -> status: {} ==>> include = {}",
                model.getName(), filter.isIncludeHistorical(), model.getStatus(), include);
        return include;
    }

    private boolean filterByHistorical(final PimppModelVariant variant, final PimFilter filter) {
        if (variant == null) {
            return false;
        }
        boolean include = filter.isIncludeHistorical() || CURRENT_STATUS.equalsIgnoreCase(variant.getStatus());
        log.debug("Filtering variant {} by historical (include = {}) -> status: {} ==>> include = {}",
                variant.getName(), filter.isIncludeHistorical(), variant.getStatus(), include);

        return include;
    }

    private boolean filterCategoryByType(final PimppCategory category, final PimFilter filter) {
        if (category == null) {
            return false;
        }
        log.debug("Filtering category {} by type {}", category.getName(), filter.getModelType());
        switch (filter.getModelType()) {
            case ATTACHMENT:
                return "Attachments".equalsIgnoreCase(category.getName());
            case PRODUCT:
                return !"Attachments".equalsIgnoreCase(category.getName());
            default: // In case a new type is added
                throw new RuntimeException("Filtering by unknown type disabled");
        }
    }
}
