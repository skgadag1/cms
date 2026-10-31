package com.tbscg.doosan.magnolia.dxp.core.setup;

import com.tbscg.doosan.magnolia.dxp.core.util.SecurityConstants;
import info.magnolia.module.InstallContext;
import info.magnolia.module.delta.*;
import info.magnolia.module.model.Version;
import info.magnolia.repository.RepositoryConstants;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * This class manages the versions of dxp-core module.
 *
 * @author Alejandro Puig Escobar
 */
public class DxpCoreVersionHandler extends AutomaticDefaultDxpVersionHandler {

	private static final Task SET_DEFAULT_EXTENSION = new SetPropertyTask(
		"Set default page link extension to empty",
		RepositoryConstants.CONFIG,
		"/server",
		"defaultExtension", ""
	);

	private static final Task ADD_CONTENT_APPROVERS_PUBLISHERS_GROUP = new NewPropertyTask(
			"dxp-content-approvers as publisher group",
			"Enables dxp-content-approvers to approve/decline publishing tasks",
			RepositoryConstants.CONFIG,
			"/modules/workflow-jbpm/tasks/publish/groups",
			SecurityConstants.UserTypes.ContentApprover.Group.NAME,
			SecurityConstants.UserTypes.ContentApprover.Group.NAME
	);

	private static final Task ADD_ADMINS_PUBLISHERS_GROUP = new NewPropertyTask(
			"dxp-admins as publisher group",
			"Enables dxp-admins to approve/decline publishing tasks",
			RepositoryConstants.CONFIG,
			"/modules/workflow-jbpm/tasks/publish/groups",
			SecurityConstants.UserTypes.Admin.Group.NAME,
			SecurityConstants.UserTypes.Admin.Group.NAME
	);

	private static final Task ENABLE_CONTENT_APPROVER_PUBLISHING_ACTION = new NewPropertyTask(
			"Enable publishing for dxp-content-approver",
			"Adds dxp-content-approver role as part of activate availability access rule on pages app",
			RepositoryConstants.CONFIG,
			"/modules/pages/apps/pages/subApps/browser/actions/activate/availability/access/roles",
			SecurityConstants.UserTypes.ContentApprover.Role.NAME,
			SecurityConstants.UserTypes.ContentApprover.Role.NAME
	);

	private static final Task ENABLE_CONTENT_AUTHOR_PUBLISHING_ACTION = new NewPropertyTask(
			"Enable publishing for dxp-content-author",
			"Adds dxp-content-author role as part of activate availability access rule on pages app",
			RepositoryConstants.CONFIG,
			"/modules/pages/apps/pages/subApps/browser/actions/activate/availability/access/roles",
			SecurityConstants.UserTypes.ContentAuthor.Role.NAME,
			SecurityConstants.UserTypes.ContentAuthor.Role.NAME
	);

	public DxpCoreVersionHandler() {
		super();
		register(DeltaBuilder.update("1.1.0", "").addTask(SET_DEFAULT_EXTENSION));
		register(DeltaBuilder.update("1.3.0", "Allow publishing for new roles")
				.addTasks(Arrays.asList(ADD_CONTENT_APPROVERS_PUBLISHERS_GROUP, ADD_ADMINS_PUBLISHERS_GROUP,
						ENABLE_CONTENT_APPROVER_PUBLISHING_ACTION, ENABLE_CONTENT_AUTHOR_PUBLISHING_ACTION)));
	}

	@Override
	public List<Delta> getDeltas(InstallContext installContext, Version from) {
		List<Delta> deltas = new ArrayList<>();

		Version currentVersion = installContext.getCurrentModuleDefinition().getVersion();
		String dxpCoreName = installContext.getCurrentModuleDefinition().getName();
		deltas.add(DeltaBuilder.update(currentVersion,
				"Dry-checking for all DXP modules, dependant on dxp-core module" )
				.addTasks(dxpModuleBootstrapFacade.getStaticPreTasksForAllDependantModules(dxpCoreName)));

		deltas.addAll(super.getDeltas(installContext, from));

		return deltas;
	}

	@Override
	protected List<Task> getExtraInstallTasks(InstallContext installContext) {
		return Arrays.asList(SET_DEFAULT_EXTENSION, ADD_CONTENT_APPROVERS_PUBLISHERS_GROUP, ADD_ADMINS_PUBLISHERS_GROUP,
				ENABLE_CONTENT_APPROVER_PUBLISHING_ACTION, ENABLE_CONTENT_AUTHOR_PUBLISHING_ACTION);
	}
}
