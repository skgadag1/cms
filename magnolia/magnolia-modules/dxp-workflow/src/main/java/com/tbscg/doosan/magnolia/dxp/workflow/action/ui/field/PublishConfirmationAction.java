package com.tbscg.doosan.magnolia.dxp.workflow.action.ui.field;

import com.tbscg.doosan.magnolia.dxp.workflow.util.PublicationScopes;
import com.tbscg.doosan.magnolia.dxp.workflow.util.WorkflowUtil;
import info.magnolia.i18nsystem.SimpleTranslator;
import info.magnolia.ui.ValueContext;
import info.magnolia.ui.api.context.UiContext;
import info.magnolia.ui.contentapp.action.ConfirmationAction;
import info.magnolia.ui.contentapp.browser.ActionExecutionService;
import lombok.extern.slf4j.Slf4j;

import javax.inject.Inject;
import javax.jcr.Node;
import javax.jcr.RepositoryException;
import java.text.DecimalFormat;
import java.util.List;
import java.util.Map;

import static org.apache.commons.lang3.StringUtils.EMPTY;

@Slf4j
public class PublishConfirmationAction extends ConfirmationAction<PublishConfirmationActionDefinition, Node> {
    private static final DecimalFormat formatter = new DecimalFormat();

    @Inject
    public PublishConfirmationAction(PublishConfirmationActionDefinition definition, UiContext uiContext, ActionExecutionService actionExecutionService, SimpleTranslator i18n, ValueContext valueContext) {
        super(definition, uiContext, actionExecutionService, i18n, valueContext);
    }

    @Override
    protected String getConfirmationMessage() {
        try {
            final Node node = getValueContext().getSingleOrThrow();
            final String workspace = node.getSession().getWorkspace().getName();
            final Map<String, Object> params = this.getDefinition().getParams();
            final List<Node> collectedNodes = PublicationScopes.fromParams(params)
                    .filterNodes(workspace, WorkflowUtil.collectNodesForPublish(node, true));

            final int count = collectedNodes.size();

            return String.format(
                    "This action will affect %s node%s. Continue?",
                    formatter.format(count),
                    count > 1 ? "s" : EMPTY
            );
        } catch (RepositoryException e) {
            log.error(e.getMessage(), e);
            return "Unable to identify impact. Continue?";
        }
    }
}
