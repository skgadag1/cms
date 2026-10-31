package com.tbscg.doosan.magnolia.product.catalog.field;

import info.magnolia.ui.editor.FormDefinition;
import info.magnolia.ui.editor.FormView;
import info.magnolia.ui.field.ConfiguredComplexPropertyDefinition;
import info.magnolia.ui.field.EditorPropertyDefinition;
import info.magnolia.ui.field.FieldType;
import info.magnolia.ui.framework.layout.ConfiguredFieldLayoutDefinition;
import info.magnolia.ui.framework.layout.StackedLayoutProducer;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

/**
 * Definition of @{@link ProductCompositeField}.
 *
 * @param <T> Field Type
 */
@Getter
@Setter
@EqualsAndHashCode(callSuper = false)
@FieldType("productCompositeField")
public class ProductCompositeFieldDefinition<T> extends ConfiguredComplexPropertyDefinition<T> implements FormDefinition<T> {
    private final Class<FormView<T>> implementationClass = (Class) ProductCompositeField.class;
    private List<EditorPropertyDefinition> properties = new ArrayList<>();
    private ConfiguredFieldLayoutDefinition<StackedLayoutProducer> layout = new StackedLayoutProducer.Definition();
    private Integer specLevel;
}
