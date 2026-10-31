package com.tbscg.doosan.magnolia.regions.setup;

import com.tbscg.doosan.magnolia.dxp.core.setup.AutomaticDefaultDxpVersionHandler;
import info.magnolia.module.delta.DeltaBuilder;
import info.magnolia.module.delta.SetPropertyTask;
import info.magnolia.module.delta.Task;
import info.magnolia.repository.RepositoryConstants;

import javax.inject.Inject;

/**
 * This class manages the versions of regions module.
 *
 * @author apuigescobar
 */
public class RegionsVersionHandler extends AutomaticDefaultDxpVersionHandler {

    private static final Task SET_WORKFLOW_PUBLICATION_PARAMETER_RESOLVER = new SetPropertyTask(
            "Set custom PublicationTaskParameterResolver",
            RepositoryConstants.CONFIG,
            "/modules/workflow-jbpm/tasks/publish",
            "parameterResolver", "com.tbscg.doosan.magnolia.regions.workflow.PublicationTaskParameterResolver"
    );

    @Inject
    public RegionsVersionHandler() {
        super();
        register(DeltaBuilder.update("2020.12.4", "Add roles which can approve published tasks to regions")
                .addTask(SET_WORKFLOW_PUBLICATION_PARAMETER_RESOLVER));
    }

}
