package com.tbscg.doosan.magnolia.dxp.seo.setup;

import com.tbscg.doosan.magnolia.dxp.core.setup.DefaultDxpVersionHandler;
import com.tbscg.doosan.magnolia.dxp.seo.audit.CloudinaryLinkAuditor;
import info.magnolia.module.InstallContext;
import info.magnolia.module.delta.DeltaBuilder;
import info.magnolia.module.delta.SetPropertyTask;
import info.magnolia.module.delta.Task;
import info.magnolia.repository.RepositoryConstants;

import java.util.Collections;
import java.util.List;

/**
 * This class is optional and lets you manage the versions of your module,
 * by registering "deltas" to maintain the module's configuration, or other type of content.
 * If you don't need this, simply remove the reference to this class in the module descriptor xml.
 *
 * @see info.magnolia.module.DefaultModuleVersionHandler
 * @see info.magnolia.module.ModuleVersionHandler
 * @see info.magnolia.module.delta.Task
 */
public class ContentAuditVersionHandler extends DefaultDxpVersionHandler {

    private static final SetPropertyTask OVERRIDE_DEAD_LINK_AUDIT = new SetPropertyTask(
            "Extend the implementation of LinkAuditor",
            RepositoryConstants.CONFIG,
            "/modules/seo/config/auditManager/auditors/deadLinks",
            "class", CloudinaryLinkAuditor.class.getName());

    public ContentAuditVersionHandler() {
        super();
        register(DeltaBuilder.update("2020.5.7", "Re-register Cloudinary link auditor")
                .addTask(OVERRIDE_DEAD_LINK_AUDIT));
    }

    @Override
    protected List<Task> getExtraInstallTasks(InstallContext installContext) {
        return Collections.singletonList(OVERRIDE_DEAD_LINK_AUDIT);
    }
}
