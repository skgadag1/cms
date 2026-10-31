package com.tbscg.doosan.magnolia.cloudinary.field.binder;

import com.vaadin.data.Binder;
import com.vaadin.data.Converter;
import com.vaadin.data.HasValue;
import com.vaadin.ui.MultiSelect;
import info.magnolia.dam.app.data.AssetSelectFieldSupport;
import info.magnolia.objectfactory.ComponentProvider;
import info.magnolia.ui.editor.MultiItemConverter;
import info.magnolia.ui.field.FieldBinder;
import info.magnolia.ui.field.FieldDefinition;

import javax.inject.Inject;
import java.util.Collections;
import java.util.Optional;

public class AssetSelectFieldBinder<T> extends FieldBinder.Default<T> {

    private final AssetSelectFieldSupport assetSelectFieldSupport;

    @Inject
    public AssetSelectFieldBinder(ComponentProvider componentProvider, AssetSelectFieldSupport selectFieldSupport) {
        super(componentProvider);
        this.assetSelectFieldSupport = selectFieldSupport;
    }

    @Override
    public <BT> Binder.BindingBuilder<BT, T> configureBinding(FieldDefinition<T> definition, Binder.BindingBuilder<BT, T> source) {
        if (source.getField() instanceof MultiSelect) {
            source = source.withNullRepresentation((T) Collections.emptySet());
        }
        return super.configureBinding(definition, source);
    }

    @Override
    protected <PT> Optional<Converter<PT, ?>> createConfiguredConverter(FieldDefinition<PT> definition, HasValue<?> field) {
        return Optional.ofNullable(super.createConfiguredConverter(definition, field).orElse((Converter) this.assetSelectFieldSupport.defaultConverter()))
                .map(converter -> {
                    if (field instanceof MultiSelect) {
                        return new MultiItemConverter((Converter) converter);
                    }
                    return converter;
                });
    }

}
