package com.tbscg.doosan.magnolia.faq.setup;

import com.tbscg.doosan.magnolia.dxp.core.setup.AutomaticDefaultDxpVersionHandler;
import info.magnolia.module.InstallContext;
import info.magnolia.module.delta.AddPermissionTask;
import info.magnolia.module.delta.AddURIPermissionTask;
import info.magnolia.module.delta.Task;

import java.util.Arrays;
import java.util.List;

/**
 * This class manages the versions of faq module.
 *
 * @author Alejandro Puig Escobar
 */
public class FaqModuleVersionHandler extends AutomaticDefaultDxpVersionHandler {

    @Override
    protected List<Task> getExtraInstallTasks(InstallContext installContext) {
        return Arrays.asList(
                new AddURIPermissionTask(
                        "Authorize anonymous read on faq endpoint",
                        "rest-anonymous",
                        "/.rest/faq/*",
                        AddURIPermissionTask.GET),
                new AddPermissionTask("Authorize read access to faq workspace",
                        "rest-anonymous",
                        "faq",
                        "/*",
                        AddURIPermissionTask.GET,
                        true)
        );
    }
}
