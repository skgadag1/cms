package com.tbscg.doosan.magnolia.cloudinary.field.definition;


import com.tbscg.doosan.magnolia.cloudinary.field.factory.CloudinaryAssetsEnabledRichTextFieldFactory;
import info.magnolia.ui.field.FieldType;
import info.magnolia.ui.field.RichTextFieldDefinition;

/**
 * This class is an extension of {@link RichTextFieldDefinition}.
 *
 * @author apuigescobar
 */
@FieldType("cloudinaryRichText")
public class CloudinaryRichTextFieldDefinition extends RichTextFieldDefinition {
    public CloudinaryRichTextFieldDefinition() {
        setType(String.class);
        setFactoryClass(CloudinaryAssetsEnabledRichTextFieldFactory.class);
        setHeight(330);
    }
}
