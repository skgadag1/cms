package com.tbscg.doosan.magnolia.product.catalog.extimport;

import com.tbscg.doosan.magnolia.product.catalog.extimport.buildandquote.BuildAndQuoteService;
import com.tbscg.doosan.magnolia.regions.service.RegionsService;
import info.magnolia.jcr.util.NodeUtil;
import info.magnolia.test.mock.MockUtil;
import info.magnolia.test.mock.jcr.MockSession;
import junitparams.JUnitParamsRunner;
import junitparams.Parameters;
import org.apache.commons.lang3.function.FailableCallable;
import org.junit.Test;
import org.junit.runner.RunWith;

import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

@RunWith(JUnitParamsRunner.class)
public class DefaultProductImportServiceTest {


    private static final String PRODUCTS_WORKSPACE = "products";

    private ProductImportService createProductImportService(BuildAndQuoteService buildAndQuoteService, RegionsService regionsService) {
        return new DefaultProductImportService(buildAndQuoteService, regionsService);
    }

    @Test
    public void testImportExternalCatalog_whenImportingProducts_NoCategories() throws Exception {
        // Setup
        // 1. Setup magnolia mocks
        final String workspace = "products" ;
        String theRootPath = "theRootPath";
        MockUtil.initMockContext();
        Session spiedSession = spy(new MockSession(workspace));
        MockUtil.setSessionAndHierarchyManager(spiedSession);
        spiedSession.getRootNode().addNode("theRootPath");

        // 2. Setup service
        ProductImportService productImportService = createProductImportService(categoryName -> {
            throw new RuntimeException("Should not be called at all");
        }, null);
        // 3. Setup catalog
        PimCatalog emptyCatalogMock = mock(PimCatalog.class);
        when(emptyCatalogMock.getCategories()).thenReturn(Collections.emptyList());
        PimCatalog emptyRelatedProductCatalogMock = mock(PimCatalog.class);
        when(emptyRelatedProductCatalogMock.getCategories()).thenReturn(Collections.emptyList());
        PimService relationSupplierMock =  mock(PimService.class);
        when(relationSupplierMock.getRelationDetails()).thenReturn(emptyRelatedProductCatalogMock);

        // Run
        productImportService.importExternalCatalog(() -> emptyCatalogMock, () -> (PimCatalog) relationSupplierMock, theRootPath, workspace);

        // Verify
        verify(emptyCatalogMock, times(1)).getCategories();
        verify(spiedSession, times(1)).getNode(theRootPath);
        verify(spiedSession, times(1)).save();
    }

    @Test
    public void testImportExternalCatalog_whenImportingAttachments_NoCategories() throws Exception {
        // Setup
        // 1. Setup magnolia mocks
        final String workspace = "attachments" ;
        String theRootPath = "theRootPath";
        MockUtil.initMockContext();

        Session spiedSession = spy(new MockSession(workspace));
        MockUtil.setSessionAndHierarchyManager(spiedSession);
        spiedSession.getRootNode().addNode("theRootPath");

        Session spiedProductsSession = spy(new MockSession(PRODUCTS_WORKSPACE));
        MockUtil.setSessionAndHierarchyManager(spiedProductsSession);
        spiedProductsSession.getRootNode().addNode("theRootPath");

        // 2. Setup service
        ProductImportService productImportService = createProductImportService(categoryName -> {
            throw new RuntimeException("Should not be called at all");
        }, null);
        // 3. Setup catalog
        PimCatalog emptyCatalogMock = mock(PimCatalog.class);
        when(emptyCatalogMock.getCategories()).thenReturn(Collections.emptyList());
        PimCatalog emptyRelatedProductCatalogMock = mock(PimCatalog.class);
        when(emptyRelatedProductCatalogMock.getCategories()).thenReturn(Collections.emptyList());
        PimService relationSupplierMock =  mock(PimService.class);
        when(relationSupplierMock.getRelationDetails()).thenReturn(emptyRelatedProductCatalogMock);

        // Run
        productImportService.importExternalCatalog(() -> emptyCatalogMock, () -> (PimCatalog) relationSupplierMock, theRootPath, workspace);

        // Verify
        verify(emptyCatalogMock, times(2)).getCategories();
        verify(spiedSession, times(1)).getNode(theRootPath);
        verify(spiedSession, times(1)).save();
        verify(spiedProductsSession, times(1)).getNode(theRootPath);
    }

