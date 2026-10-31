package com.tbscg.doosan.magnolia.regions.field;

import com.vaadin.ui.Component;
import com.vaadin.ui.TextField;
import info.magnolia.objectfactory.ComponentProvider;
import info.magnolia.ui.field.factory.AbstractFieldFactory;
import info.magnolia.ui.vaadin.extension.MaxLengthIndicatorExtension;
import lombok.extern.slf4j.Slf4j;

import javax.inject.Inject;

/**
 * Field factory for hreflang locale input.
 * Creates a text field for entering hreflang locale codes with proper formatting hints.
 */
@Slf4j
public class HreflangLocaleFieldFactory extends AbstractFieldFactory<String, HreflangLocaleFieldDefinition> {

    @Inject
    public HreflangLocaleFieldFactory(HreflangLocaleFieldDefinition definition, ComponentProvider componentProvider) {
        super(definition, componentProvider);
    }

    @Override
    public Component createFieldComponent() {
        TextField field = new TextField();
        
        // Set placeholder text
        field.setPlaceholder(getDefinition().getPlaceholder());
        
        // Set max length to enforce format (2 chars + dash + 2 chars = 5 characters)
        field.setMaxLength(5);
        MaxLengthIndicatorExtension.extend(field);
        
        // Convert input to lowercase automatically
        field.addValueChangeListener(event -> {
            String value = event.getValue();
            if (value != null && !value.isEmpty()) {
                String lowercaseValue = value.toLowerCase();
                if (!lowercaseValue.equals(value)) {
                    field.setValue(lowercaseValue);
                }
            }
        });
        
        log.debug("Created hreflang locale field component");
        return field;
    }
}
