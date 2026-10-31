package com.tbscg.doosan.magnolia.cloudinary.field.compatibility.migration;

import com.tbscg.doosan.magnolia.cloudinary.field.definition.CloudinaryRichTextFieldDefinition;

public class CloudinaryRichTextFieldDefinitionConverter extends AbstractConfiguredFieldDefinitionConverter<String, CloudinaryRichTextFieldDefinition, com.tbscg.doosan.magnolia.cloudinary.field.compatibility.definition.CloudinaryRichTextFieldDefinition> {

    @Override
    public CloudinaryRichTextFieldDefinition convert(com.tbscg.doosan.magnolia.cloudinary.field.compatibility.definition.CloudinaryRichTextFieldDefinition oldDefinition) {
        CloudinaryRichTextFieldDefinition newDefinition = super.convert(oldDefinition);

        newDefinition.setAlignment(oldDefinition.isAlignment());
        newDefinition.setImages(oldDefinition.isImages());
        newDefinition.setLists(oldDefinition.isLists());
        newDefinition.setSource(oldDefinition.isSource());
        newDefinition.setTables(oldDefinition.isTables());
        newDefinition.setHeight(oldDefinition.getHeight());
        newDefinition.setColors(oldDefinition.getColors());
        newDefinition.setFonts(oldDefinition.getFonts());
        newDefinition.setFontSizes(oldDefinition.getFontSizes());
        newDefinition.setConfigJsFile(oldDefinition.getConfigJsFile());

        return newDefinition;
    }

    protected boolean supportsType(String type) {
        return false;
    }

    @Override
    protected String convertDefaultValue(com.tbscg.doosan.magnolia.cloudinary.field.compatibility.definition.CloudinaryRichTextFieldDefinition oldDefinition,
                                         CloudinaryRichTextFieldDefinition newDefinition) {
        return oldDefinition.getDefaultValue();
    }

    @Override
    protected CloudinaryRichTextFieldDefinition createNewDefinition(com.tbscg.doosan.magnolia.cloudinary.field.compatibility.definition.CloudinaryRichTextFieldDefinition oldDefinition) {
        return new CloudinaryRichTextFieldDefinition();
    }
}
