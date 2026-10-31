package com.tbscg.doosan.magnolia.cloudinary.field.factory;

import com.google.gson.Gson;
import com.tbscg.doosan.magnolia.cloudinary.dam.CloudinaryAsset;
import com.tbscg.doosan.magnolia.cloudinary.field.dto.CloudinaryFileBrowser;
import com.tbscg.doosan.magnolia.cloudinary.util.CloudinaryAssetFieldService;
import com.tbscg.doosan.magnolia.cloudinary.util.CloudinaryMediaLibraryService;
import com.tbscg.doosan.magnolia.dxp.core.util.NodeUtil;
import com.vaadin.server.Sizeable;
import com.vaadin.server.VaadinService;
import com.vaadin.ui.Component;
import com.vaadin.ui.JavaScript;
import com.vaadin.ui.Notification;
import com.vaadin.ui.UI;
import info.magnolia.i18nsystem.SimpleTranslator;
import info.magnolia.jcr.util.PropertyUtil;
import info.magnolia.jcr.util.SessionUtil;
import info.magnolia.objectfactory.ComponentProvider;
import info.magnolia.repository.RepositoryConstants;
import info.magnolia.ui.api.app.registry.AppDescriptorRegistry;
import info.magnolia.ui.chooser.SingleItemWorkbenchChooser;
import info.magnolia.ui.chooser.definition.AppAwareWorkbenchChooserDefinition;
import info.magnolia.ui.chooser.definition.ChooserDefinition;
import info.magnolia.ui.chooser.definition.SingleItemWorkbenchChooserDefinition;
import info.magnolia.ui.field.RichTextFieldDefinition;
import info.magnolia.ui.field.factory.RichTextFieldFactory;
import info.magnolia.ui.framework.overlay.ChooserController;
import info.magnolia.ui.vaadin.ckeditor.MagnoliaCKEditorConfig;
import info.magnolia.ui.vaadin.ckeditor.MagnoliaCKEditorConfig.ToolbarGroup;
import info.magnolia.ui.vaadin.ckeditor.MagnoliaCKEditorTextField;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.jackrabbit.JcrConstants;

import javax.inject.Inject;
import javax.jcr.Node;
import java.util.ArrayList;
import java.util.List;

import static com.tbscg.doosan.magnolia.cloudinary.util.CloudinaryConstants.*;
import static com.tbscg.doosan.magnolia.cloudinary.util.CloudinaryUtils.handleCloudinaryError;
import static info.magnolia.ui.vaadin.ckeditor.MagnoliaCKEditorConfig.defaultToolbar;
import static info.magnolia.ui.vaadin.ckeditor.MagnoliaCKEditorTextFieldEvents.*;

@Slf4j
public class CloudinaryAssetsEnabledRichTextFieldFactory extends RichTextFieldFactory {

    private final ChooserController chooserController;
    private final SimpleTranslator i18nTranslator;
    private final CloudinaryAssetFieldService assetFieldService;
    private final CloudinaryMediaLibraryService cloudinaryService;
    private final Gson gson;
    private MagnoliaCKEditorTextField richTextEditor;

    @Inject
    public CloudinaryAssetsEnabledRichTextFieldFactory(RichTextFieldDefinition definition,
                                                       ComponentProvider componentProvider,
                                                       ChooserController chooserController,
                                                       SimpleTranslator i18nTranslator,
                                                       AppDescriptorRegistry appDescriptorRegistry,
                                                       CloudinaryAssetFieldService assetFieldService,
                                                       CloudinaryMediaLibraryService cloudinaryService,
                                                       Gson gson) {
        super(definition, componentProvider, i18nTranslator, chooserController, appDescriptorRegistry);
        this.chooserController = chooserController;
        this.i18nTranslator = i18nTranslator;
        this.assetFieldService = assetFieldService;
        this.cloudinaryService = cloudinaryService;
        this.gson = gson;
    }

    @Override
    protected List<ToolbarGroup> initializeToolbarConfig() {
        final List<ToolbarGroup> defaultToolbar = defaultToolbar();
        defaultToolbar.set(2, new ToolbarGroup("links", "Link", "InternalLink", "DamLink", "Unlink", "Cloudinaryplayer", "Youtube"));
        return defaultToolbar;
    }