    @Test
    public void testImportExternalCatalog_whenImportingProducts() throws Exception {
        // Setup
        // 1. Setup catalog
        PimCatalog catalogMock = createCatalogMock();

        // 2. Setup magnolia mocks
        String theRootPath = "theRootPath";
        String workspace = "products";
        MockUtil.initMockContext();
        Session spiedSession = spy(new MockSession(workspace));
        MockUtil.setSessionAndHierarchyManager(spiedSession);
        String parentType = "mgnl:folder";
        Node parentNode = spiedSession.getRootNode().addNode(theRootPath, parentType);

        // 3. Setup service
        ProductImportService productImportService = createProductImportService(categoryName -> Optional.empty(), buildRegionsServiceMock());

        // Run
        productImportService.importExternalCatalog(() -> catalogMock, () -> catalogMock, theRootPath, workspace);

        // Verify
        verify(spiedSession, times(1)).save();

        verifyNode(parentNode, theRootPath, null, parentType, 1);
        Node category1node = parentNode.getNodes().nextNode();
        verifyNode(category1node, "Category1", null,"dxp:category", 2);
        Node subcategory1 = category1node.getNode("Subcategory1");
        verifyNode(subcategory1, "Subcategory1", "", "dxp:productGroup", 2); // 1 product & 1 group

        verifyNode(subcategory1.getNode("P1"), "P1", "theP1", "dxp:product", 0);
        Node group1 = subcategory1.getNode("Group1");
        verifyNode(group1, "Group1", null, "dxp:productGroup", 2);
        verifyNode(group1.getNode("P2"), "P2", "theP2", "dxp:product", 0);
        verifyNode(group1.getNode("P3"), "P3", "theP3", "dxp:product", 0);

        Node subcategory2 = category1node.getNode("Subcategory2");
        verifyNode(subcategory2, "Subcategory2", "", "dxp:productGroup", 1); // 1 product
        verifyNode(subcategory2.getNode("P4"), "P4", "theP4", "dxp:product", 0);

    }

    @Test
    public void testImportExternalCatalog_whenImportingAttachments() throws Exception {
        // Setup
        // 1. Setup catalog
        PimCatalog catalogMock = createCatalogMock();
        PimCatalog relationSupplierMock = createRelationCatalogMock();

        // 2. Setup magnolia mocks
        String theRootPath = "theRootPath";
        String workspace = "attachments";
        MockUtil.initMockContext();
        Session spiedSession = spy(new MockSession(workspace));
        MockUtil.setSessionAndHierarchyManager(spiedSession);
        String parentType = "mgnl:folder";
        Node parentNode = spiedSession.getRootNode().addNode(theRootPath, parentType);

        Session spiedProductsSession = spy(new MockSession(PRODUCTS_WORKSPACE));
        MockUtil.setSessionAndHierarchyManager(spiedProductsSession);
        spiedProductsSession.getRootNode().addNode(theRootPath, parentType);

        // 3. Setup service
        ProductImportService productImportService = createProductImportService(categoryName -> Optional.empty(), buildRegionsServiceMock());

        // Run
        productImportService.importExternalCatalog(() -> catalogMock, () -> relationSupplierMock, theRootPath, workspace);

        // Verify
        verify(spiedSession, times(1)).save();

        // New structure: Region → Attachment Category → Attachment Group → Attachment
        verifyNode(parentNode, theRootPath, null, parentType, 1); // 1 category
        Node category1node = parentNode.getNodes().nextNode();
        verifyNode(category1node, "Category1", null, "dxp:attachmentCategory", 2); // 2 subcategories (as groups)
        
        Node subcategory1 = category1node.getNode("Subcategory1");
        verifyNode(subcategory1, "Subcategory1", "", "dxp:attachmentGroup", 2); // 1 product & 1 group
        verifyNode(subcategory1.getNode("P1"), "P1", "theP1", "dxp:attachment", 0);
        
        Node group1 = subcategory1.getNode("Group1");
        verifyNode(group1, "Group1", null, "dxp:attachmentGroup", 2);
        verifyNode(group1.getNode("P2"), "P2", "theP2", "dxp:attachment", 0);
        verifyNode(group1.getNode("P3"), "P3", "theP3", "dxp:attachment", 0);

        Node subcategory2 = category1node.getNode("Subcategory2");
        verifyNode(subcategory2, "Subcategory2", "", "dxp:attachmentGroup", 1); // 1 product
        verifyNode(subcategory2.getNode("P4"), "P4", "theP4", "dxp:attachment", 0);

    }

