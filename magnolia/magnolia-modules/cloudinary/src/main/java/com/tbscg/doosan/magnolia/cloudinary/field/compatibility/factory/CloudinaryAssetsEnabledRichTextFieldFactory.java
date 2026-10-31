package com.tbscg.doosan.magnolia.cloudinary.field.compatibility.factory;

import com.google.gson.Gson;
import com.google.gson.JsonParseException;
import com.tbscg.doosan.magnolia.cloudinary.field.dto.CloudinaryFileBrowser;
import com.tbscg.doosan.magnolia.cloudinary.field.dto.CloudinaryLink;
import com.tbscg.doosan.magnolia.cloudinary.util.CloudinaryAssetFieldService;
import com.tbscg.doosan.magnolia.cloudinary.util.CloudinaryConstants;
import com.vaadin.server.Sizeable;
import com.vaadin.server.VaadinService;
import com.vaadin.ui.UI;
import com.vaadin.v7.ui.Field;
import info.magnolia.dam.api.*;
import info.magnolia.dam.app.ui.field.factory.AssetsEnabledRichTextFieldFactory;
import info.magnolia.i18nsystem.SimpleTranslator;
import info.magnolia.ui.api.app.AppController;
import info.magnolia.ui.api.app.ChooseDialogCallback;
import info.magnolia.ui.api.context.UiContext;
import info.magnolia.ui.api.i18n.I18NAuthoringSupport;
import info.magnolia.ui.field.factory.RichTextFieldFactory;
import info.magnolia.ui.form.field.definition.RichTextFieldDefinition;
import info.magnolia.ui.vaadin.ckeditor.MagnoliaCKEditorConfig.ToolbarGroup;
import info.magnolia.ui.vaadin.integration.jcr.JcrItemId;
import info.magnolia.ui.vaadin.integration.jcr.JcrItemUtil;
import info.magnolia.ui.vaadin.richtext.MagnoliaRichTextField;
import info.magnolia.ui.vaadin.richtext.MagnoliaRichTextFieldConfig;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.jcr.Node;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import static com.tbscg.doosan.magnolia.cloudinary.util.CloudinaryConstants.*;
import static info.magnolia.ui.field.factory.RichTextFieldFactory.PLUGIN_NAME_MAGNOLIALINK;
import static info.magnolia.ui.vaadin.ckeditor.MagnoliaCKEditorConfig.defaultToolbar;
import static info.magnolia.ui.vaadin.ckeditor.MagnoliaCKEditorTextFieldEvents.*;

/**
 * Rich text field factory {@link RichTextFieldFactory} for enabling Cloudinary assets selection in rich text fields.
 *
 * @author Alejandro Puig Escobar
 */
@Deprecated
public class CloudinaryAssetsEnabledRichTextFieldFactory extends info.magnolia.ui.form.field.factory.RichTextFieldFactory {
    private static final Logger LOGGER = LoggerFactory.getLogger(CloudinaryAssetsEnabledRichTextFieldFactory.class);

    private final AssetProviderRegistry assetProviderRegistry;
    private final CloudinaryAssetFieldService assetFieldService;

    @Inject
    public CloudinaryAssetsEnabledRichTextFieldFactory(RichTextFieldDefinition definition, com.vaadin.v7.data.Item relatedFieldItem,
                                                       UiContext uiContext, I18NAuthoringSupport i18nAuthoringSupport,
                                                       AppController appController, SimpleTranslator i18n,
                                                       AssetProviderRegistry assetProviderRegistry,
                                                       CloudinaryAssetFieldService assetFieldService) {
        super(definition, relatedFieldItem, uiContext, i18nAuthoringSupport, appController, i18n);
        this.assetProviderRegistry = assetProviderRegistry;
        this.assetFieldService = assetFieldService;
    }

    /**
     * Overrides default toolbar in order to add reorganize button or add custom options like Cloudinary video.
     *
     * @return List of toolbar group items.
     */
    @Override
    protected List<ToolbarGroup> initializeToolbarConfig() {
        final List<ToolbarGroup> defaultToolbar = defaultToolbar();
        defaultToolbar.set(2, new ToolbarGroup("links", "Link", "InternalLink", "DamLink", "Unlink", "Cloudinaryplayer", "Youtube"));
        return defaultToolbar;
    }

