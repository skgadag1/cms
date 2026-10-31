package com.tbscg.doosan.magnolia.dxp.core.setup.task.bootstrap;

import com.tbscg.doosan.magnolia.dxp.core.setup.placeholder.PlaceholderFacade;
import info.magnolia.module.InstallContext;
import info.magnolia.module.delta.BootstrapSingleModuleResource;
import info.magnolia.module.delta.TaskExecutionException;
import info.magnolia.objectfactory.Components;
import org.apache.jackrabbit.ocm.repository.NodeUtil;

import javax.jcr.ImportUUIDBehavior;

/**
 * Bootstraps a single classpath resources relative to the module's bootstrap folder by instanceType and environment.
 */
public class DxpBootstrapSingleModuleResource extends BootstrapSingleModuleResource {

    private static final String RESOURCE_PATH_FORMAT = "/mgnl-bootstrap/%s/%s/%s/%s";

    private static final String TASK_NAME = "Bootstrap";
    private static final String TASK_DESC = "Bootstraps module resource '%s' from '/mgnl-bootstrap/' folder.";

    private final String instanceType;
    private final String environment;
    private final String resourceName;

    public DxpBootstrapSingleModuleResource(String resourceName, String instanceType, String environment) {
        this(resourceName, instanceType, environment, ImportUUIDBehavior.IMPORT_UUID_COLLISION_REPLACE_EXISTING);
    }

    public DxpBootstrapSingleModuleResource(String resourceName, String instanceType, String environment, int importUUIDBehavior) {
        super(TASK_NAME, String.format(TASK_DESC, getResourcePath(instanceType, environment, resourceName)),
                getResourcePath(instanceType, environment, resourceName), importUUIDBehavior);
        this.instanceType = instanceType;
        this.environment = environment;
        this.resourceName = resourceName;
    }

    private static String getResourcePath(String instanceType, String environment, String resourceName) {
        return instanceType + NodeUtil.PATH_SEPARATOR + environment
                + NodeUtil.PATH_SEPARATOR + resourceName;
    }

    @Override
    public void execute(final InstallContext installContext) throws TaskExecutionException {
        //bootstrap resource
        super.execute(installContext);
        //resolve any possible placeholder
        this.resolvePlaceholders(installContext);
    }

    private void resolvePlaceholders(final InstallContext installContext) {
        String moduleName = installContext.getCurrentModuleDefinition().getName();
        String resourcePath = formatResourcePath(moduleName, this.instanceType, this.environment, this.resourceName);
        PlaceholderFacade placeholderFacade = Components.getComponent(PlaceholderFacade.class);
        placeholderFacade.apply(resourcePath, moduleName);
    }

    private static String formatResourcePath(String moduleName, String instanceType, String environment,
                                             String resourceName) {
        return String.format(RESOURCE_PATH_FORMAT, moduleName, instanceType, environment, resourceName);
    }
}
