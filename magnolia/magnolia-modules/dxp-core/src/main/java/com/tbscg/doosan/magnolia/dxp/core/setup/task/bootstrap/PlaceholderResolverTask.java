package com.tbscg.doosan.magnolia.dxp.core.setup.task.bootstrap;

import com.tbscg.doosan.magnolia.dxp.core.setup.placeholder.PlaceholderFacade;
import info.magnolia.module.InstallContext;
import info.magnolia.module.delta.AbstractTask;
import info.magnolia.objectfactory.Components;

import javax.inject.Inject;

/**
 * Task {@link info.magnolia.module.delta.Task} that when executed
 * resolves placeholders for a given resource name in a given module.
 */
public class PlaceholderResolverTask extends AbstractTask {

    private final String resourceName;

    @Inject
    public PlaceholderResolverTask(String taskName, String taskDescription,
                                   String resourceName) {
        super(taskName, taskDescription);
        this.resourceName = resourceName;
    }

    @Override
    public void execute(InstallContext installContext) {
        PlaceholderFacade placeholderFacade = Components.getComponent(PlaceholderFacade.class);
        placeholderFacade.apply(resourceName, installContext.getCurrentModuleDefinition().getName());
    }
}
