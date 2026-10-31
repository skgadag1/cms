package com.tbscg.doosan.magnolia.cloudinary.setup;

import com.tbscg.doosan.magnolia.dxp.core.setup.AutomaticDefaultDxpVersionHandler;
import com.tbscg.doosan.magnolia.dxp.core.util.EnvironmentConstants;
import info.magnolia.module.InstallContext;
import info.magnolia.module.delta.*;
import info.magnolia.repository.RepositoryConstants;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Provider;
import java.util.Arrays;
import java.util.List;

public class CloudinaryVersionHandler extends AutomaticDefaultDxpVersionHandler {

    private static final Task HIDE_ASSETS_APP = new SetPropertyTask(
            "Disable default assets app",
            RepositoryConstants.CONFIG,
            "/modules/ui-admincentral/config/appLauncherLayout/hiddenApps",
            "assets", "assets"
    );

	private static final Task HIDE_NEW_ASSETS_APP = new NewPropertyTask(
			"Hide new assets app",
			"New assets app needs to be hidden after applying the 6.2.3 upgrade",
			RepositoryConstants.CONFIG,
			"/modules/ui-admincentral/config/appLauncherLayout/hiddenApps",
			"dam", "dam"
	);

	private static final String contentViews =
			"/modules/dam-external-app/apps/dam-external-app/subApps/browser/workbench/contentViews";

	private static final Task REMOVE_CONTENT_VIEWS_DEFS = new ArrayDelegateTask(
			"Remove unnecessary definitions",
			new RemovePropertyTask(
					"Remove list view definition",
					contentViews + "/list",
					"class"),
			new RemovePropertyTask(
					"Remove tree view definition",
					contentViews + "/tree",
					"class"),
			new RemovePropertyTask(
					"Remove thumbnail view definition",
					contentViews + "/thumbnail",
					"class")
	);

	@Inject
    public CloudinaryVersionHandler(@Named(EnvironmentConstants.INSTANCE_TYPE) Provider<String> instanceType,
                                    @Named(EnvironmentConstants.ENVIRONMENT) Provider<String> environment) {
		super();
		register(DeltaBuilder.update("1.2", "")
			.addTask(HIDE_ASSETS_APP)
		);
		register(DeltaBuilder.update("1.10", "Hide new assets app.")
			.addTask(HIDE_NEW_ASSETS_APP)
		);
		register(DeltaBuilder.update("1.11", "Remove unnecessary definitions")
			.addTask(REMOVE_CONTENT_VIEWS_DEFS)
		);
	}

	@Override
	protected List<Task> getExtraInstallTasks(InstallContext installContext) {
		return Arrays.asList(
				HIDE_ASSETS_APP,
				HIDE_NEW_ASSETS_APP,
				REMOVE_CONTENT_VIEWS_DEFS
			);
	}
}
