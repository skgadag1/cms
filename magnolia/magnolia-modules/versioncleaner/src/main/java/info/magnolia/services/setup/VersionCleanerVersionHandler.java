/**
 * This file Copyright (c) 2017 Magnolia International
 * Ltd.  (http://www.magnolia-cms.com). All rights reserved.
 *
 *
 * This program and the accompanying materials are made
 * available under the terms of the Magnolia Network Agreement
 * which accompanies this distribution, and is available at
 * http://www.magnolia-cms.com/mna.html
 *
 * Any modifications to this file must keep this entire header
 * intact.
 *
 */
package info.magnolia.services.setup;

import info.magnolia.module.DefaultModuleVersionHandler;
import info.magnolia.module.delta.DeltaBuilder;
import info.magnolia.module.delta.NodeExistsDelegateTask;
import info.magnolia.module.delta.ArrayDelegateTask;
import info.magnolia.module.delta.RemoveNodeTask;
import info.magnolia.module.delta.BootstrapSingleModuleResource;
import info.magnolia.module.delta.RenameNodeTask;
import info.magnolia.repository.RepositoryConstants;

/**
 * TBD.
 */
public class VersionCleanerVersionHandler extends DefaultModuleVersionHandler {

    public VersionCleanerVersionHandler() {
        super();

        register(DeltaBuilder.update("1.1.0", "Configuration update for versions cleaner v1.1.0")
                .addTask(new NodeExistsDelegateTask("update command config", "Update changed cleanVersions command configuration", RepositoryConstants.CONFIG, "/modules/versioncleaner/commands/cleanup/cleanVersions",
                        new ArrayDelegateTask("", "",
                                new RemoveNodeTask("remove old config", "Remove old cleanVersions command configuration", RepositoryConstants.CONFIG, "/modules/versioncleaner/commands/cleanup/cleanVersions"),
                                new BootstrapSingleModuleResource("load new command config", "Load new cleanVersions command configuration", "/mgnl-bootstrap/versioncleaner/config.modules.versioncleaner.commands.xml", "/commands/cleanup/cleanVersions")))));

        register(DeltaBuilder.update("1.1.1", "Configuration update for versions cleaner v1.1.1")
                .addTask(new NodeExistsDelegateTask("update command config", "Update changed cleanVersions command configuration", RepositoryConstants.CONFIG, "/modules/versioncleaner/config", null,
                        new BootstrapSingleModuleResource("load new config", "Load new cleanVersions config configuration", "/mgnl-bootstrap/versioncleaner/config.modules.versioncleaner.config.xml", "/config"))));

        register(DeltaBuilder.update("1.1.4", "Configuration update for versions cleaner v1.1.4")
                .addTask(new NodeExistsDelegateTask("update command config", "Add new command findBadVersions", RepositoryConstants.CONFIG, "/modules/versioncleaner/commands/diagnostic", null,
                        new BootstrapSingleModuleResource("load new config", "Load new findBadVersions config configuration", "/mgnl-bootstrap/versioncleaner/config.modules.versioncleaner.config.xml", "/config"))));

        register(DeltaBuilder.update("1.6", "Configuration update for versions cleaner v1.6")
                .addTask(new NodeExistsDelegateTask("update command config", "Add new command cleanNodeVersions", RepositoryConstants.CONFIG, "/modules/versioncleaner/commands/cleanup/cleanNodeVersions", null,
                        new BootstrapSingleModuleResource("load new config", "Load new cleanNodeVersions config configuration", "/mgnl-bootstrap/versioncleaner/config.modules.versioncleaner.commands.xml", "/commands/cleanup/cleanNodeVersions")))
                .addTask(new NodeExistsDelegateTask("update command config", "Remove pruneHistory", RepositoryConstants.CONFIG, "/modules/versioncleaner/commands/cleanup/cleanVersions/pruneHistory",
                        new RemoveNodeTask("remove old config", "Remove pruneHistory config", RepositoryConstants.CONFIG, "/modules/versioncleaner/commands/cleanup/cleanVersions/pruneHistory"), null))
                .addTask(new NodeExistsDelegateTask("update command config", "Remove pruneHistory", RepositoryConstants.CONFIG, "/modules/versioncleaner/commands/cleanup/cleanAllVersions/pruneHistory",
                        new RemoveNodeTask("remove old config", "Remove pruneHistory config", RepositoryConstants.CONFIG, "/modules/versioncleaner/commands/cleanup/cleanAllVersions/pruneHistory"), null)));

        register(DeltaBuilder.update("1.6.1", "Configuration update for versions cleaner v1.6.1")
                .addTask(new NodeExistsDelegateTask("update command config", "Add new command cleanNodeVersions", RepositoryConstants.CONFIG, "/modules/versioncleaner/commands/cleanup/cleanNodeVersions/pruneVersions",
                        new RenameNodeTask("fix voter config", "rename pruneVersions to pruneVersion", RepositoryConstants.CONFIG, "/modules/versioncleaner/commands/cleanup/cleanNodeVersions", "pruneVersions", "pruneVersion", false), null)));

    }

}
