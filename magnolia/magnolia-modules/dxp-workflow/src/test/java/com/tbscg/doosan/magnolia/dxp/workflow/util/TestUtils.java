package com.tbscg.doosan.magnolia.dxp.workflow.util;

import org.apache.commons.lang3.reflect.FieldUtils;

import java.lang.reflect.Field;
import java.util.List;

/**
 * Tests utility class.
 */
public class TestUtils {

    /**
     * Set the field with the given name on the provided targetObject to the supplied value.
     *
     * @param targetObject the target object on which to set the static field
     * @param fieldName    the name of the field to set
     * @param value        the value to set
     * @throws NoSuchFieldException   thrown when field with given name cannot be found in the target object
     * @throws IllegalAccessException thrown when target object or field name is null or where there is access problem
     */
    public static void setField(final Object targetObject, final String fieldName, final Object value)
            throws NoSuchFieldException, IllegalAccessException {
        if (targetObject == null) {
            throw new IllegalAccessException("Target object can't be null");
        }
        if (fieldName == null) {
            throw new IllegalAccessException("Target field can't be null");
        }
        final Field targetField = targetObject.getClass().getDeclaredField(fieldName);
        targetField.setAccessible(true);
        targetField.set(targetObject, value);
    }

    /**
     * Set the first field with the given name found in in the given class of the provided targetObject or its parents
     * to the supplied value.
     *
     * @param targetObject the target object on which to set the static field
     * @param fieldName    the name of the field to set
     * @param value        the value to set
     * @throws NoSuchFieldException   thrown when field with given name cannot be found in the target object
     * @throws IllegalAccessException thrown when target object or field name is null or where there is access problem
     */
    public static void setFieldInSuperclass(final Object targetObject, final String fieldName, final Object value)
            throws NoSuchFieldException, IllegalAccessException {
        if (targetObject == null) {
            throw new IllegalAccessException("Target object can't be null");
        }
        if (fieldName == null) {
            throw new IllegalAccessException("Target field can't be null");
        }
        final List<Field> allFields = FieldUtils.getAllFieldsList(targetObject.getClass());

        final Field targetField = allFields.stream()
                .filter(f -> f.getName().equals(fieldName))
                .findFirst()
                .orElseThrow(NoSuchFieldException::new);
        targetField.setAccessible(true);
        targetField.set(targetObject, value);
    }
}
