package com.tbscg.doosan.magnolia.feature;

import java.util.Arrays;
import java.util.List;

import info.magnolia.module.InstallContext;
import info.magnolia.module.delta.OrderNodeToFirstPositionTask;
import info.magnolia.module.delta.PathExistenceDelegateTask;
import info.magnolia.module.delta.SetPropertyTask;
import info.magnolia.module.delta.Task;
import info.magnolia.repository.RepositoryConstants;

public class VersionHandler extends FeatureEnvironmentHandler {

	@Override
	protected List<Task> getFeatureBootstrapTasks(InstallContext installContext) {

		return Arrays.asList(
			new OrderNodeToFirstPositionTask("Disable SSO", "server/filters/securityCallback/clientCallbacks/form"),
			new PathExistenceDelegateTask(
					"Reconfigure Publisher (if configured)",
					"Reconfigure Publisher (if configured)",
					new String[] {},
					new String[] {"/modules/publishing-core/config/receivers/magnolia-publisher"},
					new SetPropertyTask(
							"Reconfigure Publisher",
							RepositoryConstants.CONFIG,
							"/modules/publishing-core/config/receivers/magnolia-publisher",
							"url",
							"http://magnolia-publisher:8080"
					)
			)
		);
	}
}
