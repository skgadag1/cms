package com.tbscg.doosan.magnolia.feature;

import javax.jcr.RepositoryException;

import info.magnolia.module.InstallContext;
import info.magnolia.module.delta.AbstractRepositoryTask;
import info.magnolia.module.delta.TaskExecutionException;

public class FeatureFlagTask extends AbstractRepositoryTask {

	private final String featureId;
	private FeatureService service;

	public FeatureFlagTask(String featureId, FeatureService service) {
		super("Feature environment flag", "Mark the feature environment '" + featureId + "' as installed.");
		this.featureId = featureId;
		this.service = service;
	}

	@Override
	protected void doExecute(InstallContext ctx) throws RepositoryException, TaskExecutionException {
		this.service.markFeatureInstalled(ctx, featureId);
	}
}
