package com.tbscg.doosan.magnolia.cloudinary.field.image;

import com.tbscg.doosan.magnolia.cloudinary.dam.CloudinaryAsset;
import com.vaadin.data.HasValue;
import com.vaadin.ui.Component;
import info.magnolia.ui.editor.FormDefinition;
import info.magnolia.ui.editor.FormView;
import info.magnolia.ui.editor.LocaleContext;
import info.magnolia.ui.field.FieldDefinition;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;

@Slf4j
public class ImageCompositeFormView<T> extends FormView<T> {

    private static final String SRC = "src";
    private static final String WIDTH = "imageWidth";
    private static final String HEIGHT = "imageHeight";

    public ImageCompositeFormView(FormDefinition<T> formDefinition, LocaleContext localeContext) {
        super(formDefinition, localeContext);
    }

    @Override
    protected Component computeLayout(Map<FieldDefinition<T>, Component> boundFields) {
        HasValue<?> srcField        = findRawField(boundFields, SRC);
        HasValue<String> widthField  = findStringField(boundFields, WIDTH);
        HasValue<String> heightField = findStringField(boundFields, HEIGHT);

        if (srcField != null && widthField != null && heightField != null) {
            srcField.addValueChangeListener(event -> {
                Object value = event.getValue();
                if (value instanceof CloudinaryAsset asset) {
                    setDimensionOrClear(widthField,  asset.getWidth());
                    setDimensionOrClear(heightField, asset.getHeight());
                } else {
                    // image removed / no asset -> clear both
                    widthField.setValue("");
                    heightField.setValue("");
                }
            });
        } else {
            log.warn("Image composite: could not locate src/width/height fields (src={}, width={}, height={})",
                    srcField, widthField, heightField);
        }

        return super.computeLayout(boundFields);
    }

    private HasValue<?> findRawField(Map<FieldDefinition<T>, Component> boundFields, String name) {
        for (Map.Entry<FieldDefinition<T>, Component> e : boundFields.entrySet()) {
            if (name.equals(e.getKey().getName()) && e.getValue() instanceof HasValue) {
                return (HasValue<?>) e.getValue();
            }
        }
        return null;
    }

    /**
     * Returns the field as a String-typed HasValue. imageWidth/imageHeight are declared as
     * textField in image.yaml, so they are String-typed. If a non-String component is ever
     * configured, we skip it (returning null) rather than risk a ClassCastException at setValue.
     */
    @SuppressWarnings("unchecked")
    private HasValue<String> findStringField(Map<FieldDefinition<T>, Component> boundFields, String name) {
        HasValue<?> field = findRawField(boundFields, name);
        if (field == null) {
            return null;
        }
        try {
            return (HasValue<String>) field;
        } catch (ClassCastException ex) {
            log.warn("Field [{}] is not String-typed; skipping width/height auto-populate", name);
            return null;
        }
    }

    /**
     * Sets the dimension value, or clears the field when the value is missing/invalid, so the
     * field never retains a stale value from a previous selection.
     */
    private void setDimensionOrClear(HasValue<String> field, Long value) {
        if (value == null || value <= 0) {
            field.setValue("");
        } else {
            field.setValue(String.valueOf(value));
        }
    }
}
