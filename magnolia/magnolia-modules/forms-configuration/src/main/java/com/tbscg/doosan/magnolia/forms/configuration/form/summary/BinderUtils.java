package com.tbscg.doosan.magnolia.forms.configuration.form.summary;

import com.vaadin.data.Binder;
import com.vaadin.ui.DateField;

import java.util.List;
import java.util.Optional;

public class BinderUtils {

    public static DateField getFieldFromList(List<Binder<?>> fields, String fieldName) throws Exception {
        for (Binder<?> field : fields) {
            Optional<? extends Binder.Binding<?, ?>> binder = field.getBinding(fieldName);
            if (binder.isPresent()) {
                return getBindedField(binder.get());
            }
        }
        throw new Exception("Field " + fieldName + " not exists");
    }

    private static DateField getBindedField(Binder.Binding<?, ?> binder) throws Exception {
        Object result = binder.getField();
        if (result instanceof DateField) {
            return (DateField) result;
        }
        throw new Exception("Binder is not DateField");
    }
}
