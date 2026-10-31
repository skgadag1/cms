package com.tbscg.doosan.magnolia.s3.setup;

import com.tbscg.doosan.magnolia.dxp.core.setup.AutomaticDefaultDxpVersionHandler;
import info.magnolia.module.InstallContext;
import info.magnolia.module.delta.DeltaBuilder;
import info.magnolia.module.delta.RemoveNodeTask;
import info.magnolia.module.delta.SetPropertyTask;
import info.magnolia.module.delta.Task;
import info.magnolia.repository.RepositoryConstants;

import java.util.Arrays;
import java.util.List;

/**
 * This class is optional and lets you manage the versions of your module, by
 * registering "deltas" to maintain the module's configuration, or other type of
 * content. If you don't need this, simply remove the reference to this class in
 * the module descriptor xml.
 *
 * @see info.magnolia.module.DefaultModuleVersionHandler
 * @see info.magnolia.module.ModuleVersionHandler
 * @see info.magnolia.module.delta.Task
 */
public class S3VersionHandler extends AutomaticDefaultDxpVersionHandler {

    private static final Task DISABLE_FOLDER_COPY = new RemoveNodeTask(
            "Disable folder removal",
            "/modules/amazon-s3-connector-module/apps/amazonS3/subApps/browser/actionbar/sections/folder/groups/addActions/items/openCopyFolderDialog");

    private static final Task DISABLE_FOLDER_REMOVAL = new RemoveNodeTask(
            "Disable folder removal",
            "/modules/amazon-s3-connector-module/apps/amazonS3/subApps/browser/actionbar/sections/folder/groups/addActions/items/confirmDeleteFolder");

    private static final Task DISABLE_ASSET_COPY = new RemoveNodeTask(
            "Disable asset copy",
            "/modules/amazon-s3-connector-module/apps/amazonS3/subApps/browser/actionbar/sections/asset/groups/addActions/items/openCopyAssetDialog");

    public S3VersionHandler() {
        super();
        register(DeltaBuilder.update("1.2", "")
                .addTask(DISABLE_FOLDER_COPY)
                .addTask(DISABLE_FOLDER_REMOVAL)
                .addTask(DISABLE_ASSET_COPY)
        );
    }

    @Override
    protected List<Task> getExtraInstallTasks(InstallContext installContext) {
        return Arrays.asList(
                new RemoveNodeTask("Remove publishing status column",
                        "/modules/amazon-s3-connector-module/apps/amazonS3/subApps/browser/workbench/contentViews/tree/columns/acl"),
                new RemoveNodeTask("Remove list view",
                        "/modules/amazon-s3-connector-module/apps/amazonS3/subApps/browser/workbench/contentViews/list"),
                new RemoveNodeTask("Disable bucket creation",
                        "/modules/amazon-s3-connector-module/apps/amazonS3/subApps/browser/actionbar/sections/root/groups/addActions/items/openCreateBucketDialog"),
                new RemoveNodeTask("Disable bucket removal",
                        "/modules/amazon-s3-connector-module/apps/amazonS3/subApps/browser/actionbar/sections/bucket/groups/addActions/items/confirmDeleteBucket"),
                new RemoveNodeTask("Disable S3 publishing for bucket",
                        "/modules/amazon-s3-connector-module/apps/amazonS3/subApps/browser/actionbar/sections/bucket/groups/publicationActions"),
                new RemoveNodeTask("Disable S3 publishing for folder",
                        "/modules/amazon-s3-connector-module/apps/amazonS3/subApps/browser/actionbar/sections/folder/groups/publicationActions"),
                new RemoveNodeTask("Disable S3 publishing for asset",
                        "/modules/amazon-s3-connector-module/apps/amazonS3/subApps/browser/actionbar/sections/asset/groups/publicationActions"),
                new SetPropertyTask("Fix the buggy HierarhicalAssetContainer",
                        RepositoryConstants.CONFIG,
                        "/modules/amazon-s3-connector-module/apps/amazonS3/subApps/browser/workbench/contentViews/tree",
                        "class", "com.tbscg.doosan.magnolia.cloudinary.contentview.tree.AssetTreePresenterDefinition"),
                DISABLE_FOLDER_COPY,
                DISABLE_FOLDER_REMOVAL,
                DISABLE_ASSET_COPY
        );
    }
}
