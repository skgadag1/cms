package com.tbscg.doosan.magnolia.navigation;

import info.magnolia.module.ModuleLifecycle;
import info.magnolia.module.ModuleLifecycleContext;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class NavigationModule implements ModuleLifecycle {
    private static final String MODULE_NAME = "navigation";

    @Override
    public void start(ModuleLifecycleContext moduleLifecycleContext) {
        log.info("Module {} starting.", NavigationModule.MODULE_NAME);
    }

    @Override
    public void stop(ModuleLifecycleContext moduleLifecycleContext) {
        log.info("Module {} stopping.", NavigationModule.MODULE_NAME);
    }
}
