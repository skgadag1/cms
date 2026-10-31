package com.tbscg.doosan.magnolia.product.catalog.field;

import com.fasterxml.jackson.databind.JsonNode;
import com.tbscg.doosan.magnolia.product.catalog.field.product.ProductApiRequest;
import com.tbscg.doosan.magnolia.product.catalog.field.product.ProductSpecification;
import com.tbscg.doosan.magnolia.product.catalog.field.product.ProductSpecificationTemplate;
import com.tbscg.doosan.magnolia.product.catalog.field.service.ProductApiService;
import com.vaadin.shared.ui.ContentMode;
import com.vaadin.ui.Component;
import com.vaadin.ui.Label;
import com.vaadin.ui.Layout;
import info.magnolia.ui.datasource.jcr.JcrNodeWrapper;
import info.magnolia.ui.editor.FormDefinition;
import info.magnolia.ui.editor.FormView;
import info.magnolia.ui.editor.LocaleContext;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import javax.inject.Inject;
import javax.jcr.RepositoryException;
/**
 * Composite field used to parse JSON data from REST endpoint and generate corresponding Vaadin fields on the form layout.
 *
 * @param <T> FormView Type
 */
@Slf4j
public class ProductCompositeField<T> extends FormView<T> {
    private final LocaleContext localeContext;
    private final FormDefinition<T> formDefinition;
    private final ProductApiService productApiService;

    @Inject
    public ProductCompositeField(final FormDefinition<T> formDefinition,
                                 final LocaleContext localeContext,
                                 final ProductApiService productApiService) {
        super(formDefinition, localeContext);
        this.localeContext = localeContext;
        this.formDefinition = formDefinition;
        this.productApiService = productApiService;
    }

    @Override
    public void applyDefaults() {
        super.applyDefaults();
        final Label infoLabel = new Label("Displaying model specifications is not available when adding a model.");
        Component[] jsonFields = new Component[]{infoLabel};
        final Layout layout = (Layout) super.asVaadinComponent();
        layout.addComponents(jsonFields);
    }

    @Override
    public void populate(T item) {
        super.populate(item);
        JcrNodeWrapper jcrItem = (JcrNodeWrapper) item;
        final Layout layout = (Layout) super.asVaadinComponent();
        try {
            if (!jcrItem.hasProperty("id") || StringUtils.isBlank(jcrItem.getProperty("id").getString())) {
                final Label emptyIdLabel = new Label("Model ID is empty.", ContentMode.HTML);
                layout.addComponents(emptyIdLabel);
                return;
            }
            final String productId = jcrItem.getProperty("id").getString();
            final String productDetailsDataSource = jcrItem.hasProperty("productDetailsDataSource") ? jcrItem .getProperty("productDetailsDataSource").getString() : "default";
            final String region = getRegionFromPath(jcrItem.getPath());
            final Integer specLevel = ((ProductCompositeFieldDefinition<T>) formDefinition).getSpecLevel() != null ?
                    ((ProductCompositeFieldDefinition<T>) formDefinition).getSpecLevel() : 3;
            final String businessUnit = getBusinessUnitByRegion(region);
            if (ProductApiService.PIM_BUSINESS_UNIT_OTHER.equals(businessUnit)) {
                layout.addComponents(new Label("PIM specifications are only available for NA, EMEA, ALAO regions.", ContentMode.HTML));
            } else {
                final String nodeType = jcrItem.getPrimaryNodeType().getName();
                // Node type should be like 'dxp:{modelType}', so it matches model type, see link below
                // https://github.com/DoosanICA/db-api-product/blob/b07ceb5c5f890524f166eaee6fb2e85c7ff2e62b/src/main/pim/model.ts#L49
                final String modelType = StringUtils.removeStart(nodeType, "dxp:");
                layout.addComponents(addJsonComponents(productId, businessUnit, specLevel, modelType, region, productDetailsDataSource));
            }
        } catch (RepositoryException e) {
            final String message = "Failed to get model ID.";
            log.error(message, e);
            layout.addComponents(createErrorComponent(message));
        }
    }

    private String getRegionFromPath(final String path) throws RepositoryException {
        return productApiService.getRegionFromPath(path);
    }

    private String getBusinessUnitByRegion(final String region) {
        return productApiService.getBusinessUnitByRegion(region);
    }

    private String getLocale(final String region) {
        return productApiService.getLocale(region, localeContext);
    }

    private Component[] addJsonComponents(final String productId, final String businessUnit, final Integer specLevel, final String modelType, final String region, final String productDetailsDataSource) {
        ProductApiRequest request = ProductApiRequest.builder()
                .productId(productId)
                .businessUnit(businessUnit)
                .locale(getLocale(region))
                .specLevel(specLevel)
                .modelType(modelType)
                .region(region)
                .productDetailsDataSource(productDetailsDataSource)
                .instance("author")
                .build();

        JsonNode responseBody = productApiService.fetchProductSpecifications(request);

        if (responseBody == null) {
            final String message = "Failed to get model data.";
            log.error(message);
            return createErrorComponent(message);
        }

        try {
            final ProductSpecification productSpecs = productApiService.mapProductSpecification(responseBody);
            if (productSpecs == null) {
                final String message = "Couldn't read model data.";
                log.error(message);
                return createErrorComponent(message);
            }
            final ProductSpecificationTemplate specsTemplate = new ProductSpecificationTemplate(productSpecs);
            return specsTemplate.getComponents();
        } catch (Exception e) {
            final String message = "Couldn't read model data.";
            log.error(message, e);
            return createErrorComponent(message);
        }
    }

    private Component[] createErrorComponent(final String message) {
        final Label errorLabel = new Label(message, ContentMode.HTML);
        errorLabel.addStyleName("validation-display");
        return new Component[]{errorLabel};
    }

}
