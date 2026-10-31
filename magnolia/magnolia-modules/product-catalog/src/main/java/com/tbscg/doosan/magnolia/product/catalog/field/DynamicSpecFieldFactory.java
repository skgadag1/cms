package com.tbscg.doosan.magnolia.product.catalog.field;

import com.fasterxml.jackson.databind.JsonNode;
import com.tbscg.doosan.magnolia.product.catalog.field.product.ProductApiRequest;
import com.tbscg.doosan.magnolia.product.catalog.field.product.ProductSpec;
import com.tbscg.doosan.magnolia.product.catalog.field.product.ProductSpecification;
import com.tbscg.doosan.magnolia.product.catalog.field.service.ProductApiService;
import com.vaadin.ui.CheckBoxGroup;
import com.vaadin.ui.Component;
import com.vaadin.ui.Notification;
import info.magnolia.context.MgnlContext;
import info.magnolia.objectfactory.ComponentProvider;
import info.magnolia.ui.ValueContext;
import info.magnolia.ui.field.factory.AbstractFieldFactory;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import javax.inject.Inject;
import javax.jcr.Item;
import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import java.util.*;

@Slf4j
public class DynamicSpecFieldFactory extends AbstractFieldFactory<Set<ProductSpec>, DynamicSpecFieldDefinition> {

    private static final String AVAILABLE_ATTACHMENTS = "Available Attachments";
    private static final String MANUAL_KEY_SPECIFICATIONS = "manualKeySpecifications";
    private static final String PROPERTY_NAME_PROPERTY = "propertyName";
    private static final String PROPERTY_VALUE_PROPERTY = "propertValue";
    private static final String PROPERTY_UNIT_PROPERTY = "propertyUnit";
    private static final String MODEL_PROPERTY = "model";
    private static final String ID_PROPERTY = "id";

    private final ValueContext<Item> valueContext;
    private final ProductApiService productApiService;

    @Inject
    public DynamicSpecFieldFactory(DynamicSpecFieldDefinition definition,
                                   ComponentProvider componentProvider,
                                   ValueContext<Item> valueContext,
                                   ProductApiService productApiService) {
        super(definition, componentProvider);
        this.valueContext = valueContext;
        this.productApiService = productApiService;
    }

    @Override
    public Component createFieldComponent() {
        CheckBoxGroup<ProductSpec> defaultCheckBoxGroup = new CheckBoxGroup<>("Select up to " + definition.getLimitedOptions() + " options:");
        defaultCheckBoxGroup.setItemCaptionGenerator(ProductSpec::getLabel);

        try {
            Node currentNode = (Node) valueContext.getSingleOrThrow();
            Node pageNode = findPageNodeWithModel(currentNode);
            if (pageNode == null) {
                log.warn("Could not find page node with 'model' property starting from: {}", currentNode.getPath());
                return defaultCheckBoxGroup;
            }

            String modelValue = pageNode.getProperty(MODEL_PROPERTY).getString();
            Node productNode = findProductNodeByModel(modelValue);
            if (productNode == null) {
                log.warn("Could not find product node for model: {}", modelValue);
                return defaultCheckBoxGroup;
            }

            if (productNode.getPrimaryNodeType().getName().equals("dxp:productGroup")) {
                log.warn("Product node is a product group, not a product: {}", productNode.getPath());
                return defaultCheckBoxGroup;
            }

            String productId = productNode.hasProperty(ID_PROPERTY) ? productNode.getProperty(ID_PROPERTY).getString() : modelValue;
            String region = productApiService.getRegionFromPath(pageNode.getPath());
            String businessUnit = productApiService.getBusinessUnitByRegion(region);

            log.info("Creating spec checkbox group for productId: {}, businessUnit: {}, region: {}", productId, businessUnit, region);

            CheckBoxGroup<ProductSpec> checkBoxGroup =
                    createSpecCheckboxGroup(productId, businessUnit, region, productNode);

            checkBoxGroup.setItemCaptionGenerator(ProductSpec::getLabel);
            return checkBoxGroup;

        } catch (RepositoryException | IllegalArgumentException e) {
            log.error("Failed to load JCR data or invalid input", e);
            return defaultCheckBoxGroup;
        }
    }

    private CheckBoxGroup<ProductSpec> createSpecCheckboxGroup(String productId, String businessUnit, String region, Node productNode) {
        if (ProductApiService.PIM_BUSINESS_UNIT_EMEA.equalsIgnoreCase(businessUnit)) {
            return createSpecCheckboxGroupFromModelNode(productNode);
        }

        ProductApiRequest request = ProductApiRequest.builder()
                .productId(productId)
                .businessUnit(businessUnit)
                .locale(productApiService.getLocale(region))
                .specLevel(2)
                .modelType("product")
                .region(region)
                .productDetailsDataSource("default")
                .instance("public")
                .build();

        JsonNode jsonNode = productApiService.fetchProductSpecifications(request);

        if (jsonNode == null) {
            Notification.show("Error loading options", Notification.Type.ERROR_MESSAGE);
            return new CheckBoxGroup<>();
        }

        return parseOptionsCheckboxGroup(jsonNode);
    }