    private PimCatalog createCatalogMock() {
        PimGroup pimGroupMock1 = buildPimGroupMock(
                "Group1",
                Arrays.asList(
                        buildPimModelMock("P2", "theP2", "Current"),
                        buildPimModelMock("P3", "theP3", "Current")));

        PimCategory pimSubcategoryMock1 = buildCategoryMock(
                "Subcategory1",
                "Sub1",
                Collections.emptyList(),
                Arrays.asList(buildPimModelMock("P1", "theP1", "Current")),
                Arrays.asList(pimGroupMock1));

        PimCategory pimSubcategoryMock2 = buildCategoryMock(
                "Subcategory2",
                "Sub2",
                Collections.emptyList(),
                Arrays.asList(
                        buildPimModelMock("P4", "theP4", "Current")),
                Collections.emptyList()
        );

        return buildCatalogMock(
                Arrays.asList(
                    buildCategoryMock(
                            "Category1",
                            "Code1",
                            Arrays.asList(
                                    pimSubcategoryMock1,
                                    pimSubcategoryMock2
                            ),
                            Collections.emptyList(),
                            Collections.emptyList()
                )
        ));
    }
    private PimCatalog createRelationCatalogMock() {
        PimGroup pimGroupMock1 = buildPimGroupMock(
                "Group1",
                Arrays.asList(
                        buildPimModelMock("P2", "theP2", "Current"),
                        buildPimModelMock("P3", "theP3", "Current")));

        PimCategory pimSubcategoryMock1 = buildCategoryMock(
                "Subcategory1",
                "Sub1",
                Collections.emptyList(),
                Arrays.asList(buildPimModelMock("P1", "theP1", "Current")),
                Arrays.asList(pimGroupMock1));

        PimCategory pimSubcategoryMock2 = buildCategoryMock(
                "Subcategory2",
                "Sub2",
                Collections.emptyList(),
                Arrays.asList(
                        buildPimModelMock("P4", "theP4", "Current")),
                Collections.emptyList()
        );

        return buildCatalogMock(
                Arrays.asList(
                        buildCategoryMock(
                                "Category1",
                                "Code1",
                                Arrays.asList(
                                        pimSubcategoryMock1,
                                        pimSubcategoryMock2
                                ),
                                Collections.emptyList(),
                                Collections.emptyList()
                        )
                ));
    }
    private void verifyNode(final Node node, final String name, final String code, final String type, int numNodes) throws RepositoryException {
        assertNotNull(node);
        assertEquals(name, node.getName());
        assertEquals(type, node.getPrimaryNodeType().getName());
        if (code == null) {
            assertFalse(node.hasProperty("id"));
        } else {
            assertTrue(node.hasProperty("id"));
            assertEquals(code, node.getProperty("id").getValue().getString());
        }
        if (numNodes <= 0) {
            assertFalse(node.hasNodes());
        } else {
            assertEquals(numNodes, node.getNodes().getSize());
        }
    }

    private static PimCatalog buildCatalogMock(final List<? extends PimCategory> categories) {
        PimCatalog catalog = mock(PimCatalog.class);
        when(catalog.getCategories()).thenAnswer(invocationOnMock -> categories);
        return catalog;
    }

    private static RegionsService buildRegionsServiceMock() {
        RegionsService regionsService = mock(RegionsService.class);
        when(regionsService.isRegion("/theRootPath")).thenReturn(true);
        return regionsService;
    }

    private static PimCategory buildCategoryMock(
            final String categoryName,
            final String code,
            final List<? extends PimCategory> subcategories,
            final List<? extends PimModel> products,
            final List<? extends PimGroup> groups) {
        PimCategory category = mock(PimCategory.class);
        when(category.getName()).thenReturn(categoryName);
        when(category.getCode()).thenReturn(code);
        when(category.getSubCategories()).thenAnswer(invocationOnMock -> subcategories);
        when(category.getProducts()).thenAnswer(invocationOnMock -> products);
        when(category.getProductGroups()).thenAnswer(invocationOnMock -> groups);
        return category;
    }

    private static PimModel buildPimModelMock(
            final String name,
            final String code,
            final String lifecycle) {
        PimModel model = mock(PimModel.class);
        when(model.getName()).thenReturn(name);
        when(model.getCode()).thenReturn(code);
        when(model.getLifecycle()).thenReturn(lifecycle);
        return model;
    }

    private static PimGroup buildPimGroupMock(
            final String name,
            final List<? extends PimModel> products) {
        PimGroup group = mock(PimGroup.class);
        when(group.getGroupName()).thenReturn(name);
        when(group.getProducts()).thenAnswer(invocationOnMock -> products);
        return group;
    }
}