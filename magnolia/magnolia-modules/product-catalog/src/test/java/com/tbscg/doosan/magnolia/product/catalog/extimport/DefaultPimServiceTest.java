package com.tbscg.doosan.magnolia.product.catalog.extimport;

import com.tbscg.doosan.magnolia.product.catalog.TestUtils;
import com.tbscg.doosan.magnolia.product.catalog.extimport.gpim.*;
import com.tbscg.doosan.magnolia.product.catalog.extimport.pimpp.*;
import info.magnolia.init.MagnoliaConfigurationProperties;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.slf4j.Logger;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.*;

@Slf4j
@RunWith(MockitoJUnitRunner.class)
public class DefaultPimServiceTest {
    private final String LOCALE = "en_US";
    private static final String HISTORICAL_PRODUCTS_ENABLE = "magnolia.bobcat.historical.products.enable";

    @Mock
    MagnoliaConfigurationProperties magnoliaConfigurationPropertiesMock;
    Logger logger;

    @Mock
    GpimClient gpimClientMock;

    @Mock
    PimppClient pimppClientMock;

    private DefaultPimService defaultPimService;

    @Before
    public void setup() {
        GpimClientFactory gpimClientFactory = mock(GpimClientFactory.class);
        when(gpimClientFactory.createGpimClient()).thenReturn(gpimClientMock);

        PimppClientFactory pimppClientFactory = mock(PimppClientFactory.class);
        when(pimppClientFactory.createPimppClient()).thenReturn(pimppClientMock);

        // Disable historical models by default
        when(magnoliaConfigurationPropertiesMock.getProperty(HISTORICAL_PRODUCTS_ENABLE))
                .thenReturn("false");

        defaultPimService = new DefaultPimService(
                new GpimModelService(gpimClientFactory),
                new PimppModelService(pimppClientFactory),
                magnoliaConfigurationPropertiesMock);
    }

    @Test
    public void testGetGPIMProductCatalog_whenProductVariants() throws GpimClientException, URISyntaxException, IOException, PimServiceException {
        // setup
        when(gpimClientMock.getCategory("compactNAPublic_BaseMachines", LOCALE))
                .thenReturn(TestUtils.loadFromJsonFile(
                        "com/tbscg/doosan/magnolia/product/catalog/extimport/gpim_compactNAPublic_BaseMachines.json", GpimCategory.class));
        when(gpimClientMock.getCategory("simplifiedCategoryWithAgrupation", LOCALE))
                .thenReturn(TestUtils.loadFromJsonFile(
                        "com/tbscg/doosan/magnolia/product/catalog/extimport/gpim_simplifiedCategoriesWithAgrupation.json", GpimCategory.class));

        // when
        PimCatalog catalog = defaultPimService.getGPIMProductCatalog(LOCALE);

        // then
        assertNotNull(catalog);
        assertEquals(1, CollectionUtils.size(catalog.getCategories()));

        PimCategory rootCategory = catalog.getCategories().stream().findFirst().get();
        categoryVerifier("Imported", 1, 0, 0).accept(rootCategory);

        PimCategory category = rootCategory.getSubCategories().stream().findFirst().get();
        categoryVerifier("Simplified category", 0, 0, 1).accept(category);

        PimGroup group = category.getProductGroups().stream().findFirst().get();
        assertEquals("T595", group.getGroupName());
        assertEquals(4, CollectionUtils.size(group.getProducts()));

        modelVerifier("T595 T4", "Current", "M0249").accept(group.getProducts().get(0));
        modelVerifier("T595 T4", "Current", "M0249_53179").accept(group.getProducts().get(1));
        modelVerifier("T595 T4 RS", "Current", "M0249_53962").accept(group.getProducts().get(2));
        modelVerifier("T595 T4 RS", "Current", "D-53962").accept(group.getProducts().get(3));

        verify(gpimClientMock, times(1)).getCategory("compactNAPublic_BaseMachines", LOCALE);
        verify(gpimClientMock, times(1)).getCategory("simplifiedCategoryWithAgrupation", LOCALE);
        verifyNoMoreInteractions(gpimClientMock);
        verifyZeroInteractions(pimppClientMock);

    }

