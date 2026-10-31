package com.tbscg.doosan.magnolia.product.catalog.field;

import info.magnolia.ui.editor.EditorView;
import info.magnolia.ui.editor.FormDefinition;
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

@Getter
@Setter
@EqualsAndHashCode(callSuper = false)
@FieldType("relatedItemsField")
public class RelatedItemsFieldDefinition<T> extends ConfiguredComplexPropertyDefinition<T> implements FormDefinition<T> {
    private List<EditorPropertyDefinition> properties = new ArrayList<>();
    private ConfiguredFieldLayoutDefinition<StackedLayoutProducer> layout = new StackedLayoutProducer.Definition();
    private String itemType;
    private String storeProperty;
    private final Class<EditorView<T>> implementationClass = (Class) RelatedItemsField.class;
}
