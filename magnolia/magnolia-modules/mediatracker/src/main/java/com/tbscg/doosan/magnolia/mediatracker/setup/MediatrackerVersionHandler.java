package com.tbscg.doosan.magnolia.mediatracker.setup;

import com.tbscg.doosan.magnolia.dxp.core.setup.AutomaticDefaultDxpVersionHandler;
import info.magnolia.module.InstallContext;
import info.magnolia.module.delta.Task;

import java.util.Collections;
import java.util.List;

public class MediatrackerVersionHandler extends AutomaticDefaultDxpVersionHandler {

    public MediatrackerVersionHandler() {
        super();
    }

    @Override
    protected List<Task> getExtraInstallTasks(InstallContext installContext) {
        return Collections.emptyList();
    }
}
