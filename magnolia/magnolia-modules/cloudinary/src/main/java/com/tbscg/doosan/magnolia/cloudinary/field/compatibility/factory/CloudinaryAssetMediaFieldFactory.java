package com.tbscg.doosan.magnolia.cloudinary.field.compatibility.factory;

import com.tbscg.doosan.magnolia.cloudinary.field.compatibility.CloudinaryAssetMediaField;
import com.tbscg.doosan.magnolia.cloudinary.util.RenditionService;
import com.vaadin.v7.data.Item;
import com.vaadin.v7.data.util.BeanItem;
import com.vaadin.v7.ui.Field;
import info.magnolia.dam.external.app.field.definition.AssetMediaFieldDefinition;
import info.magnolia.objectfactory.ComponentProvider;
import info.magnolia.ui.api.context.UiContext;
import info.magnolia.ui.api.i18n.I18NAuthoringSupport;
import info.magnolia.ui.form.field.factory.AbstractFieldFactory;

import javax.inject.Inject;

/**
 * {@link info.magnolia.ui.form.field.factory.FieldFactory} class responsible for creating
 * {@link CloudinaryAssetMediaField} to be used in dialogs.
 *
 * @author Alejandro Puig Escobar
 */
@Deprecated
public class CloudinaryAssetMediaFieldFactory extends AbstractFieldFactory<AssetMediaFieldDefinition, BeanItem> {

    private final ComponentProvider componentProvider;
    private final AssetMediaFieldDefinition definition;
    private final RenditionService renditionService;

    @Inject
    public CloudinaryAssetMediaFieldFactory(AssetMediaFieldDefinition definition, Item item, UiContext uiContext,
                                            I18NAuthoringSupport i18nAuthoringSupport, ComponentProvider componentProvider,
                                            RenditionService renditionService) {
        super(definition, item, uiContext, i18nAuthoringSupport);
        this.componentProvider = componentProvider;
        this.definition = definition;
        this.renditionService = renditionService;
    }

    @Override
    protected Field<BeanItem> createFieldComponent() {
        return new CloudinaryAssetMediaField(definition, item, componentProvider, renditionService);
    }
}
