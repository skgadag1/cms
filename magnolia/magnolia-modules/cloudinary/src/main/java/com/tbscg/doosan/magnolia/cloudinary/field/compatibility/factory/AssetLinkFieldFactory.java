package com.tbscg.doosan.magnolia.cloudinary.field.compatibility.factory;

import com.tbscg.doosan.magnolia.cloudinary.contentapp.CustomChooseDialogCallback;
import com.tbscg.doosan.magnolia.cloudinary.contentapp.FilteredAssetContentConnectorDefinition;
import com.tbscg.doosan.magnolia.cloudinary.field.compatibility.definition.AssetLinkFieldDefinition;
import com.vaadin.ui.Button;
import com.vaadin.v7.data.Item;
import com.vaadin.v7.data.Property;
import com.vaadin.v7.ui.Field;
import info.magnolia.dam.api.ItemKey;
import info.magnolia.dam.external.app.field.AssetLinkField;
import info.magnolia.objectfactory.ComponentProvider;
import info.magnolia.ui.api.app.AppController;
import info.magnolia.ui.api.app.ChooseDialogCallback;
import info.magnolia.ui.api.context.UiContext;
import info.magnolia.ui.api.i18n.I18NAuthoringSupport;
import info.magnolia.ui.form.field.component.ContentPreviewComponent;
import info.magnolia.ui.form.field.definition.LinkFieldDefinition;
import info.magnolia.ui.form.field.factory.AbstractFieldFactory;
import info.magnolia.ui.vaadin.integration.contentconnector.ContentConnectorDefinition;
import org.apache.commons.lang3.StringUtils;

import javax.inject.Inject;
import java.util.Objects;

/**
 * customized version of {@link info.magnolia.dam.external.app.field.factory.AssetLinkFieldFactory<D>}
 * with specific support for cloudinary
 */
@Deprecated
public class AssetLinkFieldFactory<D extends LinkFieldDefinition> extends AbstractFieldFactory<D, String> {

    private final AppController appController;
    private final UiContext uiContext;
    private ComponentProvider componentProvider;

    private AssetLinkField linkField;

    @Inject
    public AssetLinkFieldFactory(D definition, Item relatedFieldItem, UiContext uiContext, I18NAuthoringSupport i18nAuthoringSupport, AppController appController, ComponentProvider componentProvider) {
        super(definition, relatedFieldItem, uiContext, i18nAuthoringSupport);
        this.appController = appController;
        this.uiContext = uiContext;
        this.componentProvider = componentProvider;
    }

    @Override
    public void setComponentProvider(ComponentProvider componentProvider) {
        super.setComponentProvider(componentProvider);
        this.componentProvider = componentProvider;
    }

    @Override
    protected Field<String> createFieldComponent() {
        linkField = new AssetLinkField(definition, componentProvider);
        // Set Caption
        linkField.setButtonCaptionNew(getMessage(definition.getButtonSelectNewLabel()));
        linkField.setButtonCaptionOther(getMessage(definition.getButtonSelectOtherLabel()));
        linkField.getSelectButton().setDisableOnClick(true);
        // Add a callback listener on the select button
        linkField.getSelectButton().addClickListener(createButtonClickListener());

        linkField.setFieldEditable(definition.isFieldEditable());

        String placeholder = definition.getPlaceholder();
        if (placeholder != null && !isMessageKey(placeholder)) {
            linkField.getTextField().setInputPrompt(placeholder);
        }

        // Register the content preview if it's defined.
        if (definition.getContentPreviewDefinition() != null && definition.getContentPreviewDefinition()
                .getContentPreviewClass() != null) {
            linkField.setContentPreview(this.createContentPreview());
        }

        return linkField;
    }

    /**
     * Create the Button click Listener. On click: Create a Dialog and
     * Initialize callback handling.
     */
    private Button.ClickListener createButtonClickListener() {
        return (Button.ClickListener) event -> {
            ChooseDialogCallback callback = createChooseDialogCallback();
            String value = linkField.getTextField().getValue();
            if (StringUtils.isNotBlank(definition.getTargetTreeRootPath())) {
                appController.openChooseDialog(definition.getAppName(), uiContext, definition.getTargetTreeRootPath(), value, callback);
            } else {
                appController.openChooseDialog(definition.getAppName(), uiContext, value, callback);
            }
        };
    }

    protected ChooseDialogCallback createChooseDialogCallback() {
        return new CustomChooseDialogCallback() {

            @Override
            public void customizeChooseDialog(ContentConnectorDefinition connectorDef) {
                if (connectorDef instanceof FilteredAssetContentConnectorDefinition) {
                    AssetLinkFieldDefinition fieldDef = (AssetLinkFieldDefinition) definition;
                    FilteredAssetContentConnectorDefinition filteredConnectorDef = (FilteredAssetContentConnectorDefinition) connectorDef;
                    filteredConnectorDef.setFilters(fieldDef.getFilters());
                }
            }

            @Override
            public void onItemChosen(String actionName, final Object chosenValue) {
                if (chosenValue instanceof ItemKey) {
                    ItemKey key = (ItemKey) chosenValue;

                    // Additional actions, data enrichment during asset selection
                    //ApiObject asset = CloudinaryAsset.fromJcrValue(key.getAssetId());
                    //ApiObject assetDetails = cloudinary.getAssetDetails(asset);
                    //key = new ItemKey(key.getProviderId(), CloudinaryAsset.toJcrValue(assetDetails));

                    String compoundId = key.asString();
                    linkField.setValue(compoundId);
                    linkField.getSelectButton().setEnabled(true);
                }
            }

            @Override
            public void onCancel() {
                linkField.getSelectButton().setEnabled(true);
            }
        };
    }

    private ContentPreviewComponent createContentPreview() {
        final ContentPreviewComponent<?> contentPreviewComponent = componentProvider.newInstance(definition.getContentPreviewDefinition().getContentPreviewClass(), definition.getTargetWorkspace());

        linkField.getTextField().addValueChangeListener((Property.ValueChangeListener) event -> {
            String itemReference = Objects.toString(event.getProperty().getValue(), StringUtils.EMPTY);
            contentPreviewComponent.onValueChange(itemReference);
            contentPreviewComponent.setVisible(StringUtils.isNotBlank(itemReference));
        });

        contentPreviewComponent.onValueChange(linkField.getValue());
        contentPreviewComponent.setVisible(StringUtils.isNotBlank(linkField.getValue()));

        return contentPreviewComponent;
    }

}
