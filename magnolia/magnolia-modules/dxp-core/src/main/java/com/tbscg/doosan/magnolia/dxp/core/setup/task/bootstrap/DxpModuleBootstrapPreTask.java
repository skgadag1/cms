package com.tbscg.doosan.magnolia.dxp.core.setup.task.bootstrap;

import com.tbscg.doosan.magnolia.dxp.core.setup.placeholder.PlaceholderFacade;
import info.magnolia.module.InstallContext;

/**
 * Task for any dry-check operations for given @{@link DxpResourceDynamicFilter}
 * which can validate correctness up-front of actual bootstrap task.
 *
 * It ensures that all placeholders can be resolved.
 * Any other additional logic which requires checking integrity of bootstrap files should go here.
 */
class DxpModuleBootstrapPreTask extends DxpModuleBootstrapAbstractTask {

    private static final String TASK_NAME = "PreBootstrap";
    private static final String TASK_DESC = "Check if necessary module repository content can be bootstrapped.";

    DxpModuleBootstrapPreTask(DxpResourceAbstractFilter resourceFilter, PlaceholderFacade placeholderFacade) {
        super(TASK_NAME, TASK_DESC, resourceFilter, placeholderFacade);
    }

    @Override
    public void execute(InstallContext installContext) {
        validateAllPlaceholdersCanBeResolved(installContext);
    }

    private void validateAllPlaceholdersCanBeResolved(InstallContext installContext) {
        String[] resourcesToBootstrap = this.getResourcesToBootstrap(installContext);
        placeholderFacade.dryCheck(resourcesToBootstrap, resourceFilter.getModuleName(installContext));
    }
}