    @Override
    protected Component createFieldComponent() {
        MagnoliaCKEditorConfig config = initializeCKEditorConfig();
        richTextEditor = new MagnoliaCKEditorTextField(config);

        if (definition.getHeight() > 0) {
            richTextEditor.setHeight(definition.getHeight(), Sizeable.Unit.PIXELS);
        }

        handleCloudinaryError();
        setupEventListeners();

        return richTextEditor;
    }

    private void setupEventListeners() {
        richTextEditor.addListener((eventName, value) -> {
            switch (eventName) {
                case EVENT_CHOOSE_ASSET:
                    handleCloudinarySelection("resource_type:image", EVENT_ASSET_CHOSEN);
                    break;
                case EVENT_CLOUDINARY_LINK:
                    handleCloudinarySelection("", EVENT_CLOUDINARY_LINK_SELECTED);
                    break;
                case EVENT_CLOUDINARY_VIDEO:
                    handleCloudinarySelection("resource_type:video", EVENT_VIDEO_CHOSEN);
                    break;
                case EVENT_CLOUDINARY_CHAPTERS_FILE:
                    handleCloudinarySelection("resource_type:raw", EVENT_VTT_CHOSEN);
                    break;
                case EVENT_GET_MAGNOLIA_LINK:
                    handleMagnoliaLinkEvent(value);
                    break;
            }
        });
    }

    private void handleCloudinarySelection(String searchExpression, String successEvent) {
        try {
            cloudinaryService.executeCloudinaryMediaLibrary(searchExpression, null, null, null, CLOUDINARY_METHOD_HANDLER, CLOUDINARY_ERROR_METHOD_HANDLER);

            JavaScript.getCurrent().addFunction(CLOUDINARY_METHOD_HANDLER, arguments -> {
                try {
                    String assetDetailsJson = arguments.getString(0);
                    CloudinaryAsset selectedAsset = cloudinaryService.createCloudinaryAsset(assetDetailsJson);
                    handleAssetSelection(selectedAsset, successEvent);
                } catch (Exception e) {
                    Notification.show("Error processing selection", Notification.Type.ERROR_MESSAGE);
                    log.error("Error handling Cloudinary selection", e);
                }
            });
        } catch (Exception e) {
            log.error("Failed to open Cloudinary Media Library", e);
            richTextEditor.firePluginEvent(EVENT_CANCEL_LINK,
                    i18nTranslator.translate(LABEL_UNABLE_TO_OPEN_CHOOSER));
        }
    }



    private void handleAssetSelection(CloudinaryAsset selectedAsset, String successEvent) {
        UI.getCurrent().access(() -> {
            try {
                CloudinaryFileBrowser fileBrowser = assetFieldService.getCloudinaryFileBrowser(selectedAsset);
                String eventJson = gson.toJson(fileBrowser);
                richTextEditor.firePluginEvent(successEvent, eventJson);
            } catch (Exception e) {
                log.error("Error processing selected asset", e);
                richTextEditor.firePluginEvent(EVENT_CANCEL_LINK,
                        i18nTranslator.translate(LABEL_UNABLE_TO_OPEN_CHOOSER));
            }
        });
    }

    private void handleMagnoliaLinkEvent(String value) {
        try {
            RichTextFieldFactory.PluginData data = gson.fromJson(value, RichTextFieldFactory.PluginData.class);
            this.linkMagnoliaPage(data.workspace, data.path);
        } catch (Exception e) {
            log.error("Unable to open Magnolia link chooser dialog", e);
            richTextEditor.firePluginEvent(EVENT_CANCEL_LINK,
                    i18nTranslator.translate(LABEL_UNABLE_TO_OPEN_CHOOSER));
        }
    }

    @Override
    protected MagnoliaCKEditorConfig initializeCKEditorConfig() {
        definition.setFontSizes("H1/56px;H2/44px;H3/36px;H4/24px;H5/18px;Caption/14px");
        MagnoliaCKEditorConfig config = super.initializeCKEditorConfig();
        configurePlugins(config);
        return config;
    }

