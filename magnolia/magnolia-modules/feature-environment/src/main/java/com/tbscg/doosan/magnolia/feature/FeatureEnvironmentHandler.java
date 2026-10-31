package com.tbscg.doosan.magnolia.feature;

import com.tbscg.doosan.magnolia.dxp.core.setup.DefaultDxpVersionHandler;
import info.magnolia.module.InstallContext;
import info.magnolia.module.delta.Delta;
import info.magnolia.module.delta.DeltaBuilder;
import info.magnolia.module.delta.Task;
import info.magnolia.module.model.Version;
import lombok.extern.slf4j.Slf4j;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Provider;
import java.util.ArrayList;
import java.util.List;

@Slf4j
public abstract class FeatureEnvironmentHandler extends DefaultDxpVersionHandler {

	@Inject
	@Named("magnolia.feature.environment.id")
	protected Provider<String> featureEnvironmentId;

	@Inject
	private FeatureService service;

	protected abstract List<Task> getFeatureBootstrapTasks(InstallContext installContext);

	@Override
	public List<Delta> getDeltas(InstallContext ctx, Version from) {
		List<Delta> deltas = super.getDeltas(ctx, from);
		String feature = featureEnvironmentId.get();
		log.debug("feature configured: {}", feature);
		if (this.service.featureNeedsBootstrapping(ctx, feature)) {
			log.debug("feature environment not yet bootstrapped, creating delta...");
			deltas = new ArrayList<>(deltas);
			deltas.add(createFeatureDelta(ctx, feature));
		}
		return deltas;
	}

	private Delta createFeatureDelta(InstallContext ctx, String feature) {
		Version version = ctx.getCurrentModuleDefinition().getVersion();
		return DeltaBuilder.install(version, "Bootstrapping feature environment: " + feature)
				.addTasks(getFeatureBootstrapTasks(ctx))
				.addTask(new FeatureFlagTask(feature, service));
	}
}
