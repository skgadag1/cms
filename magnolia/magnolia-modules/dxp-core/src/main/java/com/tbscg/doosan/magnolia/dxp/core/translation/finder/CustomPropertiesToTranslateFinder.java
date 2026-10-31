package com.tbscg.doosan.magnolia.dxp.core.translation.finder;

import info.magnolia.module.ModuleRegistry;
import info.magnolia.objectfactory.ComponentProvider;
import info.magnolia.translation.finder.AdaptivePropertiesToTranslateFinder;
import info.magnolia.translation.finder.PropertiesToTranslateFinder;

public class CustomPropertiesToTranslateFinder extends AdaptivePropertiesToTranslateFinder {

    public CustomPropertiesToTranslateFinder(ComponentProvider componentProvider, ModuleRegistry moduleRegistry, String formPath) {
        super(componentProvider, moduleRegistry, formPath);
    }

    public CustomPropertiesToTranslateFinder(ComponentProvider componentProvider, ModuleRegistry moduleRegistry) {
        super(componentProvider, moduleRegistry);
    }

    protected Class<? extends PropertiesToTranslateFinder> resolveTargetClassForContentAppTranslations() {
        return CompositeDetailPresenterPropertiesToTranslateFinder.class;
    }
}
