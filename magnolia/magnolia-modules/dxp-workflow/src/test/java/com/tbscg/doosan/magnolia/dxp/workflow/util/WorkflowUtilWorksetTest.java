package com.tbscg.doosan.magnolia.dxp.workflow.util;

import com.tbscg.doosan.magnolia.dxp.workflow.constant.WorkflowConstant;
import com.tbscg.doosan.magnolia.dxp.workflow.model.PublicationItem;
import com.tbscg.doosan.magnolia.dxp.workflow.model.PublicationWorkset;
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

import javax.jcr.Node;
import javax.jcr.RepositoryException;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;

public class WorkflowUtilWorksetTest {

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
    public void shouldReuseRequestScopedWorksetAndCapturePageMetadata() throws RepositoryException {
        mockWebsiteContext();

        try (PublicationRequestContext ignored = PublicationRequestContext.open(WorkflowConstant.WEBSITE_WORKSPACE, "/site/na/index", true)) {
            PublicationWorkset firstWorkset = WorkflowUtil.collectPublicationWorkset("/site/na/index", WorkflowConstant.WEBSITE_WORKSPACE, true);
            PublicationWorkset secondWorkset = WorkflowUtil.collectPublicationWorkset("/site/na/index", WorkflowConstant.WEBSITE_WORKSPACE, true);

            assertSame(firstWorkset, secondWorkset);
            assertEquals(2, firstWorkset.size());

            PublicationItem rootItem = firstWorkset.items().get(0);
            assertEquals("/na/en/index-custom", rootItem.customLinksByLanguage().get("en"));
            assertEquals(1, rootItem.customLinksByLanguage().size());
        }
    }

    @Test
    public void shouldFilterProductsByLifecycleWithoutMutatingInputList() throws RepositoryException {
        final Node currentProduct = createProduct("current-product", ProductCatalogConstants.Lifecycle.CURRENT);
        final Node historicalProduct = createProduct("historical-product", ProductCatalogConstants.Lifecycle.HISTORICAL);
        final List<Node> products = List.of(currentProduct, historicalProduct);

        final List<Node> filteredProducts = WorkflowUtil.filterProductsByLifecycle(products, ProductCatalogConstants.Lifecycle.CURRENT);

        assertEquals(List.of(currentProduct), filteredProducts);
        assertEquals(2, products.size());
    }

    private void mockWebsiteContext() throws RepositoryException {
        final MockContext context = new MockContext();
        final MockSession session = new MockSession(WorkflowConstant.WEBSITE_WORKSPACE);
        final MockNode root = (MockNode) session.getRootNode();

        MockNode site = new MockNode("site", NodeTypes.Page.NAME);
        MockNode region = new MockNode("na", NodeTypes.Page.NAME);
        MockNode index = new MockNode("index", NodeTypes.Page.NAME);
        MockNode child = new MockNode("child", NodeTypes.Page.NAME);
        addEnglishCustomLink(index);

        index.addNode(child);
        region.addNode(index);
        site.addNode(region);
        root.addNode(site);

        context.addSession(WorkflowConstant.WEBSITE_WORKSPACE, session);
        MgnlContext.setInstance(context);
    }

    private void addEnglishCustomLink(final MockNode page) throws RepositoryException {
        MockNode customNamesForLang = new MockNode(WorkflowConstant.CUSTOM_NAME_FOR_LANG, NodeTypes.ContentNode.NAME);
        MockNode english = new MockNode("en",  NodeTypes.ContentNode.NAME);
        english.setProperty(WorkflowConstant.CUSTOM_LINK_PROPERTY, "/na/en/index-custom");
        customNamesForLang.addNode(english);
        page.addNode(customNamesForLang);
    }

    private Node createProduct(final String name, final String lifecycle) throws RepositoryException {
        final MockNode product = new MockNode(name, ProductCatalogConstants.NodeTypes.Product.NAME);
        product.setProperty(ProductCatalogConstants.Properties.LIFECYCLE, lifecycle);
        return product;
    }
}
