package com.tbscg.doosan.magnolia.dxp.core.setup;

import info.magnolia.module.InstallContext;
import info.magnolia.module.delta.Delta;
import info.magnolia.module.delta.DeltaBuilder;
import info.magnolia.module.model.Version;

import java.util.ArrayList;
import java.util.List;

/**
 * Bootstrap always given module when updating it, THEN applies custom registered deltas.
 * Additionally, extends @{@link DefaultDxpVersionHandler} which also bootstrap given module on first installation.
 */
public class AutomaticDefaultDxpVersionHandler extends DefaultDxpVersionHandler {

    @Override
    protected List<Delta> getUpdateDeltas(InstallContext installContext, Version from) {
        List<Delta> deltas = new ArrayList<>();
        // first apply bootstrap files
        deltas.add(reboostrapModule(installContext));
        // then apply custom deltas
        deltas.addAll(super.getUpdateDeltas(installContext, from));
        return deltas;
    }

    /**
     * Bootstraps all the files inside dxp-core by DeltaBuilder
     *
     * @param ctx InstallContext
     * @return DeltaBuilder bootstrapping the files.
     */
    private Delta reboostrapModule(InstallContext ctx) {
        String moduleName = ctx.getCurrentModuleDefinition().getName();
        Version currentVersion = ctx.getCurrentModuleDefinition().getVersion();

        return DeltaBuilder.update(currentVersion,
                "Bootstrapping Magnolia Module: " + moduleName)
                .addTasks(dxpModuleBootstrapFacade.getDynamicAllTasksForModule());
    }
}
