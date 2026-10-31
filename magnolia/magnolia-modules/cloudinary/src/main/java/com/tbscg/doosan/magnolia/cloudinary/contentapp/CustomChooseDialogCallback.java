package com.tbscg.doosan.magnolia.cloudinary.contentapp;

import info.magnolia.ui.api.app.ChooseDialogCallback;
import info.magnolia.ui.contentapp.CaptionFilteringDataProviderWrapper;
import info.magnolia.ui.vaadin.integration.contentconnector.ContentConnectorDefinition;

/**
 * This callback class is used in (Magnolia UI v5) asset chooser customization
 * to pass the configuration from field definition to content connector definition.
 * This allows us to configure the content connector behavior (like filtering)
 * on the field definition level.
 */
public interface CustomChooseDialogCallback extends ChooseDialogCallback {

    void customizeChooseDialog(ContentConnectorDefinition contentConnectorDefinition);

    static CustomChooseDialogCallback composeWith(Runnable action, ChooseDialogCallback callback) {
        return new CustomChooseDialogCallback() {
            @Override
            public void customizeChooseDialog(ContentConnectorDefinition contentConnectorDefinition) {
                if (callback instanceof CustomChooseDialogCallback) {
                    ((CustomChooseDialogCallback) callback).customizeChooseDialog(contentConnectorDefinition);
                }
            }

            @Override
            public void onCancel() {
                action.run();
                callback.onCancel();
            }

            @Override
            public void onItemChosen(String actionName, Object itemId) {
                action.run();
                callback.onItemChosen(actionName, itemId);
            }
        };
    }
}
