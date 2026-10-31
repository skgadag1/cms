package com.tbscg.doosan.magnolia.cloudinary.field.factory;

import com.cloudinary.utils.StringUtils;
import com.tbscg.doosan.magnolia.cloudinary.contentapp.CustomCaptionFilteringDataProviderWrapper;
import com.tbscg.doosan.magnolia.cloudinary.contentapp.CustomChooseDialogCallback;
import com.tbscg.doosan.magnolia.cloudinary.contentapp.FilteredAssetContentConnectorDefinition;
import com.tbscg.doosan.magnolia.cloudinary.contentapp.filter.AssetFilterDefinition;
import com.tbscg.doosan.magnolia.cloudinary.contentapp.filter.AssetPropertyFilter;
import com.tbscg.doosan.magnolia.cloudinary.dam.CloudinaryAsset;
import com.tbscg.doosan.magnolia.cloudinary.field.definition.AssetLinkFieldDefinition;
import com.tbscg.doosan.magnolia.cloudinary.util.CloudinaryConstants;
import com.tbscg.doosan.magnolia.cloudinary.util.CloudinaryMediaLibraryService;
import com.vaadin.data.HasValue;
import com.vaadin.data.provider.DataProvider;
import com.vaadin.ui.*;
import info.magnolia.dam.api.*;
import info.magnolia.dam.app.data.AssetSelectFieldSupport;
import info.magnolia.i18nsystem.I18nizer;
import info.magnolia.objectfactory.ComponentProvider;
import info.magnolia.ui.api.app.AppController;
import info.magnolia.ui.api.app.ChooseDialogCallback;
import info.magnolia.ui.api.context.UiContext;
import info.magnolia.ui.chooser.definition.ChooserDefinition;
import info.magnolia.ui.dialog.DialogDefinitionRegistry;
import info.magnolia.ui.editor.ItemPreviewDefinition;
import info.magnolia.ui.field.LinkField;
import info.magnolia.ui.field.factory.ComboBoxFieldFactory;
import info.magnolia.ui.framework.overlay.ChooserController;
import info.magnolia.ui.preview.ItemPreviewComponent;
import info.magnolia.ui.vaadin.integration.contentconnector.ContentConnectorDefinition;

import javax.inject.Inject;
import java.util.*;
import java.util.concurrent.CompletableFuture;

import static com.tbscg.doosan.magnolia.cloudinary.util.CloudinaryConstants.CLOUDINARY_ERROR_METHOD_HANDLER;
import static com.tbscg.doosan.magnolia.cloudinary.util.CloudinaryConstants.CLOUDINARY_METHOD_HANDLER;
import static com.tbscg.doosan.magnolia.cloudinary.util.CloudinaryUtils.handleCloudinaryError;

public class AssetLinkFieldFactory<D extends AssetLinkFieldDefinition<Item>> extends ComboBoxFieldFactory<Item, AssetLinkFieldDefinition<Item>> {

    private final D definition;
    private final ComponentProvider componentProvider;
    private final AssetSelectFieldSupport selectFieldSupport;
    private final ChooserController chooserController;
    private final DialogDefinitionRegistry dialogDefinitionRegistry;
    private final AssetProviderRegistry assetProviderRegistry;
    private final I18nizer i18nizer;
    private final AppController appController;
    private final UiContext uiContext;
    private final CloudinaryMediaLibraryService cloudinaryMediaLibraryService;
    private LinkField<Item> linkField;


    @Inject
    public AssetLinkFieldFactory(D definition, ComponentProvider componentProvider,
                                 AssetSelectFieldSupport selectFieldSupport,
                                 ChooserController chooserController, AssetProviderRegistry assetProviderRegistry,
                                 DialogDefinitionRegistry dialogDefinitionRegistry,
                                 I18nizer i18nizer, AppController appController, UiContext uiContext,
                                 CloudinaryMediaLibraryService cloudinaryMediaLibraryService) {
        super(definition, componentProvider, selectFieldSupport);
        this.definition = definition;
        this.componentProvider = componentProvider;
        this.selectFieldSupport = selectFieldSupport;
        this.chooserController = chooserController;
        this.assetProviderRegistry = assetProviderRegistry;
        this.dialogDefinitionRegistry = dialogDefinitionRegistry;
        this.i18nizer = i18nizer;
        this.appController = appController;
        this.uiContext = uiContext;
        this.cloudinaryMediaLibraryService = cloudinaryMediaLibraryService;
    }

    @Override
    public HasValue<Item> createField() {
        linkField = new LinkField<>((ComboBox<Item>) super.createField());
        linkField.setCaption(getDefinition().getLabel());
        linkField.setButtonCaptionNew(getDefinition().getButtonSelectNewLabel());
        linkField.setButtonCaptionOther(getDefinition().getButtonSelectOtherLabel());
        linkField.setEditable(getDefinition().isEditable());
        linkField.addClickListener(createButtonClickListener());

        newItemPreviewComponent().ifPresent(linkField::setContentPreview);

        handleCloudinaryError();

        return linkField;
    }

