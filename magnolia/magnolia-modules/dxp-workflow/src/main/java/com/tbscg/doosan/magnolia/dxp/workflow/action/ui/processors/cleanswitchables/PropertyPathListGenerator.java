package com.tbscg.doosan.magnolia.dxp.workflow.action.ui.processors.cleanswitchables;

import info.magnolia.config.NamedDefinition;
import info.magnolia.ui.editor.ComplexPropertyDefinition;
import info.magnolia.ui.editor.FormDefinition;
import info.magnolia.ui.field.CompositeFieldDefinition;
import info.magnolia.ui.field.EditorPropertyDefinition;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.Deque;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * This method class is used to generate a list of property paths for a given form definition.
 */
@Slf4j
@SuppressWarnings("rawtypes")
class PropertyPathListGenerator {
    private static final int PROPERTY_PATHS_INITIAL_CAPACITY = 16;

    private final List<String> propertyPaths;
    private final Deque<String> propertyPathElements;
    private final FormDefinition<?> fieldDefinitionForm;

    PropertyPathListGenerator(final FormDefinition<?> fieldDefinitionForm) {
        this.fieldDefinitionForm = fieldDefinitionForm;

        this.propertyPaths = new ArrayList<>(PROPERTY_PATHS_INITIAL_CAPACITY);
        this.propertyPathElements = new LinkedList<>();
    }

    List<String> generatePropertyPaths() {
        log.debug("Generating property paths for formDefinition: {}", this.fieldDefinitionForm.getName());

        this.propertyPaths.addAll(collectPaths(this.fieldDefinitionForm.getProperties().stream()));
        this.handleSubFormDefinitions(this.fieldDefinitionForm.getSubFormDefinitions());

        log.debug("Generated property paths: {}", this.propertyPaths);
        return this.propertyPaths;
    }

    private void handleSubFormDefinitions(final Iterable<? extends ComplexPropertyDefinition> subFormDefinitions) {
        subFormDefinitions.forEach(this::handleComplexPropertyDefinition);
    }

    private void handleComplexPropertyDefinition(final NamedDefinition subFormDefinition) {
        if (subFormDefinition instanceof CompositeFieldDefinition) {
            this.handleCompositeFieldDefinition(((FormDefinition<?>) subFormDefinition));
        } else {
            log.debug("Skipping subFormDefinition: {} of type {}.",
                    subFormDefinition.getName(),
                    subFormDefinition.getClass());
        }
    }

    private void handleCompositeFieldDefinition(final FormDefinition<?> compositeFieldDefinition) {
        log.debug("Handling composite field definition: {}", compositeFieldDefinition.getName());

        this.pushPrefixElem(compositeFieldDefinition.getName());

        this.propertyPaths.addAll(collectPaths(
                compositeFieldDefinition.getProperties().stream(),
                this.getPrefix()));
        this.handleSubFormDefinitions(compositeFieldDefinition.getSubFormDefinitions());

        this.popPrefixElem();
    }

    private void popPrefixElem() {
        this.propertyPathElements.removeFirst();
    }

    private void pushPrefixElem(final String name) {
        this.propertyPathElements.addFirst(name);
    }

    private String getPrefix() {
        return String.join("/", this.propertyPathElements) + "/";
    }

    private static List<String> collectPaths(final Stream<? extends EditorPropertyDefinition> propertyDefinitionStream) {
        return collectPaths(propertyDefinitionStream, "");
    }

    private static List<String> collectPaths(final Stream<? extends EditorPropertyDefinition> propertyDefinitionStream, final String prefix) {
        return propertyDefinitionStream
                .map(definition -> prefix + definition.getName())
                .collect(Collectors.toList());
    }
}
