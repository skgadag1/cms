package com.tbscg.doosan.magnolia.sso.setup;

import com.tbscg.doosan.magnolia.dxp.core.setup.AutomaticDefaultDxpVersionHandler;
import com.tbscg.doosan.magnolia.dxp.core.setup.task.bootstrap.DxpBootstrapSingleModuleResource;
import com.tbscg.doosan.magnolia.dxp.core.util.EnvironmentConstants;
import info.magnolia.module.InstallContext;
import info.magnolia.module.delta.DeltaBuilder;
import info.magnolia.module.delta.OrderFilterBeforeTask;
import info.magnolia.module.delta.SetPropertyTask;
import info.magnolia.module.delta.Task;
import info.magnolia.repository.RepositoryConstants;
import org.apache.commons.lang3.StringUtils;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Provider;
import java.util.ArrayList;
import java.util.List;

public class SSOConfigVersionHandler extends AutomaticDefaultDxpVersionHandler {

	private final Provider<String> environment;
	private final Provider<String> instanceType;

	private static final Task ORDER_SESSION_INVALIDATE_FILTER =
			new OrderFilterBeforeTask("sessionInvalidate", new String[]{"securityCallback"});

	private static final Task ORDER_FAIL_AUTHENTICATION_REDIRECT_FILTER =
			new OrderFilterBeforeTask("failAuthenticationRedirect", new String[]{"logout"});

	private static final Task SET_DEFAULT_AUTHOR_SSO_GROUP = new SetPropertyTask(
			"Set default group for Magnolia Author SSO users",
			RepositoryConstants.CONFIG,
			"/modules/sso-connector/config/authenticationServices/OktaAuthorOpenIdConnectService",
			"securityGroups", StringUtils.EMPTY
	);

	private static final Task SET_DEFAULT_AUTHOR_SSO_ROLE = new SetPropertyTask(
			"Set default group for Magnolia Author SSO users",
			RepositoryConstants.CONFIG,
			"/modules/sso-connector/config/authenticationServices/OktaAuthorOpenIdConnectService",
			"securityRoles", StringUtils.EMPTY
	);

	private static final Task LOGIN_FILTERS_REBOOTSTRAPPING = new DxpBootstrapSingleModuleResource(
			"config.server.filters.login.yaml",
			EnvironmentConstants.COMMON_INSTANCE, EnvironmentConstants.SHARED_ENVIRONMENT);

