package com.tbscg.doosan.magnolia.regions.field;

import info.magnolia.ui.field.FieldType;
import info.magnolia.ui.field.TextFieldDefinition;
import lombok.Getter;
import lombok.Setter;

/**
 * Field definition for hreflang locale input.
 * Provides a text field for entering hreflang locale codes in the format xx-xx (e.g., en-us, en-ca).
 */
@Getter
@Setter
@FieldType("dxp:hreflangLocale")
public class HreflangLocaleFieldDefinition extends TextFieldDefinition {

    public HreflangLocaleFieldDefinition() {
        setType(String.class);
        setFactoryClass(HreflangLocaleFieldFactory.class);
        setPlaceholder("e.g., en-us or en-ca");
    }
}
