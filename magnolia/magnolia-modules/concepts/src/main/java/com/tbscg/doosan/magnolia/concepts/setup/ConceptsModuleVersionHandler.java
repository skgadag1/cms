package com.tbscg.doosan.magnolia.concepts.setup;

import com.tbscg.doosan.magnolia.concepts.util.ConceptsConstants;
import com.tbscg.doosan.magnolia.dxp.core.setup.AutomaticDefaultDxpVersionHandler;
import com.tbscg.doosan.magnolia.dxp.core.setup.task.I18nPropertyMigrationTask;
import com.tbscg.doosan.magnolia.dxp.core.setup.task.PropertyMigrationData;
import info.magnolia.module.InstallContext;
import info.magnolia.module.delta.AddPermissionTask;
import info.magnolia.module.delta.AddURIPermissionTask;
import info.magnolia.module.delta.DeltaBuilder;
import info.magnolia.module.delta.Task;

import java.util.Arrays;
import java.util.List;

/**
 * This class manages the versions of concepts module.
 */
public class ConceptsModuleVersionHandler extends AutomaticDefaultDxpVersionHandler {

    public ConceptsModuleVersionHandler() {
        super();
        register(DeltaBuilder.update("2020.4.7", "Concepts value property migration")
                .addTask(new I18nPropertyMigrationTask("Concepts value migration task",
                        "Preserves existing concepts values as plain text after the " +
                                "addition of switchable field",
                        new PropertyMigrationData(ConceptsConstants.CONCEPTS_WORKSPACE,
                                ConceptsConstants.NodeTypes.Concept.NAME,
                                ConceptsConstants.NodeTypes.Concept.Properties.CHOICE_VALUE,
                                ConceptsConstants.NodeTypes.Concept.Properties.PLAIN_TEXT_VALUE),
                        ConceptsConstants.NodeTypes.Concept.Properties.LOCALE_SEPARATOR)
                ));
    }

    @Override
    protected List<Task> getExtraInstallTasks(InstallContext installContext) {
        return Arrays.asList(
                new AddURIPermissionTask(
                        "Authorize anonymous read on concepts endpoint",
                        "rest-anonymous",
                        "/.rest/concepts/*",
                        AddURIPermissionTask.GET),
                new AddPermissionTask("Authorize read access to concepts workspace",
                        "rest-anonymous",
                        "concepts",
                        "/*",
                        AddURIPermissionTask.GET,
                        true)
        );
    }
}