    @Test
    public void testGetGPIMAttachmentCatalog() throws GpimClientException, URISyntaxException, IOException, PimServiceException {
        // setup
        when(gpimClientMock.getCategory("compactNAPublic_Attachments", LOCALE)).thenReturn(TestUtils.loadFromJsonFile("com/tbscg/doosan/magnolia/product/catalog/extimport/gpim_compactNAPublic_Attachments.json", GpimCategory.class));
        when(gpimClientMock.getCategory("theAttachmentCategory", LOCALE)).thenReturn(TestUtils.loadFromJsonFile("com/tbscg/doosan/magnolia/product/catalog/extimport/gpim_attachmentCategory.json", GpimCategory.class));
        when(gpimClientMock.getProduct("1111111")).thenReturn(TestUtils.loadFromJsonFile("com/tbscg/doosan/magnolia/product/catalog/extimport/gpim_attachment_relatedProductsCategory.json", GpimProduct.class));
        when(gpimClientMock.getProduct("3333333")).thenReturn(TestUtils.loadFromJsonFile("com/tbscg/doosan/magnolia/product/catalog/extimport/gpim_attachment_relatedProductsCategory3.json", GpimProduct.class));

        // when
        PimCatalog catalog = defaultPimService.getGPIMAttachmentCatalog(LOCALE);

        // then
        assertNotNull(catalog);
        assertEquals(1, CollectionUtils.size(catalog.getCategories()));

        PimCategory rootCategory = catalog.getCategories().stream().findFirst().get();
        categoryVerifier("Imported", 1, 0, 0).accept(rootCategory);

        PimCategory category = rootCategory.getSubCategories().stream().findFirst().get();
        categoryVerifier("Attachment category", 0, 1, 1).accept(category);

        modelVerifier("Another Attachment 3", "Current", "3333333").accept(category.getProducts().get(0));

        PimGroup group = category.getProductGroups().stream().findFirst().get();
        assertEquals("Attachment", group.getGroupName());
        assertEquals(2, CollectionUtils.size(group.getProducts()));

        modelVerifier("Attachment 1", "Current", "1111111").accept(group.getProducts().get(0));
        modelVerifier("Attachment 2", "Current", "2222222").accept(group.getProducts().get(1));

        PimModel attachment = group.getProducts().stream().findFirst().get();
        PimModel relatedProduct = attachment.getCompatibleCarriers().stream().findFirst().get();
        assertEquals("7105500",relatedProduct.getCode());

        verify(gpimClientMock, times(1)).getCategory("compactNAPublic_Attachments", LOCALE);
        verify(gpimClientMock, times(1)).getCategory("theAttachmentCategory", LOCALE);
        verify(gpimClientMock, times(1)).getProduct("1111111");
        verify(gpimClientMock, times(1)).getProduct("2222222");
        verify(gpimClientMock, times(1)).getProduct("3333333");
        verifyNoMoreInteractions(gpimClientMock);
        verifyZeroInteractions(pimppClientMock);

    }

