/**
 * Custom extension of default field binder implementation
 * to enable validators on empty field values.
 */

package com.tbscg.doosan.magnolia.forms.configuration.customs;

import com.vaadin.data.Binder;
import com.vaadin.data.ValidationResult;
import com.vaadin.data.Validator;
import info.magnolia.objectfactory.ComponentProvider;
import info.magnolia.ui.UiFrameworkModule;
import info.magnolia.ui.field.FieldBinder;
import info.magnolia.ui.field.FieldDefinition;
import info.magnolia.ui.field.FieldValidatorDefinition;
import info.magnolia.ui.field.FieldValidatorFactory;
import org.apache.commons.lang3.ArrayUtils;

import javax.inject.Inject;
import javax.inject.Provider;
import java.util.Collection;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static info.magnolia.util.Functions.defaultCombiner;

public class DateFieldWithValidationAlwaysEnabledBinder extends FieldBinder.Default<Object> {

    private final ComponentProvider componentProvider;
    private final Provider<UiFrameworkModule> uiFrameworkModuleProvider;

    @Inject
    DateFieldWithValidationAlwaysEnabledBinder(ComponentProvider componentProvider, Provider<UiFrameworkModule> uiFrameworkModuleProvider) {
        super(componentProvider, uiFrameworkModuleProvider);
        this.componentProvider = componentProvider;
        this.uiFrameworkModuleProvider = uiFrameworkModuleProvider;
    }

    @Override
    public <BT> Binder.BindingBuilder<BT, Object> configureBinding(FieldDefinition<Object> fieldDefinition, Binder.BindingBuilder<BT, Object> source) {
        Binder.BindingBuilder<BT, Object> result = source;
        Object emptyValue = source.getField().getEmptyValue();

        if (fieldDefinition.isRequired()) {
            result.withValidator((value, context) -> {
                Object inputValue = source.getField().getValue();
                if (Objects.equals(inputValue, emptyValue)) {
                    return ValidationResult.error(fieldDefinition.getRequiredErrorMessage());
                }
                return ValidationResult.ok();
            });
        }

        final Collection<FieldValidatorDefinition> defaultFieldValidators = uiFrameworkModuleProvider.get().getDefaultFieldValidators().entrySet().stream()
                .filter(stringCollectionEntry -> {
                    final Class<?> fieldDefinitionClass;
                    try {
                        fieldDefinitionClass = Class.forName(stringCollectionEntry.getKey());
                    } catch (ClassNotFoundException e) {
                        throw new RuntimeException(e);
                    }
                    return fieldDefinitionClass.isAssignableFrom(fieldDefinition.getClass());
                })
                .flatMap(stringCollectionEntry -> stringCollectionEntry.getValue().stream())
                .collect(Collectors.toList());

        final Set<? extends Class<? extends FieldValidatorDefinition>> defaultFieldValidatorClasses = defaultFieldValidators.stream().map(FieldValidatorDefinition::getClass).collect(Collectors.toSet());
        defaultFieldValidators.removeIf(fieldValidatorDefinition -> fieldDefinition.getValidators().stream()
                .map(FieldValidatorDefinition::getClass)
                .anyMatch(defaultFieldValidatorClasses::contains)
        );

        result = Stream.concat(fieldDefinition.getValidators().stream(), defaultFieldValidators.stream())
                .map(this::newValidator)
                .reduce(result, (bttBindingBuilder, validator) ->
                        bttBindingBuilder.withValidator((value, context) -> {
                            //CUSTOM BEGIN
                            Object inputValue = source.getField().getValue();
                            return validator.apply(inputValue, context);
                            //CUSTOM END
                        }), defaultCombiner());

        //noinspection unchecked
        result = createConfiguredConverter(fieldDefinition, result.getField()).map(result::withConverter).orElse((Binder.BindingBuilder) result);

        return result;
    }

    private <VT> Validator<VT> newValidator(FieldValidatorDefinition validatorDefinition, Object... parameters) {
        FieldValidatorFactory fieldValidatorFactory = componentProvider.newInstance(validatorDefinition.getFactoryClass(), ArrayUtils.add(parameters, validatorDefinition));
        return fieldValidatorFactory.createValidator();

    }
}
