package com.tbscg.doosan.magnolia.custom.renderer;

import info.magnolia.module.ModuleLifecycle;
import info.magnolia.module.ModuleLifecycleContext;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;

/**
 * This class is optional and represents the configuration for the magnolia-module-custom-renderer module.
 * By exposing simple getter/setter/adder methods, this bean can be configured via content2bean
 * using the properties and node from <tt>config:/modules/magnolia-module-custom-renderer</tt>.
 * If you don't need this, simply remove the reference to this class in the module descriptor xml.
 * See https://documentation.magnolia-cms.com/display/DOCS/Module+configuration for information about module configuration.
 */
@Slf4j
@Getter
@Setter
public class CustomRendererConfig implements ModuleLifecycle {

    private Map<String, String> templateScript;

    public Map<String, String> getTemplateScript() {
        return templateScript;
    }

    public void setTemplateScript(Map<String, String> templateScript) {
        this.templateScript = templateScript;
    }

    @Override
    public void start(ModuleLifecycleContext moduleLifecycleContext) {
        log.debug("START Custom Renderer module");
    }

    @Override
    public void stop(ModuleLifecycleContext moduleLifecycleContext) {
        log.debug("STOP Custom Renderer module");
    }
}
