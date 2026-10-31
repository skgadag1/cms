package com.tbscg.doosan.magnolia.facet.configuration.customs;

import com.vaadin.ui.AbstractTextField;
import com.vaadin.ui.Component;
import com.vaadin.ui.TextArea;
import com.vaadin.ui.TextField;
import info.magnolia.objectfactory.ComponentProvider;
import info.magnolia.ui.field.factory.AbstractFieldFactory;
import info.magnolia.ui.vaadin.extension.MaxLengthIndicatorExtension;

import javax.inject.Inject;

public class TextFieldWithValidationAlwaysEnabledFactory extends AbstractFieldFactory<String, TextFieldWithValidationAlwaysEnabledDefinition> {

    @Inject
    public TextFieldWithValidationAlwaysEnabledFactory(TextFieldWithValidationAlwaysEnabledDefinition definition, ComponentProvider componentProvider) {
        super(definition, componentProvider);
    }

    @Override
    public Component createFieldComponent() {
        // Create a TextArea if the rows > 1
        AbstractTextField field;
        if (getDefinition().getRows() > 1) {
            TextArea textArea = new TextArea();
            textArea.setRows(getDefinition().getRows());
            field = textArea;
        } else {
            field = new TextField();
        }
        if (getDefinition().getMaxLength() != -1) {
            field.setMaxLength(getDefinition().getMaxLength());
            MaxLengthIndicatorExtension.extend(field);
        }
        field.setPlaceholder(getDefinition().getPlaceholder());
        return field;
    }
}