    /**
     * Creates and initializes the Vaadin Field instance to use.
     *
     * @return configured rich text field.
     */
    @Override
    protected Field<String> createFieldComponent() {

        MagnoliaRichTextFieldConfig config = initializeCKEditorConfig();
        richTextEditor = new MagnoliaRichTextField(config);

        if (definition.getHeight() > 0) {
            richTextEditor.setHeight(definition.getHeight(), Sizeable.Unit.PIXELS);
        }

        if ((this.definition).isImages() || StringUtils.isNotBlank((this.definition).getConfigJsFile())) {
            richTextEditor.addListener((eventName, value) -> {
                if (eventName.equals(EVENT_CHOOSE_ASSET)) {
                    this.chooseCloudinaryImage();
                }
            });
        }

        richTextEditor.addListener(((eventName, value) -> {
            switch (eventName) {
                case EVENT_CLOUDINARY_LINK:
                    UI.getCurrent().addStyleName("ui-overlapping-ck-editor");
                    this.linkCloudinaryDocument();
                    break;

                case EVENT_CLOUDINARY_VIDEO:
                    try {
                        this.chooseCloudinaryVideo();
                    } catch (JsonParseException e) {
                        LOGGER.error("Unable to open Cloudinary video chooser dialog", e);
                        richTextEditor.firePluginEvent(EVENT_CANCEL_LINK, i18n.translate(LABEL_UNABLE_TO_OPEN_CHOOSER));
                    }
                    break;

                case EVENT_CLOUDINARY_CHAPTERS_FILE:
                    try {
                        this.chooseCloudinaryChaptersFile();
                    } catch (JsonParseException e) {
                        LOGGER.error("Unable to open Cloudinary asset chooser dialog", e);
                        richTextEditor.firePluginEvent(EVENT_CANCEL_LINK, i18n.translate(LABEL_UNABLE_TO_OPEN_CHOOSER));
                    }
                    break;

                case EVENT_GET_MAGNOLIA_LINK:
                    try {
                        Gson gson = new Gson();
                        RichTextFieldFactory.PluginData data = gson.fromJson(value, RichTextFieldFactory.PluginData.class);
                        this.linkMagnoliaPage(data.path);

                    } catch (JsonParseException e) {
                        LOGGER.error("Unable to open pages chooser dialog", e);
                        richTextEditor.firePluginEvent(EVENT_CANCEL_LINK, i18n.translate(LABEL_UNABLE_TO_OPEN_CHOOSER));
                    }
            }
        }));

        return richTextEditor;
    }

    /**
     * Initializes configuration for rich text field.
     *
     * @return rich text field configuration.
     */
    @Override
    protected MagnoliaRichTextFieldConfig initializeCKEditorConfig() {
        MagnoliaRichTextFieldConfig config = super.initializeCKEditorConfig();

        // MAGNOLIA FILEBROWSER PLUGIN — may be used with/without customConfig
        boolean isCustomConfig = StringUtils.isNotBlank(definition.getConfigJsFile());

        // Hook in our patched filebrowser plugin for triggering assets choose dialog
        String path = VaadinService.getCurrentRequest().getContextPath();

        if (definition.isImages() || isCustomConfig) {

            config.addExternalPlugin(FILE_BROWSER_PLUGIN, path + CLOUDINARY_FILE_BROWSER_PLUGIN_PATH);
            config.addListenedEvent(EVENT_CHOOSE_ASSET);

            if (!isCustomConfig) {
                config.addToExtraPlugins(FILE_BROWSER_PLUGIN);
            }

            // Make filebrowser button visible inside image dialog - just needs non-null/empty filebrowser Url properties
            config.setFilebrowserImageBrowseLinkUrl(FILE_BROWSER_PLUGIN);
            config.setFilebrowserImageBrowseUrl(FILE_BROWSER_PLUGIN);
        }

        //Override previous plugin configuration
        config.addListenedEvent(EVENT_CLOUDINARY_LINK);
        config.addListenedEvent(EVENT_CLOUDINARY_VIDEO);
        config.addListenedEvent(EVENT_CLOUDINARY_CHAPTERS_FILE);

        config.addToRemovePlugins(PLUGIN_NAME_MAGNOLIALINK);
        config.addExternalPlugin(PLUGIN_NAME_MAGNOLIALINK, path + CLOUDINARY_LINK_PLUGIN_PATH);

        if (!isCustomConfig) {
            config.addToExtraPlugins(PLUGIN_NAME_MAGNOLIALINK);
        }

        config.addExternalPlugin(CLOUDINARY_VIDEO_PLAYER_PLUGIN, path + CLOUDINARY_VIDEO_PLAYER_PLUGIN_PATH);
        config.addToExtraPlugins(CLOUDINARY_VIDEO_PLAYER_PLUGIN);

        config.addExternalPlugin(YOUTUBE_PLUGIN, path + YOUTUBE_PLUGIN_PATH);
        config.addToExtraPlugins(YOUTUBE_PLUGIN);

        List<ToolbarGroup> toolbarGroupList = new ArrayList<>();
        toolbarGroupList.add(new ToolbarGroup(TOOLBAR_STYLES, FORMAT_PLUGIN_DROPDOWN_NAME));
        config.addToolbarLine(toolbarGroupList);

        return config;
    }

    //TODO: use ChooserController once dam-external-app (Cloudinary app) is migrated to V6
    private void chooseCloudinaryImage() {
        chooseCloudinaryAsset(IMAGE_ASSET_TYPE, IMAGE_MIME_TYPE_PATTERN, EVENT_ASSET_CHOSEN);
    }

    //TODO: use ChooserController once dam-external-app (Cloudinary app) is migrated to V6
    private void chooseCloudinaryVideo() {
        chooseCloudinaryAsset(VIDEO_ASSET_TYPE, VIDEO_MIME_TYPE_PATTERN, EVENT_VIDEO_CHOSEN);
    }