    private CheckBoxGroup<ProductSpec> createSpecCheckboxGroupFromModelNode(Node productNode) {
        List<ProductSpec> options = new ArrayList<>();

        try {
            if (productNode.hasNode(MANUAL_KEY_SPECIFICATIONS)) {
                Node specsNode = productNode.getNode(MANUAL_KEY_SPECIFICATIONS);
                extractSpecsFromNode(specsNode, options);
            }
        } catch (RepositoryException e) {
            log.error("Failed to load specs from Model node", e);
        }

        if (options.stream().noneMatch(opt -> AVAILABLE_ATTACHMENTS.equals(opt.getLabel()))) {
            options.add(new ProductSpec(AVAILABLE_ATTACHMENTS, StringUtils.EMPTY, StringUtils.EMPTY));
        }

        return getCheckBoxGroup(options);
    }

    private void extractSpecsFromNode(Node node, List<ProductSpec> options) throws RepositoryException {
        if (node.hasProperty(PROPERTY_NAME_PROPERTY)) {

            String name = node.getProperty(PROPERTY_NAME_PROPERTY).getString();

            String value = node.hasProperty(PROPERTY_VALUE_PROPERTY)
                    ? node.getProperty(PROPERTY_VALUE_PROPERTY).getString()
                    : StringUtils.EMPTY;

            String unit = node.hasProperty(PROPERTY_UNIT_PROPERTY)
                    ? node.getProperty(PROPERTY_UNIT_PROPERTY).getString()
                    : StringUtils.EMPTY;

            options.add(new ProductSpec(name, value, unit));
        }

        if (node.hasNodes()) {
            NodeIterator nodeIterator = node.getNodes();
            while (nodeIterator.hasNext()) {
                extractSpecsFromNode(nodeIterator.nextNode(), options);
            }
        }
    }

    private CheckBoxGroup<ProductSpec> parseOptionsCheckboxGroup(JsonNode json) {
        List<ProductSpec> options = new ArrayList<>();

        ProductSpecification productSpecs = productApiService.mapProductSpecification(json);

        if (productSpecs != null && productSpecs.getSections() != null) {
            productSpecs.getSections().stream()
                    .filter(section -> section.getProperties() != null)
                    .flatMap(section -> section.getProperties().stream())
                    .map(p -> new ProductSpec(p.getLabel(), p.getValue().getStringValue(), p.getUnit()))
                    .forEach(options::add);
        }

        if (options.stream().noneMatch(opt -> AVAILABLE_ATTACHMENTS.equals(opt.getLabel()))) {
            options.add(new ProductSpec(AVAILABLE_ATTACHMENTS, StringUtils.EMPTY, StringUtils.EMPTY));
        }

        return getCheckBoxGroup(options);
    }

    private CheckBoxGroup<ProductSpec> getCheckBoxGroup(List<ProductSpec> options) {
        CheckBoxGroup<ProductSpec> checkBoxGroup = new CheckBoxGroup<>("Select up to " + definition.getLimitedOptions() + " options:");
        checkBoxGroup.setItems(options);
        checkBoxGroup.setItemCaptionGenerator(ProductSpec::getLabel);

        Set<ProductSpec> initialValue = new HashSet<>();
        options.stream()
                .filter(opt -> AVAILABLE_ATTACHMENTS.equals(opt.getLabel()))
                .findFirst()
                .ifPresent(initialValue::add);

        checkBoxGroup.setValue(initialValue);

        checkBoxGroup.addValueChangeListener(event -> {
            try {
                Set<ProductSpec> newValue = new HashSet<>(event.getValue());
                if (newValue.size() > definition.getLimitedOptions()) {
                    checkBoxGroup.setValue(initialValue);
                } else {
                    initialValue.clear();
                    initialValue.addAll(newValue);
                }
            } catch (RuntimeException e) {
                log.error("Error handling value change", e);
                checkBoxGroup.setValue(initialValue);
            }
        });

        return checkBoxGroup;
    }

    private Node findPageNodeWithModel(Node startNode) throws RepositoryException {
        Node currentNode = startNode;

        while (currentNode != null) {
            if (currentNode.hasProperty(MODEL_PROPERTY) && StringUtils.isNotBlank(currentNode.getProperty(MODEL_PROPERTY).getString())) {
                return currentNode;
            }

            if ("mgnl:page".equals(currentNode.getPrimaryNodeType().getName()) && currentNode.hasProperty(MODEL_PROPERTY)) {
                return currentNode;
            }

            if ("/".equals(currentNode.getPath())) {
                break;
            }
            currentNode = currentNode.getParent();
        }

        return null;
    }

    private Node findProductNodeByModel(String modelValue) {
        try {
            Session productSession = MgnlContext.getJCRSession("products");

            if (modelValue.startsWith("/")) {
                try {
                    return productSession.getNode(modelValue);
                } catch (RepositoryException e) {
                    log.debug("Direct path lookup failed for model: {}", modelValue);
                }
            }

            if (modelValue.matches("[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}")) {
                try {
                    return productSession.getNodeByIdentifier(modelValue);
                } catch (RepositoryException e) {
                    log.debug("UUID lookup failed for model: {}", modelValue);
                }
            }

            log.warn("Could not find product node for model value: {}. Consider implementing property-based search.", modelValue);

        } catch (RepositoryException e) {
            log.error("Error accessing product workspace", e);
        }

        return null;
    }
}
