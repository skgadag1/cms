package com.tbscg.doosan.magnolia.cloudinary.field.compatibility;

import com.tbscg.doosan.magnolia.cloudinary.dam.CloudinaryAsset;
import com.tbscg.doosan.magnolia.cloudinary.util.RenditionService;
import com.vaadin.server.ExternalResource;
import com.vaadin.server.Resource;
import com.vaadin.ui.Button;
import com.vaadin.ui.Component;
import com.vaadin.ui.CssLayout;
import com.vaadin.ui.Image;
import com.vaadin.v7.data.Item;
import com.vaadin.v7.data.util.BeanItem;
import info.magnolia.dam.api.Asset;
import info.magnolia.dam.app.ui.field.configuration.PreviewComponentProvider;
import info.magnolia.dam.external.app.field.AssetMediaField;
import info.magnolia.dam.external.app.field.definition.AssetMediaFieldDefinition;
import info.magnolia.icons.MagnoliaIcons;
import info.magnolia.objectfactory.ComponentProvider;
import lombok.extern.slf4j.Slf4j;

/**
 * Extension of {@link AssetMediaField} that gets a {@link Resource} as {@link ExternalResource}
 * based on its URL.
 *
 * @author Alejandro Puig Escobar
 */
@Slf4j
public class CloudinaryAssetMediaField extends AssetMediaField {

    private CssLayout rootLayout;
    
    private final RenditionService renditionService;

    public CloudinaryAssetMediaField(AssetMediaFieldDefinition definition, Item relatedFieldItem,
                                     ComponentProvider componentProvider, RenditionService renditionService) {
        super(definition, relatedFieldItem, componentProvider);
        this.renditionService = renditionService;
    }

    @Override
    protected Component initContent() {
        rootLayout = new CssLayout();
        rootLayout.setWidth("150px");
        rootLayout.setHeight("150px");
        setImmediate(false);
        initFields();
        return rootLayout;
    }

    @Override
    protected Resource getResource(Asset asset) {
        return new ExternalResource(renditionService.getDefaultImageRenditionUrl((CloudinaryAsset) asset));
    }

    @Override
    protected void initFields(BeanItem<Asset> beanItem) {
        if (beanItem == null || beanItem.getBean() == null) {
            return;
        }
        // Get Asset
        final Asset asset = beanItem.getBean();

        final Resource imageResource = getResource(asset);

        Button lightBoxButton = new Button();
        lightBoxButton.addStyleName("lightbox-button");
        lightBoxButton.setCaptionAsHtml(true);
        lightBoxButton.setCaption(MagnoliaIcons.SEARCH.getHtml());
        rootLayout.addComponent(lightBoxButton);

        embedded = new Image("", imageResource);
        embedded.addStyleName("preview-image");
        rootLayout.addComponent(embedded);

        lightBoxButton.addClickListener( event -> this.launchPreviewComponent(imageResource));
    }

    private void launchPreviewComponent(Resource imageResource) {
        {
            Class<? extends PreviewComponentProvider> previewActionClass = definition.getPreviewComponentProviderClass();
            // Launch LightBox component
            if (previewActionClass != null) {
                PreviewComponentProvider implementation = componentProvider.newInstance(previewActionClass);
                implementation.open(imageResource);
            } else {
                log.warn("No preview component defined.");
            }
        }
    }
}
