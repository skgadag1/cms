package com.tbscg.doosan.magnolia.cloudinary.field.compatibility.definition;

import info.magnolia.ui.form.field.definition.RichTextFieldDefinition;

/**
 * This class is an extension of {@link info.magnolia.ui.form.field.definition.RichTextFieldDefinition} is necessary
 * to avoid using decorations from rich text field when referencing cloudinaryRichText field in dialogs.
 *
 * @author Alejandro Puig Escobar
 */
@Deprecated
public class CloudinaryRichTextFieldDefinition extends RichTextFieldDefinition {
}
