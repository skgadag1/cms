package com.tbscg.doosan.magnolia.dxp.core.setup;

import com.tbscg.doosan.magnolia.dxp.core.setup.task.bootstrap.DxpModuleBootstrapFacade;
import info.magnolia.module.AbstractModuleVersionHandler;
import info.magnolia.module.InstallContext;
import info.magnolia.module.delta.RegisterModuleServletsTask;
import info.magnolia.module.delta.SetupModuleRepositoriesTask;
import info.magnolia.module.delta.Task;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;

public class DefaultDxpVersionHandler extends AbstractModuleVersionHandler {

    @Inject
    protected DxpModuleBootstrapFacade dxpModuleBootstrapFacade;

    @Override
    protected List<Task> getBasicInstallTasks(InstallContext installContext) {
        final List<Task> basicInstallTasks = new ArrayList<Task>();
        basicInstallTasks.add(new SetupModuleRepositoriesTask());
        basicInstallTasks.addAll(dxpModuleBootstrapFacade.getDynamicAllTasksForModule());
        basicInstallTasks.add(new RegisterModuleServletsTask());

        return basicInstallTasks;
    }
}
