package com.tbscg.doosan.magnolia.cloudinary.field.compatibility.factory;

import com.tbscg.doosan.magnolia.cloudinary.field.compatibility.CloudinaryAssetMetadataField;
import com.vaadin.v7.data.Item;
import com.vaadin.v7.data.util.BeanItem;
import com.vaadin.v7.ui.Field;
import info.magnolia.dam.external.app.field.definition.AssetMetadataFieldDefinition;
import info.magnolia.objectfactory.ComponentProvider;
import info.magnolia.ui.api.context.UiContext;
import info.magnolia.ui.api.i18n.I18NAuthoringSupport;
import info.magnolia.ui.form.field.factory.AbstractFieldFactory;
import info.magnolia.ui.form.field.factory.FieldFactoryFactory;

import javax.inject.Inject;

/**
 * {@link AbstractFieldFactory} in charge of creating {@link com.tbscg.doosan.magnolia.cloudinary.dam.CloudinaryAssetMetadata}
 * fields.
 *
 * @author Alejandro Puig Escobar
 */
@Deprecated
public class CloudinaryAssetMetadataFieldFactory extends AbstractFieldFactory<AssetMetadataFieldDefinition, BeanItem> {

    private final FieldFactoryFactory fieldFactoryFactory;
    private final ComponentProvider componentProvider;

    @Inject
    public CloudinaryAssetMetadataFieldFactory(AssetMetadataFieldDefinition definition, Item item, UiContext uiContext,
                                               I18NAuthoringSupport i18nAuthoringSupport, FieldFactoryFactory fieldFactoryFactory,
                                               ComponentProvider componentProvider) {
        super(definition, item, uiContext, i18nAuthoringSupport);
        this.fieldFactoryFactory = fieldFactoryFactory;
        this.componentProvider = componentProvider;
    }

    @Override
    protected Field<BeanItem> createFieldComponent() {
        return new CloudinaryAssetMetadataField(fieldFactoryFactory, componentProvider, item);
    }
}
