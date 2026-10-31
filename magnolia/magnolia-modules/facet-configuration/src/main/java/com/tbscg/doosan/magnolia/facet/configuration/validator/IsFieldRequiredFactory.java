package com.tbscg.doosan.magnolia.facet.configuration.validator;

import com.vaadin.data.ValidationResult;
import com.vaadin.data.Validator;
import com.vaadin.data.ValueContext;
import com.vaadin.ui.*;
import info.magnolia.ui.field.AbstractFieldValidatorFactory;
import info.magnolia.ui.framework.layout.field.FieldLayoutComponent;

import javax.inject.Inject;
import java.util.Optional;
import java.util.stream.StreamSupport;

public class IsFieldRequiredFactory extends AbstractFieldValidatorFactory<IsFieldRequiredDefinition, String> {

    private final String facetTypeFieldName;
    private final String facetProviderFieldName;
    private final String tagFacet;
    private final String facetProvider;

    @Inject
    public IsFieldRequiredFactory(IsFieldRequiredDefinition definition) {
        super(definition);
        facetTypeFieldName = definition.getFacetTypeFieldName();
        facetProviderFieldName = definition.getFacetProviderFieldName();
        tagFacet = definition.getTagFacet();
        facetProvider = definition.getFacetProvider();
    }

    @Override
    public Validator<String> createValidator() {
        return (value, ctx) -> {
            String facetType = getComponentValue(ctx, facetTypeFieldName);
            boolean isManualProvider = isManualProvider(ctx, facetProviderFieldName);

            if (value.isEmpty() && isManualProvider){
                return ValidationResult.error("Field is required");
            }
            if (!isManualProvider && !facetType.equals(tagFacet)) {
                return ValidationResult.error("List provider is available only for tag facet type!");
            }
            return ValidationResult.ok();
        };
    }

    private FormLayout getFormLayout(Component component) {
        if(component instanceof FormLayout) {
            return (FormLayout) component;
        } else if (component.getParent() == null) {
            return null;
        }
        return getFormLayout(component.getParent());
    }

    private Optional<Component> getComponentByName(ValueContext ctx, String name) {
        return ctx.getComponent().flatMap(component -> StreamSupport.stream(getFormLayout(component).spliterator(), false)
                .filter(field -> name.equalsIgnoreCase(field.getCaption()))
                .findFirst());
    }

    private String getComponentValue(ValueContext ctx, String name) {
        Optional<Component> facetTypeComponent = getComponentByName(ctx, name);
        return facetTypeComponent.map(component -> getValueFromFieldLayout((FieldLayoutComponent.Root<?>) component)).orElse("");
    }

    private Boolean isManualProvider(ValueContext ctx, String name) {
        Optional<Component> facetTypeComponent = getComponentByName(ctx, name);
        if (facetTypeComponent.isPresent()){
            String value = extractProviderValue(facetTypeComponent.get());
            return value == null || value.isEmpty() || value.equals(facetProvider);
        }else {
            return true;
        }
    }

    private String extractProviderValue(Component component){
        FieldLayoutComponent.Root<?> rootComponent = ((FieldLayoutComponent.Root<?>) component);
        VerticalLayout verticalLayout = extractVerticalLayout(rootComponent);
        FieldLayoutComponent.Root<?> fieldLayoutComponent = extractFieldLayoutComponentRoot(verticalLayout);
        return getValueFromFieldLayout(fieldLayoutComponent);
    }

    private VerticalLayout extractVerticalLayout(FieldLayoutComponent.Root<?> fieldLayout){
        return (VerticalLayout) StreamSupport.stream(fieldLayout.spliterator(), false)
                .filter(field -> field instanceof VerticalLayout)
                .findFirst().orElse(null);
    }

    private FieldLayoutComponent.Root<?> extractFieldLayoutComponentRoot(AbstractOrderedLayout verticalLayout){
        Component component = verticalLayout.getComponent(0);
        if (component instanceof FieldLayoutComponent.Root<?>){
            return (FieldLayoutComponent.Root<?>) component;
        }else {
            return extractFieldLayoutComponentRoot((AbstractOrderedLayout) component);
        }
    }

    private String getValueFromFieldLayout(FieldLayoutComponent.Root<?> fieldLayout){
        return fieldLayout.getValue() != null ? fieldLayout.getValue().toString() : null;
    }
}