    private void registerHandleCloudinarySelection() {
        JavaScript.getCurrent().addFunction(CLOUDINARY_METHOD_HANDLER, arguments -> {
            try {
                String assetDetailsJson = arguments.getString(0);

                CloudinaryAsset selectedAsset = cloudinaryMediaLibraryService.createCloudinaryAsset(assetDetailsJson);

                UI.getCurrent().access(() -> {
                    if (linkField != null) {
                        linkField.setValue(selectedAsset);
                    }
                });
            } catch (Exception e) {
                Notification.show("Error processing selection", Notification.Type.ERROR_MESSAGE);
            }
        });
    }

    private Button.ClickListener createButtonClickListener() {
        if (definition.getAppName() != null && definition.getAppName().equals(CloudinaryConstants.APP_CLOUDINARY_ASSETS)) {
            return createCloudinaryButtonClickListener();
        }

        return event -> {
            ChooseDialogCallback callback = createChooseDialogCallback();
            Item value = linkField.getValue();
            String compoundId = value != null? value.getItemKey().asString() : null;

            appController.openChooseDialog(this.definition.getAppName(), uiContext, compoundId, callback);
        };
    }

    protected ChooseDialogCallback createChooseDialogCallback() {
        return new CustomChooseDialogCallback() {

            @Override
            public void customizeChooseDialog(ContentConnectorDefinition connectorDef) {
                try {
                    if (connectorDef instanceof FilteredAssetContentConnectorDefinition filteredConnectorDef) {
                        filteredConnectorDef.setFilters(definition.getFilters());
                    }
                } catch (Error error) {
                    Notification.show("An error occured while opening the asset chooser, please try again", Notification.Type.ERROR_MESSAGE).setDelayMsec(3600);
                }
            }

            @Override
            public void onItemChosen(String actionName, final Object chosenValue) {
                               if (chosenValue instanceof ItemKey key) {
                    Item selectedItem = assetProviderRegistry.getProviderById(key.getProviderId()).getItem(key);

                    if (!(selectedItem instanceof Folder)) {
                        linkField.setValue(assetProviderRegistry.getProviderById(key.getProviderId()).getAsset(key));
                    }
                }
                linkField.setEnabled(true);
            }

            @Override
            public void onCancel() {
                linkField.setEnabled(true);
            }
        };
    }

    //TODO: to migrate to in the future when all apps are in v6
    CompletableFuture<ChooserController.ChooseResult<Item>> openChooser(LinkField<Item> linkField) {
        ChooserDefinition<Item, ?> chooser = getDefinition().getChooser(); //compatibility
        if (chooser == null) {
            chooser = (ChooserDefinition<Item, ?>) i18nizer.decorate(dialogDefinitionRegistry.getProvider(definition.getChooserId()).get());
        }
        return chooserController.openChooser(chooser, linkField.getValue())
                .whenComplete((ChooserController.ChooseResult<Item> result, Throwable err) -> {
                    linkField.setEnabled(true);
                    if (err != null) {
                        throw new RuntimeException(err);
                    }
                    result.getChoice().ifPresent(linkField::setValue);
                });
    }

    private Button.ClickListener createCloudinaryButtonClickListener() {
        return event -> {
            String searchExpression = buildCloudinarySearchExpression();
            String resourceType = null;
            String type = null;
            String publicId = null;

            if (linkField.getValue() != null && linkField.getValue() instanceof CloudinaryAsset cloudinaryAsset) {
                resourceType = cloudinaryAsset.getResourceType();
                type =  cloudinaryAsset.getType();
                publicId = cloudinaryAsset.getPublicId();
            }

            cloudinaryMediaLibraryService.executeCloudinaryMediaLibrary(searchExpression, resourceType, type, publicId, CLOUDINARY_METHOD_HANDLER, CLOUDINARY_ERROR_METHOD_HANDLER);
            registerHandleCloudinarySelection();
        };
    }


    private String buildCloudinarySearchExpression() {
        List<AssetFilterDefinition> filters = definition.getFilters();
        if (filters == null || filters.isEmpty()) {
            return StringUtils.EMPTY;
        }

        for (AssetFilterDefinition filter : filters) {
            if (filter instanceof AssetPropertyFilter propertyFilter) {
                return propertyFilter.getName() + ":" + propertyFilter.getValue();
            }
        }
        return StringUtils.EMPTY;
    }


    protected Optional<? extends ItemPreviewComponent<Item>> newItemPreviewComponent() {
        return Optional.ofNullable(getDefinition().getPreview())
                .map(ItemPreviewDefinition::getImplementationClass)
                .map(clazz -> componentProvider.newInstance(clazz));
    }

    protected DataProvider<Item, String> getDataProvider() {
        return CustomCaptionFilteringDataProviderWrapper
                .wrap(this.selectFieldSupport.getDataProvider())
                .withFilteringMode(this.getDefinition().getFilteringMode())
                .withItemCaptionGenerator(this.getCaptionGenerator());
    }
}