    @Test
    public void testGetEMEAProductCatalog() throws PimppClientException, URISyntaxException, IOException, PimServiceException {
        // setup
        when(pimppClientMock.getCatalog())
                .thenReturn(TestUtils.loadFromXmlFile(
                        "com/tbscg/doosan/magnolia/product/catalog/extimport/pimpp_catalogue.xml",
                        PimppCatalog.class));

        // when
        PimCatalog catalog = defaultPimService.getEMEAProductCatalog();

        // then
        assertNotNull(catalog);
        assertEquals(2, CollectionUtils.size(catalog.getCategories()));

        Map<String, ? extends PimCategory> categoryMap = catalog.getCategories().stream()
                .collect(Collectors.toMap(PimCategory::getName, Function.identity()));

        PimCategory category1 = categoryMap.get("Imported-Category 1");
        categoryVerifier("Imported-Category 1", 1, 0, 0).accept(category1);

        PimCategory subcategory1 = category1.getSubCategories().get(0);
        categoryVerifier("Cat 1 - Subcategory 1", 0, 1, 1).accept(subcategory1);

        modelVerifier("P1", "Current", "PIM1").accept(subcategory1.getProducts().get(0));

        PimGroup group1 = subcategory1.getProductGroups().get(0);
        assertEquals("P2", group1.getGroupName());
        assertEquals(2, CollectionUtils.size(group1.getProducts()));

        modelVerifier(group1.getGroupName(), "Current", "PIM2").accept(group1.getProducts().get(0));
        modelVerifier(group1.getGroupName(), "Current", "PIM3").accept(group1.getProducts().get(1));

        PimCategory category2 = categoryMap.get("Imported-Category 2");
        categoryVerifier("Imported-Category 2", 1, 0, 0).accept(category2);

        PimCategory subcategory2 = category2.getSubCategories().get(0);
        categoryVerifier("Cat 2 - Subcategory 1", 0, 1, 0).accept(subcategory2);

        // No PIM code, defaulting to ID
        modelVerifier("P3", "Current", "9999999999").accept(subcategory2.getProducts().get(0));

        verify(pimppClientMock, times(1)).getCatalog();
        verifyNoMoreInteractions(pimppClientMock);
        verifyZeroInteractions(gpimClientMock);

    }

    @Test
    public void testGetEMEAAttachmentCatalog() throws PimppClientException, URISyntaxException, IOException, PimServiceException {
        // setup
        when(pimppClientMock.getCatalog())
                .thenReturn(TestUtils.loadFromXmlFile(
                        "com/tbscg/doosan/magnolia/product/catalog/extimport/pimpp_catalogue.xml",
                        PimppCatalog.class));

        // when
        PimCatalog catalog = defaultPimService.getEMEAAttachmentCatalog();

        // then
        assertNotNull(catalog);
        assertEquals(1, CollectionUtils.size(catalog.getCategories()));
        PimCategory rootCategory = catalog.getCategories().stream().findFirst().get();
        categoryVerifier("Imported-Attachments", 1, 0, 0).accept(rootCategory);


        Map<String, ? extends PimCategory> categoryMap = rootCategory.getSubCategories().stream()
                .collect(Collectors.toMap(PimCategory::getName, Function.identity()));

        PimCategory category1 = categoryMap.get("Attachment Cat1");
        categoryVerifier("Attachment Cat1", 1, 0, 0).accept(category1);

        PimCategory subcategory1 = category1.getSubCategories().get(0);
        categoryVerifier("Attachment Cat1 - Subcategory 1", 0, 2, 0).accept(subcategory1);

        modelVerifier("A1", "Current", "141414141414").accept(subcategory1.getProducts().get(0));
        modelVerifier("A2", "Current", "1515151515").accept(subcategory1.getProducts().get(1));

        verify(pimppClientMock, times(1)).getCatalog();
        verifyNoMoreInteractions(pimppClientMock);
        verifyZeroInteractions(gpimClientMock);

    }

    private Consumer<PimCategory> categoryVerifier(final String name, int subCategoriesSize, int productsSize, int groupsSize) {
        return (category) -> {
            assertNotNull(category);
            assertEquals(name, category.getName());
            assertEquals(subCategoriesSize, CollectionUtils.size(category.getSubCategories()));
            assertEquals(productsSize, CollectionUtils.size(category.getProducts()));
            assertEquals(groupsSize, CollectionUtils.size(category.getProductGroups()));
        };
    }

    private Consumer<PimModel> modelVerifier(final String name, final String status, final String code) {
        return (model) -> {
            assertNotNull(model);
            assertEquals(name, model.getName());
            assertEquals(status, model.getLifecycle());
            assertEquals(code, model.getCode());
        };
    }

}