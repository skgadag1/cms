package com.tbscg.doosan.magnolia.regions.field.datasource;

import info.magnolia.rendering.template.registry.TemplateDefinitionRegistry;
import info.magnolia.ui.datasource.optionlist.Option;
import info.magnolia.ui.datasource.optionlist.OptionListDefinition;
import info.magnolia.objectfactory.Components;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Datasource for template selection that dynamically populates options with available page templates.
 * Templates are retrieved from the TemplateDefinitionRegistry and filtered to only include page templates.
 */
@Slf4j
@Getter
@Setter
public class TemplateOptionListDefinition extends OptionListDefinition {

    private static final String PAGE_TEMPLATE_IDENTIFIER = ":pages";

    private static Map<String, String> cachedTemplateOptions;

    private final TemplateDefinitionRegistry templateDefinitionRegistry;

    public TemplateOptionListDefinition() {
        templateDefinitionRegistry = Components.getComponent(TemplateDefinitionRegistry.class);
    }

    @Inject
    public TemplateOptionListDefinition(TemplateDefinitionRegistry templateDefinitionRegistry) {
        this.templateDefinitionRegistry = templateDefinitionRegistry;
    }

    @Override
    public List<Option> getOptions() {
        if (cachedTemplateOptions == null) {
            cachedTemplateOptions = loadAvailableTemplates();
        }
        
        List<Option> options = new ArrayList<>();
        for (Map.Entry<String, String> entry : cachedTemplateOptions.entrySet()) {
            Option option = new Option();
            option.setValue(entry.getKey());
            option.setLabel(entry.getValue());
            option.setName(entry.getKey());
            options.add(option);
        }
        return options;
    }

    /**
     * Loads all available page templates from the template registry.
     * Only templates containing ":pages" in their ID are included.
     *
     * @return Map of template IDs to their display names
     */
    private Map<String, String> loadAvailableTemplates() {
        Map<String, String> templatesMap = new LinkedHashMap<>();
        
        try {
            templateDefinitionRegistry.getAllDefinitions().stream()
                .filter(templateDefinition -> templateDefinition.getId().contains(PAGE_TEMPLATE_IDENTIFIER))
                .forEach(template -> {
                    String templateId = template.getId();
                    String displayName = template.getTitle() != null && !template.getTitle().isEmpty() 
                        ? template.getTitle() 
                        : templateId;
                    
                    templatesMap.put(templateId, displayName);
                });
            
            log.debug("Loaded {} page templates for selection", templatesMap.size());
            
        } catch (Exception e) {
            log.error("Error loading templates from registry", e);
        }
        
        return templatesMap;
    }
}

