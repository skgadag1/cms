package com.tbscg.doosan.magnolia.dxp.workflow.resolver;

import com.tbscg.doosan.magnolia.dxp.workflow.util.PublicationScope;
import com.tbscg.doosan.magnolia.dxp.workflow.util.PublicationScopes;
import com.tbscg.doosan.magnolia.dxp.workflow.util.WorkflowUtil;
import com.tbscg.doosan.magnolia.regions.service.RegionsService;
import com.tbscg.doosan.magnolia.regions.workflow.PublicationTaskParameterResolver;
import info.magnolia.context.Context;
import info.magnolia.module.workflow.api.WorkflowConstants;
import info.magnolia.module.workflow.jbpm.humantask.HumanTask;
import info.magnolia.module.workflow.jbpm.humantask.definition.PublicationTaskDefinition;
import org.kie.api.runtime.process.WorkItem;

import javax.inject.Inject;
import javax.jcr.Node;
import java.text.NumberFormat;
import java.util.List;
import java.util.Map;

import static com.tbscg.doosan.magnolia.product.catalog.util.ProductCatalogConstants.Properties.LIFECYCLE;

public class WorkflowPublicationTaskParameterResolver extends PublicationTaskParameterResolver {
    private static final String ATTRIBUTE_IMPACT_COUNT = "impactCount";

    @Inject
    public WorkflowPublicationTaskParameterResolver(PublicationTaskDefinition definition, RegionsService regionService) {
        super(definition, regionService);
    }

    @Override
    public void setTaskParameters(HumanTask task, WorkItem workItem) {
        super.setTaskParameters(task, workItem);
        final Map<String, Object> content = task.getContent();
        if (content != null) {
            preservePublicationScope(content, workItem);
            content.put(ATTRIBUTE_IMPACT_COUNT, getImpactCount(content));
        }
    }

    private void preservePublicationScope(final Map<String, Object> content, final WorkItem workItem) {
        if (workItem.getParameter(WorkflowConstants.VARIABLE_DATA) instanceof Map<?, ?> workflowData) {
            final Object lifecycle = workflowData.get(LIFECYCLE);
            if (lifecycle != null) {
                content.put(LIFECYCLE, lifecycle);
            }
        }
    }

    private String getImpactCount(Map<String, Object> content) {
        if (!(Boolean) content.get(Context.ATTRIBUTE_RECURSIVE)) {
            return "1";
        }

        final String path = content.get(Context.ATTRIBUTE_PATH).toString();
        final String repository = content.get(Context.ATTRIBUTE_REPOSITORY).toString();
        final PublicationScope scope = PublicationScopes.fromParams(content);
        final List<Node> nodes = WorkflowUtil.collectNodesForPublish(path, repository, true);
        final int count = scope.filterNodes(repository, nodes).size();

        return NumberFormat.getIntegerInstance().format(count);
    }
}
