package com.tbscg.doosan.magnolia.dxp.core.setup.task.bootstrap;

import info.magnolia.module.InstallContext;
import org.apache.jackrabbit.ocm.repository.NodeUtil;

/**
 * Allow to filter all classpath resources relative to the module's bootstrap folder
 * by instance type (e.g. author) and environment (e.g. dev)
 */
abstract class DxpResourceAbstractFilter {

    private final String instanceType;
    private final String environment;

    DxpResourceAbstractFilter(String instanceType, String environment) {
        this.instanceType = instanceType;
        this.environment = environment;
    }

    boolean filter(InstallContext ctx, String resourceName) {
        return isResourceIncluded(resourceName, getModuleName(ctx));
    }

    boolean isResourceIncluded(String resourceName, String moduleName) {
        return resourceName.startsWith("/mgnl-bootstrap/" + moduleName + NodeUtil.PATH_SEPARATOR + instanceType +
                NodeUtil.PATH_SEPARATOR + environment);
    }

    abstract String getModuleName(InstallContext ctx);
}
