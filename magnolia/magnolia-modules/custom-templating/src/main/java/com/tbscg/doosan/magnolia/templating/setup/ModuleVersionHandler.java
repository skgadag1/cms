package com.tbscg.doosan.magnolia.templating.setup;

import com.tbscg.doosan.magnolia.dxp.core.setup.DefaultDxpVersionHandler;
import com.tbscg.doosan.magnolia.templating.ConfigTemplatingFunctions;
import com.tbscg.doosan.magnolia.templating.ResourcesTemplatingFunctions;
import info.magnolia.module.InstallContext;
import info.magnolia.module.delta.Task;
import info.magnolia.rendering.module.setup.InstallRendererContextAttributeTask;

import java.util.ArrayList;
import java.util.List;

public class ModuleVersionHandler extends DefaultDxpVersionHandler {

	@Override
	protected List<Task> getExtraInstallTasks(InstallContext installContext) {
		List<Task> tasks = new ArrayList<>();
		tasks.add(new InstallRendererContextAttributeTask(
					"rendering",
					"freemarker",
					ResourcesTemplatingFunctions.NAME,
					ResourcesTemplatingFunctions.class.getName()));
		tasks.add(new InstallRendererContextAttributeTask(
					"rendering",
					"freemarker",
					ConfigTemplatingFunctions.NAME,
					ConfigTemplatingFunctions.class.getName()));
		return tasks;
	}
}
