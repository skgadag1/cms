package com.tbscg.doosan.magnolia.dealernet.field;

import info.magnolia.ui.datasource.optionlist.Option;
import info.magnolia.ui.datasource.optionlist.OptionListDefinition;
import info.magnolia.ui.editor.FormDefinition;
import info.magnolia.ui.editor.ItemProviderDefinition;
import info.magnolia.ui.field.*;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@FieldType("tagSwitchableField")
@Data
public class TagSwitchableFieldDefinition<T> extends ConfiguredSwitchableFieldDefinition<T> {
    private AbstractSelectFieldDefinition<Option, OptionListDefinition> field;
    private List<FormDefinition<T>> forms = new ArrayList();
    private String root;
    private String defaultValue;
    private Class<WithPropertyNameDecorator.PropertyNameDecorator> propertyNameDecorator = (Class) NoopNameDecorator.class;

    public TagSwitchableFieldDefinition() {
        super.setImplementationClass((Class) TagSwitchableFormView.class);
    }
}
