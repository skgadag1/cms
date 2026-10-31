package com.tbscg.doosan.magnolia.dxp.core.setup.task.bootstrap;

import com.tbscg.doosan.magnolia.dxp.core.setup.placeholder.PlaceholderFacade;
import info.magnolia.module.InstallContext;
import info.magnolia.module.delta.TaskExecutionException;

import static javax.jcr.ImportUUIDBehavior.IMPORT_UUID_COLLISION_REPLACE_EXISTING;

/**
 * Task for bootstrapping module for given @{@link DxpResourceDynamicFilter}.
 *
 * It additionally resolves and applies all placeholders to JCR.
 */
class DxpModuleBootstrapTask extends DxpModuleBootstrapAbstractTask {

    private static final String TASK_NAME = "Bootstrap";
    private static final String TASK_DESC = "Bootstraps the necessary module repository content.";

    DxpModuleBootstrapTask(DxpResourceAbstractFilter resourceFilter, PlaceholderFacade placeholderFacade) {
        super(TASK_NAME, TASK_DESC, IMPORT_UUID_COLLISION_REPLACE_EXISTING, resourceFilter, placeholderFacade);
    }

    @Override
    public void execute(InstallContext installContext) throws TaskExecutionException {
        super.execute(installContext);

        resolveAndApplyAllPlaceholders(installContext);
    }

    private void resolveAndApplyAllPlaceholders(InstallContext installContext) {
        String[] resourcesToBootstrap = this.getResourcesToBootstrap(installContext);
        placeholderFacade.apply(resourcesToBootstrap, resourceFilter.getModuleName(installContext));
    }
}
