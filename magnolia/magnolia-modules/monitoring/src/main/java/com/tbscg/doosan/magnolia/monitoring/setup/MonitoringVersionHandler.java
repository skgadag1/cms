package com.tbscg.doosan.magnolia.monitoring.setup;

import com.tbscg.doosan.magnolia.dxp.core.setup.DefaultDxpVersionHandler;
import info.magnolia.module.delta.AddURIPermissionTask;
import info.magnolia.module.delta.DeltaBuilder;
import info.magnolia.module.delta.RemoveNodeTask;
import info.magnolia.module.delta.Task;
import info.magnolia.repository.RepositoryConstants;

public class MonitoringVersionHandler extends DefaultDxpVersionHandler {

	private static final Task MONITORING_ENDPOINT_ANONYMOUS_PERMISSION = new AddURIPermissionTask(
					"Authorize anonymous read on monitoring endpoints",
					"rest-anonymous",
					"/.rest/monitoring/*",
					AddURIPermissionTask.GET
	);

	private static final Task REMOVE_ENDPOINT_DEF_FROM_CONFIG = new RemoveNodeTask(
			"Remove monitoring definition from config",
			"Removes monitoring status endpoint definition from config workspace",
			RepositoryConstants.CONFIG,
			"/modules/monitoring/rest-endpoints"
	);

	public MonitoringVersionHandler() {
		register(DeltaBuilder.update("2019.1.1", "Adds endpoint permissions to anonymous")
				.addTask(MONITORING_ENDPOINT_ANONYMOUS_PERMISSION));
		register(DeltaBuilder.update("2021.2.12", "Refactors monitoring endpoint definition")
				.addTask(REMOVE_ENDPOINT_DEF_FROM_CONFIG));
	}
}