	@Inject
	public SSOConfigVersionHandler(@Named("environment") Provider<String> environment,
								   @Named("instanceType") Provider<String> instanceType) {
		super();
		this.environment = environment;
		this.instanceType = instanceType;

		DeltaBuilder v2222 = DeltaBuilder.update("2222", "Bootstrap Okta SSO configs");
		List.of(
				"config.modules.sso-connector.config.authenticationServices.kags-okta.yaml",
				"config.server.filters.securityCallback.clientCallbacks.kags-okta.yaml",
				"config.modules.sso-connector.config.authenticationServices.kags-key-account-okta.yaml",
				"config.server.filters.securityCallback.clientCallbacks.kags-key-account-okta.yaml"
		).forEach(path -> v2222.addTask(
				new DxpBootstrapSingleModuleResource(
						path,
						EnvironmentConstants.COMMON_INSTANCE,
						environment.get())));
		List.of(
				"config.server.filters.login.yaml",
				"config.server.filters.logout.yaml",
				"config.server.filters.sessionInvalidate.yaml",
				"config.server.filters.uriSecurity.bypasses.customLogout.yaml"
		).forEach(path -> v2222.addTask(
				new DxpBootstrapSingleModuleResource(
						path,
						EnvironmentConstants.COMMON_INSTANCE,
						EnvironmentConstants.SHARED_ENVIRONMENT)));

		register(v2222.addTask(ORDER_SESSION_INVALIDATE_FILTER));

		DeltaBuilder v2223 = DeltaBuilder.update("2223", "Bootstrap conditional redirect configuration");
		List.of(
				"config.server.filters.uriSecurity.bypasses.errorPage.yaml"
		).forEach(path -> v2223.addTask(
				new DxpBootstrapSingleModuleResource(
						path,
						EnvironmentConstants.COMMON_INSTANCE,
						EnvironmentConstants.SHARED_ENVIRONMENT)));

		if (!environment.get().equals(EnvironmentConstants.LOCAL_ENVIRONMENT) &&
				instanceType.get().equals(EnvironmentConstants.AUTHOR_INSTANCE)) {
			register(DeltaBuilder.update("2224", "Set default groups and roles for Magnolia " +
					"Author SSO users").addTasks(List.of(SET_DEFAULT_AUTHOR_SSO_GROUP, SET_DEFAULT_AUTHOR_SSO_ROLE)));
		}

		DeltaBuilder v2225 = DeltaBuilder.update("2225", "Bootstrap redirect on error configuration");
		register(v2225.addTask(new DxpBootstrapSingleModuleResource(
				"config.server.filters.login.yaml",
				EnvironmentConstants.COMMON_INSTANCE,
				EnvironmentConstants.SHARED_ENVIRONMENT)));

		DeltaBuilder v2228 = DeltaBuilder.update("2228",
				"Re-bootstrap Magnolia Author SSO authentication service configuration");

		if (!EnvironmentConstants.LOCAL_ENVIRONMENT.equals(environment.get())) {
			register(v2228.addTask(new DxpBootstrapSingleModuleResource(
					"config.modules.sso-connector.config.authenticationServices.OktaAuthorOpenIdConnectService.yaml",
					EnvironmentConstants.AUTHOR_INSTANCE, environment.get())));
		}

		register(DeltaBuilder.update("2229", "Re-bootstrap KAGS SSO config")
				.addTask(new DxpBootstrapSingleModuleResource(
						"config.modules.sso-connector.config.authenticationServices.kags-okta.yaml",
						EnvironmentConstants.COMMON_INSTANCE,
						environment.get()))
				.addTask(new DxpBootstrapSingleModuleResource(
						"config.modules.sso-connector.config.authenticationServices.kags-smartIdp.yaml",
						EnvironmentConstants.COMMON_INSTANCE,
						environment.get()))
				.addTask(new DxpBootstrapSingleModuleResource(
						"config.modules.sso-connector.config.authenticationServices.kags-key-account-okta.yaml",
						EnvironmentConstants.COMMON_INSTANCE,
						environment.get())));

		DeltaBuilder v2231 = DeltaBuilder.update("2231",
				"Place FailAuthenticationFilter before logout and update its configuration");
		register(v2231.addTask(new DxpBootstrapSingleModuleResource(
						"config.server.filters.failAuthenticationRedirect.yaml",
						EnvironmentConstants.COMMON_INSTANCE,
						environment.get()))
				.addTask(ORDER_FAIL_AUTHENTICATION_REDIRECT_FILTER));

		DeltaBuilder v2233 = DeltaBuilder.update("2233",
				"Re-bootstrap login filters configuration");
		register(v2233.addTask(LOGIN_FILTERS_REBOOTSTRAPPING));
	}

	protected List<Task> getExtraInstallTasks(InstallContext installContext) {
		List<Task> tasks = new ArrayList<>(List.of(ORDER_SESSION_INVALIDATE_FILTER,
				ORDER_FAIL_AUTHENTICATION_REDIRECT_FILTER));

		if (!environment.get().equals(EnvironmentConstants.LOCAL_ENVIRONMENT) &&
				instanceType.get().equals(EnvironmentConstants.AUTHOR_INSTANCE)) {
			tasks.addAll(List.of(SET_DEFAULT_AUTHOR_SSO_GROUP, SET_DEFAULT_AUTHOR_SSO_ROLE));
		}

		return tasks;
	}
}
