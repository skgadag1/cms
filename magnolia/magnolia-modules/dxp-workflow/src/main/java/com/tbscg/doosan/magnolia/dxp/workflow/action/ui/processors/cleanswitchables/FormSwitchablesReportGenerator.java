package com.tbscg.doosan.magnolia.dxp.workflow.action.ui.processors.cleanswitchables;

import info.magnolia.config.NamedDefinition;
import info.magnolia.ui.editor.ConfiguredFormDefinition;
import info.magnolia.ui.editor.FormDefinition;
import info.magnolia.ui.editor.SwitchableFormDefinition;
import info.magnolia.ui.field.CompositeFieldDefinition;
import info.magnolia.ui.field.ConfiguredSwitchableFieldDefinition;
import info.magnolia.ui.field.EditorPropertyDefinition;
import info.magnolia.ui.field.JcrMultiFieldDefinition;
import lombok.extern.slf4j.Slf4j;

import java.util.*;
import java.util.regex.Pattern;

/**
 * This class is responsible for generating a report of the switchable fields of a form.
 * <p>
 * The report is a list of {@link SwitchableFieldDesc}
 */
@Slf4j
public class FormSwitchablesReportGenerator {

    private static final int PATH_STACK_INITIAL_CAPACITY = 16;
    private static final int INITIAL_PATH_STRING_BUILDER_CAPACITY = 128;
    private static final int INITIAL_FLD_CAPACITY = 16;

    private final FormDefinition<?> formDefinition;
    private final Deque<String> currentPathPatternBuilder = new ArrayDeque<>(PATH_STACK_INITIAL_CAPACITY);
    private final List<SwitchableFieldDesc> fieldDescList = new ArrayList<>(INITIAL_FLD_CAPACITY);

    public FormSwitchablesReportGenerator(final FormDefinition<?> formDefinition) {
        this.formDefinition = formDefinition;
    }

    /**
     * Generates a report of the switchable fields of a form.
     *
     * @return Instance of {@link FormSwitchablesReport}
     */
    public FormSwitchablesReport build() {
        log.debug("Generating form switchables report. For formDefinition: {}", this.formDefinition.getName());

        this.handleProperties(this.formDefinition.getProperties());
        return FormSwitchablesReport.builder()
                .withFieldDescList(this.fieldDescList)
                .build();
    }

    private void handleProperties(final Iterable<? extends EditorPropertyDefinition> editorPropertyDefinitions) {
        if (editorPropertyDefinitions == null) {
            return;
        }
        editorPropertyDefinitions.forEach(this::handleProperty);
    }

    private void handleProperty(final NamedDefinition editorPropertyDefinition) {
        log.debug("Handling property: {} of type {}.", editorPropertyDefinition.getName(), editorPropertyDefinition.getClass());
        if (editorPropertyDefinition instanceof CompositeFieldDefinition) {
            this.handleCompositeField((FormDefinition<?>) editorPropertyDefinition);
        } else if (editorPropertyDefinition instanceof JcrMultiFieldDefinition) {
            final String editorPropertyDefinitionName = editorPropertyDefinition.getName();

            this.currentPathPatternBuilder.push(editorPropertyDefinitionName + "/" + editorPropertyDefinitionName + "\\d+?");
            this.handleJcrMultiFieldDefinition((JcrMultiFieldDefinition) editorPropertyDefinition);
            this.currentPathPatternBuilder.pop();
        } else if (editorPropertyDefinition instanceof ConfiguredSwitchableFieldDefinition) {
            this.currentPathPatternBuilder.push(editorPropertyDefinition.getName());
            this.handleConfiguredSwitchableFieldDefinition((SwitchableFormDefinition<?>) editorPropertyDefinition);
            this.currentPathPatternBuilder.pop();
        }
    }

    private void handleConfiguredSwitchableFieldDefinition(
            final SwitchableFormDefinition<?> switchableFieldDefinition) {
        log.debug("Handling configured switchable field definition: {}", switchableFieldDefinition.getName());

        final List<? extends FormDefinition<?>> fieldDefinitionForms = switchableFieldDefinition.getForms();

        final Map<String, List<String>> valueToPropertyNameList = new HashMap<>(fieldDefinitionForms.size(), 1.0f);
        for (final FormDefinition<?> fieldDefinitionForm : fieldDefinitionForms) {
            if (!(fieldDefinitionForm instanceof ConfiguredFormDefinition)) {
                continue;
            }

            final List<String> propertyPaths = new PropertyPathListGenerator(fieldDefinitionForm).generatePropertyPaths();

            log.debug("Property paths: {}", propertyPaths);
            valueToPropertyNameList.put(fieldDefinitionForm.getName(), propertyPaths);
        }

        final SwitchableFieldDesc switchableFieldDesc = SwitchableFieldDesc.builder()
                .withFieldPathPattern(compileFieldPathPattern(this.buildCurrentPathRegexp()))
                .withValueToPropertyList(valueToPropertyNameList)
                .build();

        log.debug("adding switchableFieldDesc: {}", switchableFieldDesc);
        this.fieldDescList.add(switchableFieldDesc);
    }

    private static Pattern compileFieldPathPattern(final String pathRegexp) {
        return Pattern.compile("^.*/" + pathRegexp + "$");
    }

    private String buildCurrentPathRegexp() {
        final Iterator<String> iterator = this.currentPathPatternBuilder.descendingIterator();
        final StringBuilder sb = new StringBuilder(INITIAL_PATH_STRING_BUILDER_CAPACITY);

        int count = 0;
        while (iterator.hasNext()) {
            if (count > 0) {
                sb.append("/");
            }
            sb.append(iterator.next());
            count++;
        }
        return sb.toString();
    }

    private void handleJcrMultiFieldDefinition(final JcrMultiFieldDefinition jcrMultiFieldDefinition) {
        log.debug("Handling jcr multi field definition: {}", jcrMultiFieldDefinition.getField());
        this.handleProperty(jcrMultiFieldDefinition.getField());
    }

    private void handleCompositeField(final FormDefinition<?> compositeFieldDefinition) {
        log.debug("Handling composite field: {}", compositeFieldDefinition.getName());
        this.handleProperties(compositeFieldDefinition.getProperties());
    }
}
