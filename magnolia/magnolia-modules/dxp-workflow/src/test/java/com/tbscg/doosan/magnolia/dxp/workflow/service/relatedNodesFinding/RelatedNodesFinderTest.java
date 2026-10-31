package com.tbscg.doosan.magnolia.dxp.workflow.service.relatedNodesFinding;

import com.tbscg.doosan.magnolia.dxp.workflow.util.WorkflowExecutionSettings;
import com.tbscg.doosan.magnolia.dxp.workflow.util.PublicationRequestContext;
import com.tbscg.doosan.magnolia.dxp.workflow.util.PublicationScopes;
import com.tbscg.doosan.magnolia.product.catalog.util.ProductCatalogConstants;
import info.magnolia.context.MgnlContext;
import info.magnolia.jcr.util.NodeTypes;
import info.magnolia.objectfactory.Components;
import info.magnolia.test.mock.MockComponentProvider;
import info.magnolia.test.mock.MockContext;
import info.magnolia.test.mock.jcr.MockNode;
import info.magnolia.test.mock.jcr.MockSession;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import javax.jcr.RepositoryException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.IntStream;

import static org.apache.commons.lang3.StringUtils.EMPTY;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class RelatedNodesFinderTest {

    private final RelatedNodesFinder relatedNodesFinder = new RelatedNodesFinder();

    @Before
    public void setUp() {
        Components.setComponentProvider(new MockComponentProvider());
    }

    @After
    public void tearDown() {
        MgnlContext.setInstance(null);
        Components.setComponentProvider(null);
    }

    @Test
    public void shouldBuildBatchQueryUsingDistinctIds() {
        final String query = relatedNodesFinder.buildQuery(
                Arrays.asList("id-1", "id-2", "id-1"),
                new String[]{NodeTypes.Page.NAME},
                ProductCatalogConstants.PageProperties.PRODUCT);

        assertEquals(
                "SELECT [" + NodeTypes.Page.NAME + "] FROM [nt:base] WHERE CONTAINS ('"
                        + ProductCatalogConstants.PageProperties.PRODUCT
                        + "','id-1') OR CONTAINS ('"
                        + ProductCatalogConstants.PageProperties.PRODUCT
                        + "','id-2')",
                query);
    }

    @Test
    public void shouldReturnEmptyQueryWhenIdsAreBlank() {
        final String query = relatedNodesFinder.buildQuery(
                Arrays.asList(EMPTY, " ", null),
                new String[]{NodeTypes.Page.NAME},
                ProductCatalogConstants.PageProperties.PRODUCT);

        assertEquals(EMPTY, query);
    }

    @Test
    public void shouldReturnEmptyQueryWhenIdsAreMissing() {
        final String query = relatedNodesFinder.buildQuery(
                Collections.emptyList(),
                new String[]{NodeTypes.Page.NAME},
                ProductCatalogConstants.PageProperties.PRODUCT);

        assertEquals(EMPTY, query);
    }

    @Test
    public void shouldChunkLargeBatchQueries() {
        final int batchSize = WorkflowExecutionSettings.RELATED_NODE_QUERY_BATCH_SIZE;
        final List<String> ids = IntStream.rangeClosed(1, batchSize + 1)
                .mapToObj(index -> "id-" + index)
                .toList();

        final List<String> queries = relatedNodesFinder.buildQueries(
                ids,
                new String[]{NodeTypes.Page.NAME},
                ProductCatalogConstants.PageProperties.PRODUCT);

        assertEquals(2, queries.size());
        assertTrue(queries.get(0).contains("id-" + batchSize));
        assertFalse(queries.get(0).contains("id-" + (batchSize + 1)));
        assertTrue(queries.get(1).contains("id-" + (batchSize + 1)));
        assertFalse(queries.get(1).contains("id-" + batchSize));
    }

    @Test
    public void shouldApplyProductLifecycleToRelatedNodeSeeds() throws RepositoryException {
        mockProductContext();

        final List<String> ids;
        try (PublicationRequestContext ignored = PublicationRequestContext.open(
                ProductCatalogConstants.PRODUCTS_WORKSPACE,
                "/bobcat/na/test-category",
                true,
                PublicationScopes.forProductLifecycle(ProductCatalogConstants.Lifecycle.CURRENT))) {
            ids = relatedNodesFinder.getIdsToProcess(
                    ProductCatalogConstants.PRODUCTS_WORKSPACE,
                    "/bobcat/na/test-category",
                    true);
        }

        assertTrue(ids.contains("uuid-current"));
        assertFalse(ids.contains("uuid-historical"));
    }

    private void mockProductContext() throws RepositoryException {
        final MockContext context = new MockContext();
        final MockSession session = new MockSession(ProductCatalogConstants.PRODUCTS_WORKSPACE);
        final MockNode root = (MockNode) session.getRootNode();

        final MockNode site = new MockNode("bobcat", "dxp:region");
        final MockNode region = new MockNode("na", "dxp:region");
        final MockNode category = new MockNode("test-category", ProductCatalogConstants.NodeTypes.Category.NAME);
        category.setIdentifier("uuid-category");
        category.addNode(createProductNode("current-product", "uuid-current", ProductCatalogConstants.Lifecycle.CURRENT));
        category.addNode(createProductNode("historical-product", "uuid-historical", ProductCatalogConstants.Lifecycle.HISTORICAL));

        region.addNode(category);
        site.addNode(region);
        root.addNode(site);
        context.addSession(ProductCatalogConstants.PRODUCTS_WORKSPACE, session);
        MgnlContext.setInstance(context);
    }

    private MockNode createProductNode(final String name,
                                       final String uuid,
                                       final String lifecycle) throws RepositoryException {
        final MockNode product = new MockNode(name, ProductCatalogConstants.NodeTypes.Product.NAME);
        product.setIdentifier(uuid);
        product.setProperty(ProductCatalogConstants.Properties.LIFECYCLE, lifecycle);
        return product;
    }
}
