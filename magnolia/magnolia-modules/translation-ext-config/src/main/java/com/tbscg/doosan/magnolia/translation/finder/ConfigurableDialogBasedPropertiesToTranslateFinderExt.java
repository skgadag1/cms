package com.tbscg.doosan.magnolia.translation.finder;

import com.tbscg.doosan.magnolia.translation.ExcludedTranslationFieldsConfig;
import info.magnolia.i18nsystem.I18nizer;
import info.magnolia.resourceloader.ResourceOrigin;
import info.magnolia.resourceloader.layered.LayeredResource;
import info.magnolia.translation.TranslationModule;
import info.magnolia.translation.ext.core.CtsExtCoreModule;
import info.magnolia.translation.ext.core.finder.DialogBasedPropertiesToTranslateFinderExt;
import info.magnolia.translation.finder.PropertyToTranslate;
import info.magnolia.ui.api.app.registry.AppDescriptorRegistry;
import info.magnolia.ui.dialog.DialogDefinitionRegistry;
import info.magnolia.rendering.template.registry.TemplateDefinitionRegistry;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.jcr.Node;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * CTSX properties finder that filters out fields configured in Magnolia config, even if they are marked as i18n.
 */
public class ConfigurableDialogBasedPropertiesToTranslateFinderExt extends DialogBasedPropertiesToTranslateFinderExt {

    private static final Logger log = LoggerFactory.getLogger(ConfigurableDialogBasedPropertiesToTranslateFinderExt.class);

    @Inject
    public ConfigurableDialogBasedPropertiesToTranslateFinderExt(CtsExtCoreModule module,
                                                                 TranslationModule translationModule,
                                                                 I18nizer i18nizer,
                                                                 DialogDefinitionRegistry dialogDefinitionRegistry,
                                                                 TemplateDefinitionRegistry templateDefinitionRegistry,
                                                                 AppDescriptorRegistry appDescriptorRegistry,
                                                                 ResourceOrigin<LayeredResource> resourceOrigin,
                                                                 String configPath) {
        super(module, translationModule, i18nizer, dialogDefinitionRegistry, templateDefinitionRegistry, appDescriptorRegistry, resourceOrigin, configPath);
    }

    @Override
    public List<PropertyToTranslate> getPropertiesToTranslate(Node node) {
        List<PropertyToTranslate> properties = super.getPropertiesToTranslate(node);
        Set<String> excludedFields = ExcludedTranslationFieldsConfig.getExcludedFields();
        if (excludedFields.isEmpty()) {
            return properties;
        }

        List<PropertyToTranslate> filteredProperties = properties.stream()
                .filter(property -> !ExcludedTranslationFieldsMatcher.matches(excludedFields, property.getPropertyName()))
                .collect(Collectors.toList());

        int excludedCount = properties.size() - filteredProperties.size();
        if (excludedCount > 0) {
            log.debug("Excluded {} translation field(s) using CTSX config property '{}'.",
                    excludedCount,
                    ExcludedTranslationFieldsConfig.EXCLUDED_FIELDS_PROPERTY);
        }

        return filteredProperties;
    }
}
