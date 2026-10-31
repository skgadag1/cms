package com.tbscg.doosan.magnolia.facet.configuration.setup;

import com.tbscg.doosan.magnolia.dxp.core.setup.AutomaticDefaultDxpVersionHandler;
import info.magnolia.module.InstallContext;
import info.magnolia.module.delta.Task;

import javax.inject.Inject;
import java.util.Collections;
import java.util.List;

public class FacetConfigurationVersionHandler extends AutomaticDefaultDxpVersionHandler {

    @Inject
    public FacetConfigurationVersionHandler() {
        super();
    }

    @Override
    protected List<Task> getExtraInstallTasks(final InstallContext installContext) {
        return Collections.emptyList();
    }
}
