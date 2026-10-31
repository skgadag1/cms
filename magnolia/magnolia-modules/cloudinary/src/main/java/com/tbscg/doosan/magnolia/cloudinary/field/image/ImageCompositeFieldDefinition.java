package com.tbscg.doosan.magnolia.cloudinary.field.image;

import info.magnolia.ui.editor.EditorView;
import info.magnolia.ui.field.CompositeFieldDefinition;
import info.magnolia.ui.field.FieldType;

@FieldType("imageCompositeField")
public class ImageCompositeFieldDefinition<T> extends CompositeFieldDefinition<T> {
    @Override
    @SuppressWarnings("unchecked")
    public Class<EditorView<T>> getImplementationClass() {
        return (Class<EditorView<T>>) (Class<?>) ImageCompositeFormView.class;
    }
}
