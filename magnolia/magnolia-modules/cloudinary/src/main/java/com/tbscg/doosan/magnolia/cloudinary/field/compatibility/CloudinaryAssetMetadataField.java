package com.tbscg.doosan.magnolia.cloudinary.field.compatibility;

import com.tbscg.doosan.magnolia.cloudinary.dam.CloudinaryAsset;
import com.tbscg.doosan.magnolia.cloudinary.dam.CloudinaryAssetMetadata;
import com.vaadin.annotations.StyleSheet;
import com.vaadin.ui.AbstractOrderedLayout;
import com.vaadin.ui.Component;
import com.vaadin.ui.FormLayout;
import com.vaadin.v7.data.Item;
import com.vaadin.v7.data.util.BeanItem;
import com.vaadin.v7.ui.CustomField;
import com.vaadin.v7.ui.Field;
import info.magnolia.dam.api.Asset;
import info.magnolia.dam.api.metadata.AssetMetadata;
import info.magnolia.objectfactory.ComponentProvider;
import info.magnolia.ui.form.field.definition.ConfiguredFieldDefinition;
import info.magnolia.ui.form.field.definition.FieldDefinition;
import info.magnolia.ui.form.field.definition.TextFieldDefinition;
import info.magnolia.ui.form.field.factory.FieldFactory;
import info.magnolia.ui.form.field.factory.FieldFactoryFactory;
import org.apache.commons.lang3.StringUtils;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Vaadin {@link Field} for displaying Cloudinary assets metadata.
 *
 * @author Alejandro Puig Escobar
 */
@StyleSheet("vaadin://themes/cloudinary/styles.css")
public class CloudinaryAssetMetadataField extends CustomField<BeanItem> {

    private final FieldFactoryFactory fieldFactoryFactory;
    private final ComponentProvider componentProvider;
    private final Item relatedFieldItem;
    private AbstractOrderedLayout root;

    @Inject
    public CloudinaryAssetMetadataField(FieldFactoryFactory fieldFactoryFactory, ComponentProvider componentProvider,
                                        Item relatedFieldItem) {
        this.fieldFactoryFactory = fieldFactoryFactory;
        this.componentProvider = componentProvider;
        this.relatedFieldItem = relatedFieldItem;
    }

    @Override
    protected Component initContent() {

        //do not display caption label as we already have the corresponding field label in another column
        this.setCaption(null);

        // Initialize root
        root = new FormLayout();
        setWidth(100, Unit.PERCENTAGE);
        setHeight(-1, Unit.PIXELS);
        root.setWidth(100, Unit.PERCENTAGE);
        root.setHeight(-1, Unit.PIXELS);
        root.addStyleName("md-table-border");

        // Initialize Existing field
        this.initFields();
        return root;
    }

    private void initFields() {
        if (relatedFieldItem instanceof BeanItem && ((BeanItem) relatedFieldItem).getBean() instanceof Asset) {
            initFields((BeanItem<Asset>) relatedFieldItem);
        }
    }

    private void initFields(BeanItem<Asset> beanItem) {
        root.removeAllComponents();

        if (beanItem == null || beanItem.getBean() == null) {
            return;
        }

        // Get Asset
        Asset asset = beanItem.getBean();

        if (asset instanceof CloudinaryAsset) {
            CloudinaryAssetMetadata metadata = asset.getMetadata(CloudinaryAssetMetadata.class);

            if (metadata != null) {

                List<ConfiguredFieldDefinition> mdFieldDefinitions = this.createFieldDefinitions(metadata);
                BeanItem<AssetMetadata> mdBeanItem = new BeanItem<>(metadata);

                this.createLocalFields(mdFieldDefinitions, mdBeanItem);
            }
        }
    }

    private void createLocalFields(List<ConfiguredFieldDefinition> mdFieldDefinitions, BeanItem<AssetMetadata> mdBeanItem) {
        for (ConfiguredFieldDefinition fieldDefinition : mdFieldDefinitions) {
            Field<?> field = this.createLocalField(fieldDefinition, mdBeanItem);
            root.addComponent(field);
        }
    }

    private List<ConfiguredFieldDefinition> createFieldDefinitions(CloudinaryAssetMetadata assetMetadata) {
        List<ConfiguredFieldDefinition> fields = new ArrayList<>();
        addMetadataFields(fields, assetMetadata.getContextualMetadata());
        addMetadataFields(fields, assetMetadata.getStructuredMetadata());
        return fields;
    }

    private static void addMetadataFields(List<ConfiguredFieldDefinition> fields, Map<String, Object> metadataMap) {
        if (metadataMap != null) {
            metadataMap.forEach((key, value) ->
                    fields.add(createFieldDefinition(key, value.toString())));
        }
    }

    private static ConfiguredFieldDefinition createFieldDefinition(String key, String value) {
        TextFieldDefinition field = new TextFieldDefinition();
        field.setName(key);
        field.setLabel(key);
        field.setReadOnly(true);
        field.setDefaultValue(value);
        field.setType(value.getClass().getSimpleName());

        return field;
    }

    private Field<?> createLocalField(FieldDefinition fieldDefinition, Object item) {
        FieldFactory fieldfactory = fieldFactoryFactory.createFieldFactory(fieldDefinition, item);
        fieldfactory.setComponentProvider(componentProvider);

        Field<?> field = fieldfactory.createField();

        if (StringUtils.isNotBlank(fieldDefinition.getLabel())) {
            field.setCaption(fieldDefinition.getLabel());
        }

        field.setWidth(100, Unit.PERCENTAGE);
        field.addStyleName("md-row-border");

        return field;
    }

    @Override
    public Class<? extends BeanItem> getType() {
        return BeanItem.class;
    }
}
