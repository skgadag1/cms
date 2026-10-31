package com.tbscg.doosan.magnolia.dxp.core.app.column;

import com.vaadin.data.ValueProvider;
import info.magnolia.task.Task;
import info.magnolia.ui.contentapp.configuration.column.ConfiguredColumnDefinition;

import java.util.Map;

/**
 * Path column definition for Tasks app decoration (see tasks-app.yaml).
 *
 * @author aczyszczon
 */
public class TaskPathColumnDefinition extends ConfiguredColumnDefinition {

    @Override
    public Class<? extends ValueProvider> getValueProvider() {
        return TaskRegionValueProvider.class;
    }

    /**
     * Provider for path column value.
     */
    public static class TaskRegionValueProvider implements ValueProvider<Task, String> {

        @Override
        public String apply(final Task task) {
            final Map<String, Object> taskContent = task.getContent();
            return String.valueOf(taskContent.getOrDefault("path", ""));
        }
    }
}
