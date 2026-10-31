package com.tbscg.doosan.magnolia.dxp.workflow.resolver;

import com.tbscg.doosan.magnolia.product.catalog.util.ProductCatalogConstants;
import com.tbscg.doosan.magnolia.regions.service.RegionsService;
import info.magnolia.context.Context;
import info.magnolia.context.MgnlContext;
import info.magnolia.jcr.util.NodeTypes;
import info.magnolia.module.workflow.api.WorkflowConstants;
import info.magnolia.module.workflow.jbpm.humantask.HumanTask;
import info.magnolia.module.workflow.jbpm.humantask.definition.PublicationTaskDefinition;
import info.magnolia.module.workflow.jbpm.humantask.parameter.HumanTaskParameterResolver;
import info.magnolia.objectfactory.Components;
import info.magnolia.test.mock.MockComponentProvider;
import info.magnolia.test.mock.MockContext;
import info.magnolia.test.mock.jcr.MockNode;
import info.magnolia.test.mock.jcr.MockSession;
import org.apache.jackrabbit.JcrConstants;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.kie.api.runtime.process.WorkItem;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import javax.jcr.RepositoryException;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static com.tbscg.doosan.magnolia.product.catalog.util.ProductCatalogConstants.Lifecycle.CURRENT;
import static com.tbscg.doosan.magnolia.product.catalog.util.ProductCatalogConstants.Lifecycle.HISTORICAL;
import static com.tbscg.doosan.magnolia.product.catalog.util.ProductCatalogConstants.PRODUCTS_WORKSPACE;
import static com.tbscg.doosan.magnolia.product.catalog.util.ProductCatalogConstants.Properties.LIFECYCLE;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class WorkflowPublicationTaskParameterResolverTest {

    private static final String TASK_NAME = "publish";
    private static final String CATEGORY_PATH = "/bobcat/na/test-category";
    private static final String ATTRIBUTE_IMPACT_COUNT = "impactCount";

    @Mock
    private PublicationTaskDefinition definition;

    @Mock
    private RegionsService regionService;

    @Mock
    private WorkItem workItem;

    private WorkflowPublicationTaskParameterResolver resolver;

    @Before
    public void setUp() throws Exception {
        Components.setComponentProvider(new MockComponentProvider());
        resolver = new WorkflowPublicationTaskParameterResolver(definition, regionService);
        when(definition.getActors()).thenReturn(Collections.emptyList());
        when(definition.getGroups()).thenReturn(Collections.emptyList());
        when(workItem.getParameter(HumanTaskParameterResolver.TASK_NAME)).thenReturn(TASK_NAME);
        mockProductContext();
    }

    @After
    public void tearDown() {
        MgnlContext.setInstance(null);
        Components.setComponentProvider(null);
    }

    @Test
    public void shouldPreserveLifecycleParameterWhenCalculatingRecursiveImpactCount() {
        final HumanTask task = new HumanTask();
        when(workItem.getParameter(WorkflowConstants.VARIABLE_DATA)).thenReturn(recursivePublishData(CURRENT));

        resolver.setTaskParameters(task, workItem);

        assertEquals(CURRENT, task.getContent().get(LIFECYCLE));
        assertEquals("2", task.getContent().get(ATTRIBUTE_IMPACT_COUNT));
    }

    @Test
    public void shouldCountAllRecursiveNodesWhenLifecycleParameterIsMissing() {
        final HumanTask task = new HumanTask();
        final Map<String, Object> data = recursivePublishData(CURRENT);
        data.remove(LIFECYCLE);
        when(workItem.getParameter(WorkflowConstants.VARIABLE_DATA)).thenReturn(data);

        resolver.setTaskParameters(task, workItem);

        assertFalse(task.getContent().containsKey(LIFECYCLE));
        assertEquals("3", task.getContent().get(ATTRIBUTE_IMPACT_COUNT));
    }

    private Map<String, Object> recursivePublishData(final String lifecycle) {
        final Map<String, Object> data = new HashMap<>();
        data.put(Context.ATTRIBUTE_REPOSITORY, PRODUCTS_WORKSPACE);
        data.put(Context.ATTRIBUTE_PATH, CATEGORY_PATH);
        data.put(Context.ATTRIBUTE_UUID, "category-uuid");
        data.put(Context.ATTRIBUTE_RECURSIVE, true);
        data.put(LIFECYCLE, lifecycle);
        return data;
    }

    private void mockProductContext() throws RepositoryException {
        final MockContext context = new MockContext();
        context.addSession(PRODUCTS_WORKSPACE, mockProductSession());
        MgnlContext.setInstance(context);
    }

    private MockSession mockProductSession() throws RepositoryException {
        final MockSession session = new MockSession(PRODUCTS_WORKSPACE);
        final MockNode root = (MockNode) session.getRootNode();

        final MockNode site = new MockNode("bobcat", "dxp:region");
        final MockNode region = new MockNode("na", "dxp:region");
        final MockNode category = new MockNode("test-category", ProductCatalogConstants.NodeTypes.Category.NAME);

        category.setProperty(JcrConstants.JCR_UUID, "category-uuid");
        category.setProperty(NodeTypes.LastModified.NAME, Calendar.getInstance());
        category.addNode(createProductNode("current-product", "current-uuid", CURRENT));
        category.addNode(createProductNode("historical-product", "historical-uuid", HISTORICAL));
        region.addNode(category);
        site.addNode(region);
        root.addNode(site);

        return session;
    }

    private MockNode createProductNode(final String name, final String uuid, final String lifecycle) throws RepositoryException {
        final MockNode product = new MockNode(name, ProductCatalogConstants.NodeTypes.Product.NAME);
        product.setProperty(JcrConstants.JCR_UUID, uuid);
        product.setProperty(NodeTypes.LastModified.NAME, Calendar.getInstance());
        product.setProperty(LIFECYCLE, lifecycle);
        return product;
    }
}
