package com.tbscg.doosan.magnolia.cloudinary.field.compatibility.migration;

import info.magnolia.objectfactory.Classes;
import info.magnolia.ui.field.ConfiguredFieldDefinition;
import info.magnolia.ui.field.EmailValidatorDefinition;
import info.magnolia.ui.field.FieldValidatorDefinition;
import info.magnolia.ui.field.RegexpValidatorDefinition;
import info.magnolia.ui.form.field.converter.FieldValueConverter;
import info.magnolia.ui.form.field.definition.migration.FieldDefinitionConverter;
import info.magnolia.ui.vaadin.integration.jcr.DefaultPropertyUtil;

import java.util.ArrayList;
import java.util.List;

import javax.jcr.PropertyType;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import com.vaadin.data.Converter;

/**
 * Abstract field definition converter which converts all the properties from ConfiguredFieldDefinition.
 *
 * @param <T> item type used in new definition
 * @param <NEW> new definition class which extends {@link info.magnolia.ui.field.ConfiguredFieldDefinition}
 * @param <OLD> old definition class {@link info.magnolia.ui.form.field.definition.FieldDefinition}
 */
@Slf4j
public abstract class AbstractConfiguredFieldDefinitionConverter<T, NEW extends ConfiguredFieldDefinition<T>, OLD extends info.magnolia.ui.form.field.definition.FieldDefinition> implements FieldDefinitionConverter<T, NEW, OLD> {

    @Override
    public NEW convert(OLD oldDefinition) {
        NEW newDefinition = createNewDefinition(oldDefinition);

        newDefinition.setName(oldDefinition.getName());
        newDefinition.setLabel(oldDefinition.getLabel());
        newDefinition.setI18n(oldDefinition.isI18n());
        newDefinition.setDescription(oldDefinition.getDescription());

        if (newDefinition.getType() == String.class) {
            newDefinition.setType(convertDefinitionType(oldDefinition.getType()));
        }

        newDefinition.setRequired(oldDefinition.isRequired());
        newDefinition.setRequiredErrorMessage(oldDefinition.getRequiredErrorMessage());
        newDefinition.setReadOnly(oldDefinition.isReadOnly());
        newDefinition.setDefaultValue(convertDefaultValue(oldDefinition, newDefinition));
        newDefinition.setStyleName(oldDefinition.getStyleName());
        newDefinition.setValidators(convertValidators(oldDefinition.getValidators()));
        newDefinition.setConverterClass(convertConverterClass(oldDefinition.getConverterClass()));

        return newDefinition;
    }

    abstract NEW createNewDefinition(OLD oldDefinition);

    abstract T convertDefaultValue(OLD oldDefinition, NEW newDefinition);

    protected Class<?> convertDefinitionType(String type) {
        if (StringUtils.isNotBlank(type)) {
            if (DefaultPropertyUtil.isKnownJcrTypeName(type)) {
                return DefaultPropertyUtil.getFieldTypeClass(type);
            } else {
                try {
                    return Classes.getClassFactory().forName(type);
                } catch (ClassNotFoundException e) {
                    log.debug("Unknown configured type {}", type);
                }
            }
        }
        return String.class;
    }

    protected List<FieldValidatorDefinition> convertValidators(List<info.magnolia.ui.form.validator.definition.FieldValidatorDefinition> validators) {
        List<FieldValidatorDefinition> newValidators = new ArrayList<>();
        for (info.magnolia.ui.form.validator.definition.FieldValidatorDefinition oldValidator : validators) {
            if (oldValidator instanceof info.magnolia.ui.form.validator.definition.EmailValidatorDefinition) {
                EmailValidatorDefinition emailValidatorDefinition = new EmailValidatorDefinition();
                emailValidatorDefinition.setName(((info.magnolia.ui.form.validator.definition.EmailValidatorDefinition) oldValidator).getName());
                emailValidatorDefinition.setErrorMessage(oldValidator.getErrorMessage());
                newValidators.add(emailValidatorDefinition);
            } else if (oldValidator instanceof info.magnolia.ui.form.validator.definition.RegexpValidatorDefinition) {
                RegexpValidatorDefinition regexpValidatorDefinition = new RegexpValidatorDefinition();
                regexpValidatorDefinition.setName(((info.magnolia.ui.form.validator.definition.RegexpValidatorDefinition) oldValidator).getName());
                regexpValidatorDefinition.setErrorMessage(oldValidator.getErrorMessage());
                regexpValidatorDefinition.setPattern(((info.magnolia.ui.form.validator.definition.RegexpValidatorDefinition) oldValidator).getPattern());
                newValidators.add(regexpValidatorDefinition);
            } else {
                log.warn(oldValidator.getClass() + " doesn't have replacement in new ui framework. So it can't be converted and as such is skipped.");
            }
        }
        return newValidators;
    }

    protected Class<? extends Converter<T, ?>> convertConverterClass(Class<?> oldConverterClass) {
        if (oldConverterClass != null && com.vaadin.v7.data.util.converter.Converter.class.isAssignableFrom(oldConverterClass)) {
            try {
                return (Class<? extends Converter<T, ?>>) Class.forName("com.vaadin.data.converter." + oldConverterClass.getSimpleName());
            } catch (ReflectiveOperationException e) {
                log.error("Can't convert {} to Vaadin 8 alternative.", oldConverterClass, e);
            }
        } else {
            if (oldConverterClass != null && FieldValueConverter.class.isAssignableFrom(oldConverterClass)) {
                log.error("{} are not supported in new ui framework. {} can't be converted.", FieldValueConverter.class, oldConverterClass);
            } else {
                log.error("Invalid old configuration. Old converter class [{}] does not match with one of the expected types.", oldConverterClass);
            }
        }
        return null;
    }

    @Override
    public boolean supports(OLD oldDefinition) {
        if (oldDefinition.getTransformerClass() != null && !supportsTransformerClass(oldDefinition.getTransformerClass())) {
            log.error("Unsupported transformer class for field definition: " + oldDefinition.getName() + ": " + oldDefinition.getTransformerClass().getName());
            return false;
        }
        if (oldDefinition.getConverterClass() != null && !supportsConverterClass(oldDefinition.getConverterClass())) {
            log.error("Unsupported converter class for field definition: " + oldDefinition.getName() + ": " + oldDefinition.getConverterClass().getName());
        }
        if (oldDefinition.getConverterClass() == null && oldDefinition.getType() != null && !oldDefinition.getType().equals(PropertyType.TYPENAME_STRING)) {
            if (!supportsType(oldDefinition.getType())) {
                log.error("No converter set for non-String field:" + oldDefinition.getName() + ": " + oldDefinition.getType());
                return false;
            }
            log.debug(oldDefinition.getType() + " type is used in definition " + oldDefinition.getName() + " without proper converterClass.");
        }
        return true;
    }

    protected boolean supportsTransformerClass(Class<?> transformerClass) {
        return false;
    }

    protected boolean supportsConverterClass(Class<?> converterClass) {
        return convertConverterClass(converterClass) != null;
    }

    protected boolean supportsType(String type) {
        return false;
    }
}
