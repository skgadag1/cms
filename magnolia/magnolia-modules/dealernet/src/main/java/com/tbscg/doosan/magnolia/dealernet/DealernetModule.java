package com.tbscg.doosan.magnolia.dealernet;

import lombok.extern.slf4j.Slf4j;
import info.magnolia.module.ModuleLifecycle;
import info.magnolia.module.ModuleLifecycleContext;

/**
 * This class is optional and represents the configuration for the magnolia-module-dealer-net module.
 * By exposing simple getter/setter/adder methods, this bean can be configured via content2bean
 * using the properties and node from <tt>config:/modules/magnolia-module-dealer-net</tt>.
 * If you don't need this, simply remove the reference to this class in the module descriptor xml.
 * See https://documentation.magnolia-cms.com/display/DOCS/Module+configuration for information about module configuration.
 */

@Slf4j
public class DealernetModule implements ModuleLifecycle {
    public static final String MODULE_NAME = "dealernet";

    @Override
    public void start(ModuleLifecycleContext moduleLifecycleContext) {
        log.info("Module {} starting.", DealernetModule.MODULE_NAME);
    }

    @Override
    public void stop(ModuleLifecycleContext moduleLifecycleContext) {
        log.debug("Module {} stopping.", DealernetModule.MODULE_NAME);
    }
}