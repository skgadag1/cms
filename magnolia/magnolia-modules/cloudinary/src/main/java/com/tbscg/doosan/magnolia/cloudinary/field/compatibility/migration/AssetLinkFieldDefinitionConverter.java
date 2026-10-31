package com.tbscg.doosan.magnolia.cloudinary.field.compatibility.migration;

import com.tbscg.doosan.magnolia.cloudinary.field.definition.AssetLinkFieldDefinition;
import info.magnolia.i18nsystem.SimpleTranslator;
import info.magnolia.objectfactory.Components;
import info.magnolia.ui.chooser.definition.SingleItemWorkbenchChooserDefinition;
import info.magnolia.ui.datasource.jcr.JcrDatasourceDefinition;
import info.magnolia.ui.form.field.definition.migration.CompatibilityAppAwareWorkbenchChooserDefinition;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class AssetLinkFieldDefinitionConverter<T> extends AbstractConfiguredFieldDefinitionConverter<T, AssetLinkFieldDefinition<T>, com.tbscg.doosan.magnolia.cloudinary.field.compatibility.definition.AssetLinkFieldDefinition> {

    private final Map<String, String> APP_MAPPING = new HashMap<String, String>() {{
        put("assets", "dam");
    }};

    @Override
    public AssetLinkFieldDefinition<T> convert(com.tbscg.doosan.magnolia.cloudinary.field.compatibility.definition.AssetLinkFieldDefinition oldDefinition) {
        AssetLinkFieldDefinition<T> newDefinition = super.convert(oldDefinition);
        newDefinition.setButtonSelectOtherLabel(convertButtonLabel(oldDefinition.getButtonSelectOtherLabel(), oldDefinition));
        newDefinition.setButtonSelectNewLabel(convertButtonLabel(oldDefinition.getButtonSelectNewLabel(), oldDefinition));
        newDefinition.setEditable(oldDefinition.isFieldEditable());
        newDefinition.setPlaceholder(oldDefinition.getPlaceholder());
        newDefinition.setAppName(oldDefinition.getAppName());
        newDefinition.setFilters(oldDefinition.getFilters());
        return newDefinition;
    }

    private String convertButtonLabel(String buttonSelectLabel, com.tbscg.doosan.magnolia.cloudinary.field.compatibility.definition.AssetLinkFieldDefinition oldDefinition) {
        return Optional.ofNullable(buttonSelectLabel)
                .map(label -> label.endsWith("buttonSelectOtherLabel") ? oldDefinition.getButtonSelectOtherDefaultLabel() : label)
                .map(label -> label.endsWith("buttonSelectNewLabel") ? oldDefinition.getButtonSelectNewDefaultLabel() : label)
                .orElse(buttonSelectLabel);
    }

    @Override
    AssetLinkFieldDefinition<T> createNewDefinition(com.tbscg.doosan.magnolia.cloudinary.field.compatibility.definition.AssetLinkFieldDefinition oldDefinition) {
        return new AssetLinkFieldDefinition<T>();
    }

    @Override
    T convertDefaultValue(com.tbscg.doosan.magnolia.cloudinary.field.compatibility.definition.AssetLinkFieldDefinition oldDefinition, AssetLinkFieldDefinition<T> newDefinition) {
        return (T) oldDefinition.getDefaultValue();
    }

    private SingleItemWorkbenchChooserDefinition<T> createSingleItemWorkbenchChooser(com.tbscg.doosan.magnolia.cloudinary.field.compatibility.definition.AssetLinkFieldDefinition oldDefinition) {
        CompatibilityAppAwareWorkbenchChooserDefinition<T> appAwareWorkbenchChooserDefinition = new CompatibilityAppAwareWorkbenchChooserDefinition<>();
        appAwareWorkbenchChooserDefinition.setAppName(APP_MAPPING.getOrDefault(oldDefinition.getAppName(), oldDefinition.getAppName()));
        Optional.ofNullable(appAwareWorkbenchChooserDefinition.getDatasource())
                .filter(JcrDatasourceDefinition.class::isInstance)
                .map(JcrDatasourceDefinition.class::cast)
                .ifPresent(datasource -> {
                    Optional.ofNullable(oldDefinition.getTargetTreeRootPath()).ifPresent(datasource::setRootPath);
                });

        SingleItemWorkbenchChooserDefinition<T> chooserDefinition = new SingleItemWorkbenchChooserDefinition<>();
        chooserDefinition.setWorkbenchChooser(appAwareWorkbenchChooserDefinition);
        chooserDefinition.setLabel(Components.getComponent(SimpleTranslator.class).translate("dialogs.chooseDialog.label"));
        return chooserDefinition;
    }

    @Override
    public boolean supports(com.tbscg.doosan.magnolia.cloudinary.field.compatibility.definition.AssetLinkFieldDefinition oldDefinition) {
        return true;
    }

}
