package com.tbscg.doosan.magnolia.dxp.core.setup.task.bootstrap;

import info.magnolia.module.InstallContext;

class DxpResourceDynamicFilter extends DxpResourceAbstractFilter {

    DxpResourceDynamicFilter(String instanceType, String environment) {
        super(instanceType, environment);
    }

    @Override
    String getModuleName(InstallContext ctx) {
        return ctx.getCurrentModuleDefinition().getName();
    }
}
