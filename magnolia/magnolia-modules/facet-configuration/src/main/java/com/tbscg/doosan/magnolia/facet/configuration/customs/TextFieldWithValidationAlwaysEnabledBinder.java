package com.tbscg.doosan.magnolia.facet.configuration.customs;

/**
 * This file Copyright (c) 2019 Magnolia International
 * Ltd.  (http://www.magnolia-cms.com). All rights reserved.
 *
 *
 * This file is dual-licensed under both the Magnolia
 * Network Agreement and the GNU General Public License.
 * You may elect to use one or the other of these licenses.
 *
 * This file is distributed in the hope that it will be
 * useful, but AS-IS and WITHOUT ANY WARRANTY; without even the
 * implied warranty of MERCHANTABILITY or FITNESS FOR A
 * PARTICULAR PURPOSE, TITLE, or NONINFRINGEMENT.
 * Redistribution, except as permitted by whichever of the GPL
 * or MNA you select, is prohibited.
 *
 * 1. For the GPL license (GPL), you can redistribute and/or
 * modify this file under the terms of the GNU General
 * Public License, Version 3, as published by the Free Software
 * Foundation.  You should have received a copy of the GNU
 * General Public License, Version 3 along with this program;
 * if not, write to the Free Software Foundation, Inc., 51
 * Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * 2. For the Magnolia Network Agreement (MNA), this file
 * and the accompanying materials are made available under the
 * terms of the MNA which accompanies this distribution, and
 * is available at http://www.magnolia-cms.com/mna.html
 *
 * Any modifications to this file must keep this entire header
 * intact.
 *
 */

import com.vaadin.data.ValidationResult;
import com.vaadin.data.Validator;
import info.magnolia.objectfactory.ComponentProvider;
import info.magnolia.ui.UiFrameworkModule;

import java.util.Collection;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.inject.Inject;
import javax.inject.Provider;

import info.magnolia.ui.field.*;

import com.vaadin.data.Binder;
import org.apache.commons.lang3.ArrayUtils;

import static info.magnolia.util.Functions.defaultCombiner;

/**
 * Custom extension of a default field binder implementation
 * with enabling validators on empty values and
 * which applies text field binding bug workaround.
 */
public class TextFieldWithValidationAlwaysEnabledBinder<T> extends TextFieldBinder<T> {
    private final ComponentProvider componentProvider;
    private final Provider<UiFrameworkModule> uiFrameworkModuleProvider;

    @Inject
    TextFieldWithValidationAlwaysEnabledBinder(ComponentProvider componentProvider, Provider<UiFrameworkModule> uiFrameworkModuleProvider) {
        super(componentProvider);
        this.componentProvider = componentProvider;
        this.uiFrameworkModuleProvider = uiFrameworkModuleProvider;
    }

    @Override
    public <BT> Binder.BindingBuilder<BT, T> configureBinding(FieldDefinition<T> fieldDefinition, Binder.BindingBuilder<BT, T> source) {
        Binder.BindingBuilder<BT, T> result = source;
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

        return result.withNullRepresentation(getDefaultNullRepresentation(fieldDefinition));
    }

    private <VT> Validator<VT> newValidator(FieldValidatorDefinition validatorDefinition, Object... parameters) {
        FieldValidatorFactory fieldValidatorFactory = componentProvider.newInstance(validatorDefinition.getFactoryClass(), ArrayUtils.add(parameters, validatorDefinition));
        return fieldValidatorFactory.createValidator();
    }
}

