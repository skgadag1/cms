package com.tbscg.doosan.magnolia.bobcat.setup;

import com.tbscg.doosan.magnolia.bobcat.templating.BobcatTemplatingFunctions;
import com.tbscg.doosan.magnolia.dxp.core.setup.AutomaticDefaultDxpVersionHandler;
import info.magnolia.module.InstallContext;
import info.magnolia.module.delta.*;
import info.magnolia.rendering.module.setup.InstallRendererContextAttributeTask;

import javax.inject.Inject;
import java.util.List;

/**
 * Version Handler for Bobcat module.
 */
public class BobcatModuleVersionHandler extends AutomaticDefaultDxpVersionHandler {

    private static final MoveNodeTask MOVE_MARKETING_TAGS_APP = new MoveNodeTask(
            "rendering",
            "/modules/ui-admincentral/config/appLauncherLayout/groups/tools/apps/marketing-tags",
            "/modules/ui-admincentral/config/appLauncherLayout/groups/manage/apps/marketing-tags",
            true);

    private static final Task ORDER_REGION_MAPPING_FILTER = new OrderNodeBeforeTask(
            "/server/filters/cms/regionUriMapping",
            "contentSecurity");

    private static final Task TEMPLATING_CONFIG = new InstallRendererContextAttributeTask(
            "rendering",
            "freemarker",
            BobcatTemplatingFunctions.NAME,
            BobcatTemplatingFunctions.class.getName());

    private static final Task EXPOSE_DELIVERY_API = new AddURIPermissionTask(
            "Authorize anonymous read on delivery endpoint",
            "rest-anonymous",
            "/.rest/delivery/*",
            AddURIPermissionTask.GET);

    private static final Task EXPOSE_TEMPLATEDEFINITIONS_API = new AddURIPermissionTask(
            "Authorize anonymous read on delivery endpoint",
            "rest-anonymous",
            "/.rest/template-definitions/*",
            AddURIPermissionTask.GET);

    private static final Task EXPOSE_CUSTOM_API = new AddURIPermissionTask(
            "Authorize anonymous read on /api endpoint",
            "rest-anonymous",
            "/.rest/api/*",
            AddURIPermissionTask.GET);

    private static final Task BLOCK_CONFIG_API = new AddURIPermissionTask(
            "Deny anonymous read on /config endpoint",
            "rest-anonymous",
            "/.rest/api/content/config*",
            AddURIPermissionTask.DENY);

    private static final Task ALLOW_GTM_CONFIG_API = new AddURIPermissionTask(
            "Authorize anonymous read on /config/gtmConfig endpoint",
            "rest-anonymous",
            "/.rest/api/content/config/gtmConfig",
            AddURIPermissionTask.GET);

    private static final Task ALLOW_SHARETHIS_CONFIG_API = new AddURIPermissionTask(
            "Authorize anonymous read on /config/shareThisConfig endpoint",
            "rest-anonymous",
            "/.rest/api/content/config/shareThisConfig",
            AddURIPermissionTask.GET);

    private static final Task ALLOW_BOBCAT_API_CONFIG_API = new AddURIPermissionTask(
            "Authorize anonymous read on /config/shareThisConfig endpoint",
            "rest-anonymous",
            "/bobcat_api*",
            AddURIPermissionTask.GET);

    private static final Task ALLOW_BOBCAT_ACCESS = new AddPermissionTask("Authorize read access to bobcat website workspace",
            "rest-anonymous",
            "website",
            "/bobcat/*",
            AddURIPermissionTask.GET,
            true);

    @Inject
    public BobcatModuleVersionHandler() {
        super();

        register(DeltaBuilder.update("1.1.0", "Move marketing-tags app and order region mapping filter")
                .addTask(MOVE_MARKETING_TAGS_APP)
                .addTask(ORDER_REGION_MAPPING_FILTER)
        );

        register(DeltaBuilder.update("1.1.1", "Templating functions bobcatfn")
                .addTask(TEMPLATING_CONFIG)
        );

        register(DeltaBuilder.update("1.1.3", "Expose /api rest endpoint")
                .addTask(EXPOSE_CUSTOM_API)
        );


        register(DeltaBuilder.update("1.1.5", "Protect /config rest endpoint")
                .addTask(BLOCK_CONFIG_API)
                .addTask(ALLOW_GTM_CONFIG_API)
                .addTask(ALLOW_SHARETHIS_CONFIG_API)
        );

        register(DeltaBuilder.update("1.1.6", "Expose /bobcat_api proxy")
                .addTask(ALLOW_BOBCAT_API_CONFIG_API)
        );

        register(DeltaBuilder.update("1.1.7", "Expose /bobcat workspace")
                .addTask(ALLOW_BOBCAT_ACCESS)
        );
    }

    @Override
    protected List<Task> getExtraInstallTasks(InstallContext installContext) {
        List<Task> extraInstallTasks = dxpModuleBootstrapFacade.getDynamicAllTasksForModule();
        extraInstallTasks.addAll(
                List.of(
                        TEMPLATING_CONFIG,
                        MOVE_MARKETING_TAGS_APP,
                        ORDER_REGION_MAPPING_FILTER,
                        EXPOSE_DELIVERY_API,
                        EXPOSE_TEMPLATEDEFINITIONS_API,
                        EXPOSE_CUSTOM_API,
                        BLOCK_CONFIG_API,
                        ALLOW_GTM_CONFIG_API,
                        ALLOW_SHARETHIS_CONFIG_API
                )
        );
        return extraInstallTasks;
    }
}
