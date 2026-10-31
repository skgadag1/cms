package com.tbscg.doosan.magnolia.dxp.core.setup.task.bootstrap;

import com.tbscg.doosan.magnolia.dxp.core.setup.placeholder.PlaceholderFacade;
import info.magnolia.module.InstallContext;
import info.magnolia.module.delta.BootstrapResourcesTask;

import static javax.jcr.ImportUUIDBehavior.IMPORT_UUID_COLLISION_REPLACE_EXISTING;

class DxpModuleBootstrapAbstractTask extends BootstrapResourcesTask {

    protected final DxpResourceAbstractFilter resourceFilter;
    protected final PlaceholderFacade placeholderFacade;

    DxpModuleBootstrapAbstractTask(String taskName, String taskDescription, DxpResourceAbstractFilter resourceFilter, PlaceholderFacade placeholderFacade) {
        this(taskName, taskDescription, IMPORT_UUID_COLLISION_REPLACE_EXISTING, resourceFilter, placeholderFacade);
    }

    DxpModuleBootstrapAbstractTask(String taskName, String taskDescription, int importUUIDBehavior, DxpResourceAbstractFilter resourceFilter, PlaceholderFacade placeholderFacade) {
        super(taskName, taskDescription, importUUIDBehavior);
        this.resourceFilter = resourceFilter;
        this.placeholderFacade = placeholderFacade;
    }

    @Override
    protected boolean acceptResource(InstallContext ctx, String resourceName) {
        return resourceFilter.filter(ctx, resourceName) && super.acceptResource(ctx, resourceName);
    }
}
