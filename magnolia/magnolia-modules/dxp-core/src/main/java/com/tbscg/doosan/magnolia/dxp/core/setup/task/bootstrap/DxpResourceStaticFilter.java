package com.tbscg.doosan.magnolia.dxp.core.setup.task.bootstrap;

import info.magnolia.module.InstallContext;

class DxpResourceStaticFilter extends DxpResourceAbstractFilter {

    private final String moduleName;

    DxpResourceStaticFilter(String instanceType, String environment, String moduleName) {
        super(instanceType, environment);
        this.moduleName = moduleName;
    }

    @Override
    String getModuleName(InstallContext ctx) {
        return moduleName;
    }


}
