package com.tbscg.doosan.magnolia.localizations.compatibility.app.column;

import com.tbscg.doosan.magnolia.bobcat.ui.contentapp.column.definition.AbstractSafeHtmlColumnDefinition;
import com.vaadin.data.ValueProvider;
import info.magnolia.ui.datasource.jcr.JcrNodeWrapper;
import lombok.extern.slf4j.Slf4j;

/**
 * Column definition for the Localization Status column with HTML support.
 * Extends AbstractSafeHtmlColumnDefinition to safely render HTML content.
 */
@Slf4j
public class LocalizationStatusColumnDefinition extends AbstractSafeHtmlColumnDefinition<JcrNodeWrapper> {

    @Override
    public Class<? extends ValueProvider<JcrNodeWrapper, String>> getValueProvider() {
        return LocalizationStatusValueProvider.class;
    }
}

