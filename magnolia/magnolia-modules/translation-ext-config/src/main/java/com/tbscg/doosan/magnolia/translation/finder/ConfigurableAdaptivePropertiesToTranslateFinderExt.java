package com.tbscg.doosan.magnolia.translation.finder;

import info.magnolia.module.ModuleRegistry;
import info.magnolia.objectfactory.ComponentProvider;
import info.magnolia.translation.ext.core.finder.AdaptivePropertiesToTranslateFinderExt;
import info.magnolia.translation.ext.core.finder.DialogBasedPropertiesToTranslateFinderExt;
import info.magnolia.translation.finder.PropertiesToTranslateFinder;

/**
 * Adaptive CTSX finder that routes Magnolia to the project-specific filtering implementation.
 */
public class ConfigurableAdaptivePropertiesToTranslateFinderExt extends AdaptivePropertiesToTranslateFinderExt {

    public ConfigurableAdaptivePropertiesToTranslateFinderExt(ComponentProvider componentProvider,
                                                              ModuleRegistry moduleRegistry,
                                                              String formPath) {
        super(componentProvider, moduleRegistry, formPath);
    }

    public ConfigurableAdaptivePropertiesToTranslateFinderExt(ComponentProvider componentProvider,
                                                              ModuleRegistry moduleRegistry) {
        super(componentProvider, moduleRegistry);
    }

    @Override
    protected Class<? extends PropertiesToTranslateFinder> resolveTargetClass(ModuleRegistry moduleRegistry) {
        Class<? extends PropertiesToTranslateFinder> resolvedPropFinderClass = super.resolveTargetClass(moduleRegistry);
        return DialogBasedPropertiesToTranslateFinderExt.class.equals(resolvedPropFinderClass) ?
                ConfigurableDialogBasedPropertiesToTranslateFinderExt.class :
                resolvedPropFinderClass;
    }

    @Override
    protected Class<? extends PropertiesToTranslateFinder> resolveTargetClassForContentAppTranslations() {
        return ConfigurableDialogBasedPropertiesToTranslateFinderExt.class;
    }
}
