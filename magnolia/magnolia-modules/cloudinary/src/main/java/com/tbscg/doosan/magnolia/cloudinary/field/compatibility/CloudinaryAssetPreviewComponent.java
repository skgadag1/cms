package com.tbscg.doosan.magnolia.cloudinary.field.compatibility;

import com.google.common.net.MediaType;
import com.tbscg.doosan.magnolia.cloudinary.CloudinaryConfig;
import com.tbscg.doosan.magnolia.cloudinary.dam.CloudinaryAsset;
import com.tbscg.doosan.magnolia.cloudinary.field.compatibility.config.AssetPreviewConfiguration;
import com.tbscg.doosan.magnolia.cloudinary.field.compatibility.config.thumbnail.CloudinaryThumbnailComponentProvider;
import com.tbscg.doosan.magnolia.cloudinary.util.CloudinaryConstants;
import com.tbscg.doosan.magnolia.cloudinary.util.RenditionService;
import com.vaadin.server.ExternalResource;
import com.vaadin.server.Resource;
import com.vaadin.ui.*;
import info.magnolia.cms.util.PathUtil;
import info.magnolia.dam.api.*;
import info.magnolia.dam.app.ui.field.configuration.PreviewComponentProvider;
import info.magnolia.i18nsystem.SimpleTranslator;
import info.magnolia.icons.MagnoliaIcons;
import info.magnolia.objectfactory.ComponentProvider;
import info.magnolia.ui.form.field.component.AbstractContentPreviewComponent;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;

import javax.inject.Inject;

/**
 * {@link info.magnolia.ui.form.field.component.ContentPreviewComponent} implementation for
 * assets coming from Cloudinary.
 *
 * @author Alejandro Puig Escobar
 */
@Slf4j
public class CloudinaryAssetPreviewComponent extends AbstractContentPreviewComponent<Asset> {

    private static final String FILE_PREVIEW_TITLE_LABEL = "dam.assets.filePreview.title";
    private static final String FILE_PREVIEW_SIZE_LABEL = "dam.assets.filePreview.size";
    private static final String FILE_PREVIEW_FORMAT_LABEL = "dam.assets.filePreview.format";
    private static final String LINKFIELD_PREVIEW_DETAIL_LABEL = "cloudinary.linkField.detailLabel";

    private CloudinaryAsset currentAsset;
    private AssetPreviewConfiguration assetPreviewConfig;
    private final SimpleTranslator i18n;

    private final ComponentProvider componentProvider;
    private final AssetProviderRegistry assetProviderRegistry;
    private final CloudinaryConfig cloudinaryConfig;
    private final RenditionService renditionService;

    @Inject
    public CloudinaryAssetPreviewComponent(String workspace, ComponentProvider componentProvider,
                                           AssetProviderRegistry assetProviderRegistry,
                                           SimpleTranslator i18n, CloudinaryConfig cloudinaryConfig,
                                           RenditionService renditionService) {
        super(workspace);
        this.renditionService = renditionService;
        this.componentProvider = componentProvider;
        this.assetProviderRegistry = assetProviderRegistry;
        this.cloudinaryConfig = cloudinaryConfig;
        this.i18n = i18n;

        this.rootLayout = new HorizontalLayout();
        this.setCompositionRoot(this.rootLayout);
    }

    @Override
    protected Asset refreshItem(String itemKey) {
        AssetProvider provider = this.assetProviderRegistry.getProviderById(CloudinaryConstants.ASSET_PROVIDER_ID);

        Asset asset = provider.getAsset(new ItemKey(CloudinaryConstants.ASSET_PROVIDER_ID, itemKey));

        if (!(asset instanceof CloudinaryAsset)) {
            return null;
        }

        this.currentAsset = (CloudinaryAsset) asset;

        String mimeType = this.currentAsset.getMimeType();

        if (mimeType.isEmpty()) {
            return null;
        }

        this.assetPreviewConfig = this.cloudinaryConfig.getAssetPreviewConfigurationForMediaType(MediaType.parse(mimeType));

        return this.currentAsset;
    }

    @Override
    public Component refreshContentDetail(Asset item) {
        FormLayout fileInfo = new FormLayout();

        fileInfo.setSizeUndefined();
        fileInfo.addStyleName("file-details");
        fileInfo.addComponent(this.getFileDetailFileName());
        fileInfo.addComponent(this.getFileDetailSize());
        fileInfo.addComponent(this.getFileDetailFileFormat());

        return fileInfo;
    }

    @Override
    public Component refreshContentPreview(Asset item) {
        CssLayout previewLayout = new CssLayout();
        previewLayout.addStyleName("file-preview-area");

        Component image = createPreviewComponent();

        if (image != null) {
            image.addStyleName("preview-image");
            previewLayout.addComponent(image);
        }

        if (this.assetPreviewConfig.hasPreviewConfig()) {

            Button lightBoxButton = new Button(MagnoliaIcons.SEARCH);
            lightBoxButton.addStyleNames("edit-button", "v-button-icon");
            lightBoxButton.setCaptionAsHtml(true);
            lightBoxButton.setDescription(this.i18n.translate(LINKFIELD_PREVIEW_DETAIL_LABEL));
            previewLayout.addComponent(lightBoxButton);

            lightBoxButton.addClickListener(new Button.ClickListener() {
                private static final long serialVersionUID = 1L;

                @Override
                public void buttonClick(Button.ClickEvent event) {

                    Class<? extends PreviewComponentProvider> previewActionClass = assetPreviewConfig.getPreviewConfig().getPreviewComponentProviderClass();

                    if (previewActionClass != null) {
                        PreviewComponentProvider implementation = componentProvider.newInstance(previewActionClass);
                        implementation.open(getResource());

                    } else {
                        log.warn("No preview class is defined");
                    }
                }
            });
        }

        return previewLayout;
    }

    /**
     * Add File Name.
     */
    private Component getFileDetailFileName() {
        Label label = new Label();
        label.setCaption(i18n.translate(FILE_PREVIEW_TITLE_LABEL));
        label.setValue(PathUtil.stripExtension(this.currentAsset.getFileName()));
        return label;
    }

    /**
     * Add File Info.
     */
    private Component getFileDetailSize() {
        Label label = new Label();
        label.setCaption(i18n.translate(FILE_PREVIEW_SIZE_LABEL));
        label.setValue(FileUtils.byteCountToDisplaySize(this.currentAsset.getFileSize()));
        return label;
    }

    /**
     * Add File Format.
     */
    private Component getFileDetailFileFormat() {
        Label label = new Label();
        label.setCaption(this.i18n.translate(FILE_PREVIEW_FORMAT_LABEL));
        label.setValue(this.currentAsset.getFormat());
        return label;
    }

    /**
     * Create a resource from the link.
     */
    private Resource getResource() {
        return new ExternalResource(this.renditionService.getDefaultImageRenditionUrl(this.currentAsset));
    }

    private Component createPreviewComponent() {
        Class<? extends CloudinaryThumbnailComponentProvider> thumbnailProviderClass =
                this.assetPreviewConfig.getThumbnailComponentProviderClass();

        if (thumbnailProviderClass != null) {

            CloudinaryThumbnailComponentProvider implementation = this.componentProvider.newInstance(thumbnailProviderClass);
            return implementation.createThumbnailComponent(this.currentAsset);

        } else {
            log.warn("No CloudinaryThumbnailComponentProvider is defined");
            return null;
        }
    }

}
