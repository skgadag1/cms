package com.tbscg.doosan.magnolia.cloudinary.field;

import com.google.common.net.MediaType;
import com.tbscg.doosan.magnolia.cloudinary.CloudinaryConfig;
import com.tbscg.doosan.magnolia.cloudinary.dam.CloudinaryAsset;
import com.tbscg.doosan.magnolia.cloudinary.field.compatibility.config.AssetPreviewConfiguration;
import com.tbscg.doosan.magnolia.cloudinary.field.compatibility.config.thumbnail.CloudinaryThumbnailComponentProvider;
import com.tbscg.doosan.magnolia.cloudinary.util.RenditionService;
import com.vaadin.server.ExternalResource;
import com.vaadin.server.Resource;
import com.vaadin.ui.*;
import info.magnolia.amazon.s3.dam.AmazonS3Asset;
import info.magnolia.dam.api.Item;
import info.magnolia.dam.app.data.AssetPropertySetFactory;
import info.magnolia.dam.app.imageprovider.AssetPreviewProvider;
import info.magnolia.dam.app.ui.field.configuration.PreviewComponentProvider;
import info.magnolia.i18nsystem.SimpleTranslator;
import info.magnolia.icons.MagnoliaIcons;
import info.magnolia.objectfactory.ComponentProvider;
import info.magnolia.ui.preview.AbstractItemPreviewComponent;
import info.magnolia.ui.preview.ItemPreviewComponent;
import lombok.extern.slf4j.Slf4j;

import javax.inject.Inject;
import java.util.HashMap;
import java.util.Map;

/**
 * {@link ItemPreviewComponent} implementation for assets coming from Cloudinary.
 *
 * @author Alejandro Puig Escobar
 */
@Slf4j
public class CloudinaryAssetPreviewComponent extends AbstractItemPreviewComponent<Item> {

    private static final String LINKFIELD_PREVIEW_DETAIL_LABEL = "cloudinary.linkField.detailLabel";

    private AssetPreviewConfiguration assetPreviewConfig;

    private final ComponentProvider componentProvider;
    private final CloudinaryConfig cloudinaryConfig;
    private final RenditionService renditionService;

    @Inject
    public CloudinaryAssetPreviewComponent(ComponentProvider componentProvider,
                                           CloudinaryConfig cloudinaryConfig, RenditionService renditionService,
                                           SimpleTranslator i18n, AssetPropertySetFactory propertySetFactory,
                                           AssetPreviewProvider previewProvider) {
        super(i18n, propertySetFactory, previewProvider, getProperties());
        this.renditionService = renditionService;
        this.componentProvider = componentProvider;
        this.cloudinaryConfig = cloudinaryConfig;
    }

    private static Map<String, Class> getProperties() {
        Map<String, Class> properties = new HashMap<>();
        properties.put("name", String.class);
        properties.put("fileSize", String.class);
        properties.put("mimeType", String.class);
        return properties;
    }

    @Override
    protected Layout getRootLayout() {
        return rootLayout == null ? new VerticalLayout() : rootLayout;
    }

    @Override
    protected Component createDetailComponent(String propertyName, Object value) {
        TextField component = new TextField(getI18n().translate("dam.damLinkField.fileInfo.".concat(propertyName)));
        component.setValue(String.valueOf(value));
        component.setReadOnly(true);
        return component;
    }

    @Override
    public Component refreshItemPreview(Item item) {

        if (item instanceof AmazonS3Asset) {
            return null;
        }

        if (!(item instanceof CloudinaryAsset)) {
            throw new IllegalArgumentException("Can only work with assets!");
        }

       CloudinaryAsset asset = (CloudinaryAsset) item;

        CssLayout previewLayout = new CssLayout();
        previewLayout.addStyleName("file-preview-area");

        this.assetPreviewConfig = this.cloudinaryConfig.getAssetPreviewConfigurationForMediaType(
                MediaType.parse(asset.getMimeType()));

        Component image = createPreviewComponent(asset);

        if (image != null) {
            image.addStyleName("preview-image");
            previewLayout.addComponent(image);
        }

        if (this.assetPreviewConfig.hasPreviewConfig()) {

            Button lightBoxButton = new Button(MagnoliaIcons.SEARCH);
            lightBoxButton.addStyleNames("edit-button", "v-button-icon");
            lightBoxButton.setCaptionAsHtml(true);
            lightBoxButton.setDescription(getI18n().translate(LINKFIELD_PREVIEW_DETAIL_LABEL));
            previewLayout.addComponent(lightBoxButton);

            lightBoxButton.addClickListener(new Button.ClickListener() {
                private static final long serialVersionUID = 1L;

                @Override
                public void buttonClick(Button.ClickEvent event) {

                    Class<? extends PreviewComponentProvider> previewActionClass = assetPreviewConfig.getPreviewConfig().getPreviewComponentProviderClass();

                    if (previewActionClass != null) {
                        PreviewComponentProvider implementation = componentProvider.newInstance(previewActionClass);
                        implementation.open(getResource(asset));

                    } else {
                        log.warn("No preview class is defined");
                    }
                }
            });
        }

        return previewLayout;
    }

    /**
     * Create a resource from the link.
     */
    private Resource getResource(CloudinaryAsset asset) {
        return new ExternalResource(this.renditionService.getDefaultImageRenditionUrl(asset));
    }

    private Component createPreviewComponent(CloudinaryAsset asset) {
        Class<? extends CloudinaryThumbnailComponentProvider> thumbnailProviderClass =
                this.assetPreviewConfig.getThumbnailComponentProviderClass();

        if (thumbnailProviderClass != null) {

            CloudinaryThumbnailComponentProvider implementation = this.componentProvider.newInstance(thumbnailProviderClass);
            return implementation.createThumbnailComponent(asset);

        } else {
            log.warn("No CloudinaryThumbnailComponentProvider is defined");
            return null;
        }
    }
}