    //TODO: use ChooserController once dam-external-app (Cloudinary app) is migrated to V6
    private void chooseCloudinaryChaptersFile() {
        chooseCloudinaryAsset(TEXT_ASSET_TYPE, TEXT_MIME_TYPE_PATTERN, EVENT_VTT_CHOSEN);
    }

    private void chooseCloudinaryAsset(String assetType, Pattern mimeTypePattern, String eventChosen) {
        this.appController.openChooseDialog(APP_CLOUDINARY_ASSETS, this.uiContext, null, new ChooseDialogCallback() {

            public void onItemChosen(String actionName, Object chosenValue) {
                UI.getCurrent().removeStyleName("ui-overlapping-ck-editor");

                Gson gson = new Gson();
                String eventJson = "{}";

                try {
                    Item item = assetProviderRegistry.getProviderById(CloudinaryConstants.ASSET_PROVIDER_ID)
                            .getItem((ItemKey) chosenValue);

                    if (!(item instanceof Asset)) {
                        LOGGER.error("Chosen item {} is not an asset", ((ItemKey) chosenValue).asString());
                        return;
                    }

                    Asset asset = (Asset) item;

                    if (mimeTypePattern.matcher(asset.getMimeType()).matches()) {
                        // Sending over asset properties
                        CloudinaryFileBrowser cloudinaryFileBrowser = assetFieldService.getCloudinaryFileBrowser(asset);
                        eventJson = gson.toJson(cloudinaryFileBrowser);

                    } else {
                        eventJson = gson.toJson(new AssetsEnabledRichTextFieldFactory.FileBrowserUrlDTO(StringUtils.EMPTY,
                        "Selected asset is not a(n) " + assetType));
                    }
                } catch (DamException | JsonParseException ex) {
                    LOGGER.error("Unable to choose Cloudinary asset {}", ((ItemKey) chosenValue).asString(), ex);
                } finally {
                    richTextEditor.firePluginEvent(eventChosen, eventJson);
                }
            }

            public void onCancel() {
                richTextEditor.firePluginEvent(EVENT_CANCEL_LINK);
            }
        });
    }

    private void linkCloudinaryDocument() {
        this.appController.openChooseDialog(APP_CLOUDINARY_ASSETS, this.uiContext, null, new ChooseDialogCallback() {

            @Override
            public void onItemChosen(String actionName, Object chosenValue) {
                UI.getCurrent().removeStyleName("ui-overlapping-ck-editor");

                Gson gson = new Gson();
                String eventJson = "{}";

                try {
                    Item item = assetProviderRegistry.getProviderById(CloudinaryConstants.ASSET_PROVIDER_ID)
                            .getItem((ItemKey) chosenValue);

                    if (!(item instanceof Asset)) {
                        richTextEditor.firePluginEvent(EVENT_CANCEL_LINK);
                        LOGGER.error("Chosen item {} is not an asset", ((ItemKey) chosenValue).asString());
                        return;
                    }

                    Asset asset = (Asset) item;

                    eventJson = gson.toJson(new CloudinaryLink(asset.getFileName(), asset.getLink()));

                } catch (Exception e) {
                    LOGGER.error("Unable to link Cloudinary asset {}", ((ItemKey) chosenValue).asString(), e);

                } finally {
                    richTextEditor.firePluginEvent(EVENT_CLOUDINARY_LINK_SELECTED, eventJson);
                }

            }

            @Override
            public void onCancel() {
                richTextEditor.firePluginEvent(EVENT_CANCEL_LINK);
            }
        });
    }

    private void linkMagnoliaPage(String path) {
        appController.openChooseDialog(APP_PAGES_OLD, uiContext, path, new ChooseDialogCallback() {

            @Override
            public void onItemChosen(String actionName, Object chosenValue) {
                if (!(chosenValue instanceof JcrItemId)) {
                    richTextEditor.firePluginEvent(EVENT_CANCEL_LINK);
                    return;
                }

                try {
                    javax.jcr.Item jcrItem = JcrItemUtil.getJcrItem((JcrItemId) chosenValue);

                    if (!jcrItem.isNode()) {
                        return;
                    }

                    final Node selected = (Node) jcrItem;
                    Gson gson = new Gson();
                    RichTextFieldFactory.MagnoliaLink mlink = new RichTextFieldFactory.MagnoliaLink();
                    mlink.identifier = selected.getIdentifier();
                    mlink.repository = selected.getSession().getWorkspace().getName();
                    mlink.path = selected.getPath();

                    if (selected.hasProperty(PROPERTY_TITLE)) {
                        mlink.caption = selected.getProperty(PROPERTY_TITLE).getString();
                    } else {
                        mlink.caption = selected.getName();
                    }

                    richTextEditor.firePluginEvent(EVENT_SEND_MAGNOLIA_LINK, gson.toJson(mlink));
                } catch (Exception e) {
                    String error = i18n.translate(LABEL_NON_EXITING_DIRECTORY);
                    LOGGER.error(error, e);
                    richTextEditor.firePluginEvent(EVENT_CANCEL_LINK, error);
                }
            }

            @Override
            public void onCancel() {
                richTextEditor.firePluginEvent(EVENT_CANCEL_LINK);
            }
        });
    }

}
