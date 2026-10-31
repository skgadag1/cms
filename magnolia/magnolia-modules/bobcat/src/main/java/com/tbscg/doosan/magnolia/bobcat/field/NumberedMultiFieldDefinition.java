package com.tbscg.doosan.magnolia.bobcat.field;

import info.magnolia.ui.field.FieldType;
import info.magnolia.ui.field.JcrMultiFieldDefinition;

/**
 * A {@code jcrMultiField} that renders a running "Tile/Element N" caption on each
 * entry via {@link NumberedMultiFormView}.
 *
 * Wired the same way as the project's other custom field views (see dealernet's
 * {@code TagSwitchableFieldDefinition}): a {@link FieldType}-annotated definition
 * that sets its view through {@code setImplementationClass} in the constructor, used
 * from dialog YAML via {@code $type: numberedJcrMultiField}.
 */
@FieldType("numberedJcrMultiField")
public class NumberedMultiFieldDefinition extends JcrMultiFieldDefinition {

    @SuppressWarnings({"unchecked", "rawtypes"})
    public NumberedMultiFieldDefinition() {
        super.setImplementationClass((Class) NumberedMultiFormView.class);
    }
}
