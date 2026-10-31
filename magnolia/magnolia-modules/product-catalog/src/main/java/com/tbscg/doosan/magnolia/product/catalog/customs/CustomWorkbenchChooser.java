package com.tbscg.doosan.magnolia.product.catalog.customs;

import info.magnolia.ui.chooser.WorkbenchChooser;
import info.magnolia.ui.ValueContext;
import info.magnolia.ui.chooser.definition.WorkbenchChooserDefinition;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.stream.Collectors;

public class CustomWorkbenchChooser<T> extends WorkbenchChooser<T> {

    private final ValueContext<T> valueContext;

    public CustomWorkbenchChooser(WorkbenchChooserDefinition<T> definition, ValueContext<T> valueContext) {
        super(definition, valueContext);
        this.valueContext = valueContext;
    }
    @Override
    public Set<T> getChoice() {
        return this.valueContext.get().collect(Collectors.toCollection(LinkedHashSet::new));
    }
}
