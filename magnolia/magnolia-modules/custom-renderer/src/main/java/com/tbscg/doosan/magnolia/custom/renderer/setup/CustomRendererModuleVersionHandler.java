package com.tbscg.doosan.magnolia.custom.renderer.setup;

import com.tbscg.doosan.magnolia.dxp.core.setup.AutomaticDefaultDxpVersionHandler;

import javax.inject.Inject;

/**
 * Version Handler for custom-renderer module.
 */
public class CustomRendererModuleVersionHandler extends AutomaticDefaultDxpVersionHandler {

    @Inject
    public CustomRendererModuleVersionHandler() {
        super();
    }
}