    private void configurePlugins(MagnoliaCKEditorConfig config) {
        boolean isCustomConfig = StringUtils.isNotBlank(definition.getConfigJsFile());
        String path = VaadinService.getCurrentRequest().getContextPath();

        if (definition.isImages() || isCustomConfig) {
            config.addExternalPlugin(FILE_BROWSER_PLUGIN, path + CLOUDINARY_FILE_BROWSER_PLUGIN_PATH);
            config.addListenedEvent(EVENT_CHOOSE_ASSET);
            if (!isCustomConfig) {
                config.addToExtraPlugins(FILE_BROWSER_PLUGIN);
            }
            config.setFilebrowserImageBrowseLinkUrl(FILE_BROWSER_PLUGIN);
            config.setFilebrowserImageBrowseUrl(FILE_BROWSER_PLUGIN);
        }

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

        config.addExternalPlugin(COLOR_DIALOG_PLUGIN, path + COLOR_DIALOG_PLUGIN_PATH);
        config.addToExtraPlugins(COLOR_DIALOG_PLUGIN);

        config.addExternalPlugin(COLOR_BUTTON_PLUGIN, path + COLOR_BUTTON_PLUGIN_PATH);
        config.addToExtraPlugins(COLOR_BUTTON_PLUGIN);

        config.addExtraConfig(FONT_SIZES_DEFAULT_LABEL, CKEDITOR_FONT_SIZES_DEFAULT_LABEL);

        config.addExtraConfig(COLORS_BUTTON_CONFIG_NAME, CKEDITOR_COLORS);

        config.addExtraConfig(FONT_SIZES_CONFIG_NAME, CKEDITOR_FONT_SIZES);

        List<ToolbarGroup> toolbarGroupList = new ArrayList<>();
        toolbarGroupList.add(new ToolbarGroup(TOOLBAR_STYLES, FORMAT_PLUGIN_DROPDOWN_NAME));
        config.addToolbarLine(toolbarGroupList);
    }

    private void linkMagnoliaPage(String workspace, String path) {
        chooserController.openChooser(createChooserDefinition(APP_PAGES), SessionUtil.getNode(path, workspace))
        .whenComplete((result, e) -> {

            if (e != null) {
                String error =
                        i18nTranslator.translate(LABEL_UNABLE_TO_ACCESS_SELECTION);
                log.error(error, e);
                richTextEditor.firePluginEvent(EVENT_CANCEL_LINK, error);
                return;
            }

            if (result == null || !result.isChosen()) {
                richTextEditor.firePluginEvent(EVENT_CANCEL_LINK);
                return;
            }

            if (result.getChoice().isPresent()) {
                MagnoliaLink mlink =
                        createMagnoliaLink(result.getChoice().get());
                richTextEditor.firePluginEvent(
                        EVENT_SEND_MAGNOLIA_LINK,
                        gson.toJson(mlink));
            } else {
                richTextEditor.firePluginEvent(EVENT_CANCEL_LINK);
            }
        });
    }

    private MagnoliaLink createMagnoliaLink(Node node) {
        MagnoliaLink mlink = new MagnoliaLink();
        mlink.identifier = PropertyUtil.getString(node, JcrConstants.JCR_UUID);
        mlink.repository = RepositoryConstants.WEBSITE;
        mlink.path = NodeUtil.getPath(node);
        mlink.caption = PropertyUtil.getString(node, PROPERTY_TITLE, NodeUtil.getName(node));
        return mlink;
    }

    protected ChooserDefinition<Node, SingleItemWorkbenchChooser<Node>> createChooserDefinition(String appName) {
        SingleItemWorkbenchChooserDefinition<Node> definition = new SingleItemWorkbenchChooserDefinition<>();
        AppAwareWorkbenchChooserDefinition<Node> workbenchChooserDefinition = new AppAwareWorkbenchChooserDefinition<>();
        workbenchChooserDefinition.setAppName(appName);
        definition.setWorkbenchChooser(workbenchChooserDefinition);
        return definition;
    }
}
