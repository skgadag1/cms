package com.tbscg.doosan.magnolia.kags.setup;

import com.tbscg.doosan.magnolia.dxp.core.setup.DefaultDxpVersionHandler;
import com.tbscg.doosan.magnolia.dxp.core.setup.task.EnvironmentAwareTask;
import com.tbscg.doosan.magnolia.dxp.core.setup.task.bootstrap.DxpBootstrapSingleModuleResource;
import com.tbscg.doosan.magnolia.dxp.core.util.EnvironmentConstants;
import info.magnolia.jcr.util.NodeTypes;
import info.magnolia.module.InstallContext;
import info.magnolia.module.delta.*;
import info.magnolia.repository.RepositoryConstants;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Provider;
import java.util.Arrays;
import java.util.List;

public class KagsModuleVersionHandler extends DefaultDxpVersionHandler {

	private static final Task ADD_KAGS_CS_VIRTUAL_MAPPING = new DxpBootstrapSingleModuleResource("config.modules.kags.virtualUriMappings.comingSoonKags.yaml",
			EnvironmentConstants.COMMON_INSTANCE, EnvironmentConstants.SHARED_ENVIRONMENT);

	private static final Task ADD_SAFETY_DOCS_ASSETS_APP = new CreateNodeTask(
			"Add safety-docs-app",
			"Add safety docs app to launcher layout",
			RepositoryConstants.CONFIG,
			"/modules/ui-admincentral/config/appLauncherLayout/groups/edit/apps",
			"safety-docs-app",
			NodeTypes.ContentNode.NAME
	);

	private static final Task UPDATE_MULTISITE_CONFIG = new DxpBootstrapSingleModuleResource(
			"config.modules.multisite.config.sites.kags.yaml",
			EnvironmentConstants.COMMON_INSTANCE,
			EnvironmentConstants.SHARED_ENVIRONMENT);

	private static final Task ADD_USER_IMPERSONATOR_FILTER = new DxpBootstrapSingleModuleResource(
			"config.server.filters.userImpersonator.yaml",
			EnvironmentConstants.AUTHOR_INSTANCE,
			EnvironmentConstants.SHARED_ENVIRONMENT);

	private static final Task ORDER_USER_IMPERSONATOR_FILTER = new OrderNodeBeforeTask(
			"/server/filters/userImpersonator",
			"channel");

	private static final Task CONFIGURE_USER_IMPERSONATOR_FILTER = new ArrayDelegateTask(
			"Configure user impersonator filter",
			ADD_USER_IMPERSONATOR_FILTER,
			ORDER_USER_IMPERSONATOR_FILTER
	);

	private static final Task ENABLE_SUPERUSER = new SetPropertyTask(
			"Enable superuser",
			RepositoryConstants.USERS,
			"/system/superuser",
			"enabled", "true"
	);

	private static final Task BOOTSTRAP_CONCEPTS = new DxpBootstrapSingleModuleResource(
			"concepts.kags.yaml",
			EnvironmentConstants.COMMON_INSTANCE,
			EnvironmentConstants.SHARED_ENVIRONMENT);


	private static final Task ORDER_IMPERSONATOR_FILTER_SETUP = new EnvironmentAwareTask(
			"Order user impersonator filter",
			"Bootstraps and orders the user impersonator filter on author instances",
			EnvironmentConstants.AUTHOR_INSTANCE,
			EnvironmentConstants.SHARED_ENVIRONMENT,
			KagsModuleVersionHandler.CONFIGURE_USER_IMPERSONATOR_FILTER);

	@Inject
	public KagsModuleVersionHandler(@Named(EnvironmentConstants.ENVIRONMENT) Provider<String> environment) {
		super();
		register(DeltaBuilder.update("1.2", "")
				.addTask(ADD_KAGS_CS_VIRTUAL_MAPPING)
		);
		register(DeltaBuilder.update("1.3", "Add dependant content apps")
				.addTask(ADD_SAFETY_DOCS_ASSETS_APP));

		register(DeltaBuilder.update("2020.5.19", "Re-bootstrap multisite config for kags site")
				.addTask(UPDATE_MULTISITE_CONFIG));

		register(DeltaBuilder.update("2020.5.28", "Re-bootstrap analytics tags with GTM tag.")
				.addTask(new DxpBootstrapSingleModuleResource("marketing-tags.google-analytics.kags.yaml",
						EnvironmentConstants.COMMON_INSTANCE, environment.get())));

		register(DeltaBuilder.update("2021.9.22", "Order user impersonator filter")
				.addTask(ORDER_IMPERSONATOR_FILTER_SETUP));

		register(DeltaBuilder.update("2021.9.24", "Re-bootstrap pulse insights.")
				.addTask(new DxpBootstrapSingleModuleResource("marketing-tags.pulse-insights.yaml",
						EnvironmentConstants.COMMON_INSTANCE, environment.get())));

		register(DeltaBuilder.update("2021.9.26", "Enable superuser")
				.addTask(ENABLE_SUPERUSER));
	}

	protected List<Task> getExtraInstallTasks(InstallContext installContext) {
		return Arrays.asList(
				new AddURIPermissionTask(
						"Authorize anonymous read on sitemap endpoint",
						"rest-anonymous",
						"/.rest/sitemap/*",
						AddURIPermissionTask.GET),
				new AddPermissionTask("Authorize read access to website workspace",
						"rest-anonymous",
						"website",
						"/*",
						AddURIPermissionTask.GET,
						true),
				ADD_KAGS_CS_VIRTUAL_MAPPING,
				ORDER_IMPERSONATOR_FILTER_SETUP,
				BOOTSTRAP_CONCEPTS);
	}
}
