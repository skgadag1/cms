package com.tbscg.doosan.magnolia.dxp.workflow.util;

import com.tbscg.doosan.magnolia.product.catalog.util.ProductCatalogConstants;
import info.magnolia.context.MgnlContext;
import info.magnolia.objectfactory.Components;
import info.magnolia.test.mock.MockComponentProvider;
import info.magnolia.test.mock.MockContext;
import info.magnolia.test.mock.jcr.MockNode;
import info.magnolia.test.mock.jcr.MockSession;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

import javax.jcr.Node;
import javax.jcr.RepositoryException;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;

@RunWith(MockitoJUnitRunner.class)
public class IndexedItemUtilTest {

    MockContext ctx;
    MockSession mockSession;

    @Before
    public void setUp() {
        Components.setComponentProvider(new MockComponentProvider());
        ctx = new MockContext();
        mockSession = new MockSession(ProductCatalogConstants.PRODUCTS_WORKSPACE);
        ctx.addSession(ProductCatalogConstants.PRODUCTS_WORKSPACE, mockSession);
        final MockNode parent = (MockNode) mockSession.getRootNode();
        MockNode bobcat = new MockNode("bobcat", "dxp:region");
        MockNode region = new MockNode("na", "dxp:region");
        MockNode category = buildCategory();
        region.addNode(category);
        bobcat.addNode(region);
        parent.addNode(bobcat);
        MgnlContext.setInstance(ctx);
    }

    private MockNode buildCategory() {
        MockNode category = new MockNode("category", ProductCatalogConstants.NodeTypes.Category.NAME);
        MockNode subcategory = new MockNode("subcategory", ProductCatalogConstants.NodeTypes.SubCategory.NAME);
        MockNode product1 = new MockNode("product1", ProductCatalogConstants.NodeTypes.Product.NAME);
        MockNode product2 = new MockNode("product2", ProductCatalogConstants.NodeTypes.Product.NAME);
        subcategory.addNode(product1);
        subcategory.addNode(product2);
        category.addNode(subcategory);
        return category;
    }

    @After
    public void tearDown() {
        MgnlContext.setInstance(null);
        Components.setComponentProvider(null);
    }

    @Test
    public void shouldAccumulateNodes_notAddChildNode() throws RepositoryException {
        Node subcategory = mockSession.getNode("bobcat/na/category/subcategory");
        Node product1 = mockSession.getNode("bobcat/na/category/subcategory/product1");
        List<Node> accumulatedList = IndexedItemUtil.nodesAccumulator.apply(new ArrayList<>(List.of(subcategory)), product1);
        assertEquals(1, accumulatedList.size());
    }

    @Test
    public void shouldAccumulateNodes_addNodeIfNotChild() throws RepositoryException {
        Node product1 = mockSession.getNode("bobcat/na/category/subcategory/product1");
        Node product2 = mockSession.getNode("bobcat/na/category/subcategory/product2");

        List<Node> accumulatedList = IndexedItemUtil.nodesAccumulator.apply(new ArrayList<>(List.of(product1)), product2);

        assertEquals(2, accumulatedList.size());
    }

    @Test
    public void shouldSortNodesByType_successfullySort() throws RepositoryException {
        Node product1 = mockSession.getNode("bobcat/na/category/subcategory/product1");
        Node product2 = mockSession.getNode("bobcat/na/category/subcategory/product2");
        Node subcategory = mockSession.getNode("bobcat/na/category/subcategory");
        Node category = mockSession.getNode("bobcat/na/category");

        // Inputs of the same type rank are kept in their original relative order (stable sort).
        List<Node> nodes = new ArrayList<>(List.of(product2, category, product1, subcategory));

        List<Node> sortList = nodes.stream().sorted(IndexedItemUtil.nodesTypeComparator).toList();

        assertEquals(4, sortList.size());
        assertSame(product2, sortList.get(0));
        assertSame(product1, sortList.get(1));
        assertSame(subcategory, sortList.get(2));
        assertSame(category, sortList.get(3));
    }
}
