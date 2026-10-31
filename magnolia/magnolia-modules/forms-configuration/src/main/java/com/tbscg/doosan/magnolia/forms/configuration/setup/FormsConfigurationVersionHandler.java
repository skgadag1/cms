package com.tbscg.doosan.magnolia.forms.configuration.setup;

import com.tbscg.doosan.magnolia.dxp.core.setup.AutomaticDefaultDxpVersionHandler;
import info.magnolia.module.InstallContext;
import info.magnolia.module.delta.Task;

import javax.inject.Inject;
import java.util.Collections;
import java.util.List;

public class FormsConfigurationVersionHandler extends AutomaticDefaultDxpVersionHandler {

    @Inject
    public FormsConfigurationVersionHandler() {
        super();
    }

    @Override
    protected List<Task> getExtraInstallTasks(final InstallContext installContext) {
        return Collections.emptyList();
    }
}
