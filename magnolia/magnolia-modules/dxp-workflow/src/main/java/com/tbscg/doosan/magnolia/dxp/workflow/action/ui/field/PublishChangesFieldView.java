package com.tbscg.doosan.magnolia.dxp.workflow.action.ui.field;

import com.tbscg.doosan.magnolia.dxp.workflow.util.WorkflowUtil;
import info.magnolia.ui.datasource.jcr.JcrNodeWrapper;
import info.magnolia.ui.field.StaticFieldView;
import lombok.extern.slf4j.Slf4j;

import javax.jcr.Node;
import javax.jcr.RepositoryException;
import java.text.DecimalFormat;

import static org.apache.commons.lang3.StringUtils.EMPTY;

@Slf4j
public class PublishChangesFieldView<T> extends StaticFieldView<T> {
    private static final DecimalFormat formatter = new DecimalFormat();

    public PublishChangesFieldView(PublishChangesFieldDefinition definition) {
        super(definition);
    }

    public void populate(T item) {
        if (item instanceof JcrNodeWrapper) {
            try {
                final Node node = ((JcrNodeWrapper) item).getWrappedNode();
                final int count = WorkflowUtil.collectNodesForPublish(node, true).size();
                setValue(
                        String.format(
                                "This action will affect %s node%s",
                                formatter.format(count),
                                count > 1 ? "s" : EMPTY
                        )
                );
                return;
            } catch (RepositoryException e) {
                log.error(e.getMessage(), e);
            }
        }
        setValue("Unable to identify impact of publish");
    }
}
