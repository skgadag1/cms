package com.tbscg.doosan.magnolia.dxp.workflow.service;

import com.tbscg.doosan.magnolia.dxp.workflow.constant.WorkflowConstant;
import com.tbscg.doosan.magnolia.dxp.workflow.service.exception.IndexingException;
import com.tbscg.doosan.magnolia.dxp.workflow.service.indexing.AttachmentIndexingService;
import com.tbscg.doosan.magnolia.dxp.workflow.service.indexing.PageIndexingService;
import com.tbscg.doosan.magnolia.dxp.workflow.service.indexing.ProductIndexingService;
import com.tbscg.doosan.magnolia.product.catalog.util.ProductCatalogConstants;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import static com.tbscg.doosan.magnolia.dxp.workflow.service.configuration.IndexingServiceConfiguration.DEINDEX_ACTION;
import static com.tbscg.doosan.magnolia.dxp.workflow.service.configuration.IndexingServiceConfiguration.INDEX_ACTION;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class IndexingServiceTest {

    private static final String PATH = "/na-test";
    private static final String UUID = "node-uuid";
    private static final String PUBLISH_TIME = "2026-04-27T12:00:00Z";
    @Mock
    private PageIndexingService pageIndexingServiceMock;

    @Mock
    private ProductIndexingService productIndexingServiceMock;

    @Mock
    private AttachmentIndexingService attachmentIndexingService;

    private IndexingService indexingService;

    @Before
    public void setUp() {
        when(pageIndexingServiceMock.getWorkspace()).thenReturn(WorkflowConstant.WEBSITE_WORKSPACE);
        when(productIndexingServiceMock.getWorkspace()).thenReturn(ProductCatalogConstants.PRODUCTS_WORKSPACE);
        when(attachmentIndexingService.getWorkspace()).thenReturn(ProductCatalogConstants.ATTACHMENTS_WORKSPACE);
        this.indexingService = new IndexingService(pageIndexingServiceMock, productIndexingServiceMock, attachmentIndexingService);
        clearInvocations(pageIndexingServiceMock, productIndexingServiceMock, attachmentIndexingService);
    }

    @Test
    public void shouldIndex_successfullyIndexPages() throws IndexingException {
        when(pageIndexingServiceMock.index(INDEX_ACTION, PATH, UUID, PUBLISH_TIME, false)).thenReturn(true);

        final boolean result = indexingService.index(WorkflowConstant.WEBSITE_WORKSPACE, PATH, UUID, PUBLISH_TIME, false);

        assertTrue(result);
        verify(pageIndexingServiceMock).index(INDEX_ACTION, PATH, UUID, PUBLISH_TIME, false);
        verifyNoInteractions(productIndexingServiceMock, attachmentIndexingService);
    }

    @Test
    public void shouldIndex_unsuccessfullyIndexPages() throws IndexingException {
        when(pageIndexingServiceMock.index(INDEX_ACTION, PATH, UUID, PUBLISH_TIME, false)).thenReturn(false);

        final boolean result = indexingService.index(WorkflowConstant.WEBSITE_WORKSPACE, PATH, UUID, PUBLISH_TIME, false);

        assertFalse(result);
        verify(pageIndexingServiceMock).index(INDEX_ACTION, PATH, UUID, PUBLISH_TIME, false);
        verifyNoInteractions(productIndexingServiceMock, attachmentIndexingService);
    }

    @Test
    public void shouldReindex_successfullyReindexPages() throws IndexingException {
        when(pageIndexingServiceMock.index(DEINDEX_ACTION, PATH, UUID, PUBLISH_TIME, true)).thenReturn(true);

        final boolean result = indexingService.deindex(WorkflowConstant.WEBSITE_WORKSPACE, PATH, UUID, PUBLISH_TIME);

        assertTrue(result);
        verify(pageIndexingServiceMock).index(DEINDEX_ACTION, PATH, UUID, PUBLISH_TIME, true);
        verifyNoInteractions(productIndexingServiceMock, attachmentIndexingService);
    }

    @Test
    public void shouldReindex_unsuccessfullyReindexPages() throws IndexingException {
        when(pageIndexingServiceMock.index(DEINDEX_ACTION, PATH, UUID, PUBLISH_TIME, true)).thenReturn(false);

        final boolean result = indexingService.deindex(WorkflowConstant.WEBSITE_WORKSPACE, PATH, UUID, PUBLISH_TIME);

        assertFalse(result);
        verify(pageIndexingServiceMock).index(DEINDEX_ACTION, PATH, UUID, PUBLISH_TIME, true);
        verifyNoInteractions(productIndexingServiceMock, attachmentIndexingService);
    }

    @Test
    public void shouldIndex_successfullyIndexProducts() throws IndexingException {
        when(productIndexingServiceMock.index(INDEX_ACTION, PATH, UUID, PUBLISH_TIME, false)).thenReturn(true);

        final boolean result = indexingService.index(ProductCatalogConstants.PRODUCTS_WORKSPACE, PATH, UUID, PUBLISH_TIME, false);

        assertTrue(result);
        verify(productIndexingServiceMock).index(INDEX_ACTION, PATH, UUID, PUBLISH_TIME, false);
        verifyNoInteractions(pageIndexingServiceMock, attachmentIndexingService);
    }

    @Test
    public void shouldIndex_unsuccessfullyIndexProducts() throws IndexingException {
        when(productIndexingServiceMock.index(INDEX_ACTION, PATH, UUID, PUBLISH_TIME, false)).thenReturn(false);

        final boolean result = indexingService.index(ProductCatalogConstants.PRODUCTS_WORKSPACE, PATH, UUID, PUBLISH_TIME, false);

        assertFalse(result);
        verify(productIndexingServiceMock).index(INDEX_ACTION, PATH, UUID, PUBLISH_TIME, false);
        verifyNoInteractions(pageIndexingServiceMock, attachmentIndexingService);
    }

    @Test
    public void shouldReindex_successfullyReindexProducts() throws IndexingException {
        when(productIndexingServiceMock.index(DEINDEX_ACTION, PATH, UUID, PUBLISH_TIME, true)).thenReturn(true);

        final boolean result = indexingService.deindex(ProductCatalogConstants.PRODUCTS_WORKSPACE, PATH, UUID, PUBLISH_TIME);

        assertTrue(result);
        verify(productIndexingServiceMock).index(DEINDEX_ACTION, PATH, UUID, PUBLISH_TIME, true);
        verifyNoInteractions(pageIndexingServiceMock, attachmentIndexingService);
    }

    @Test
    public void shouldReindex_unsuccessfullyReindexProducts() throws IndexingException {
        when(productIndexingServiceMock.index(DEINDEX_ACTION, PATH, UUID, PUBLISH_TIME, true)).thenReturn(false);

        final boolean result = indexingService.deindex(ProductCatalogConstants.PRODUCTS_WORKSPACE, PATH, UUID, PUBLISH_TIME);

        assertFalse(result);
        verify(productIndexingServiceMock).index(DEINDEX_ACTION, PATH, UUID, PUBLISH_TIME, true);
        verifyNoInteractions(pageIndexingServiceMock, attachmentIndexingService);
    }

    @Test
    public void shouldIndex_successfullyIndexAttachments() throws IndexingException {
        when(attachmentIndexingService.index(INDEX_ACTION, PATH, UUID, PUBLISH_TIME, false)).thenReturn(true);

        final boolean result = indexingService.index(ProductCatalogConstants.ATTACHMENTS_WORKSPACE, PATH, UUID, PUBLISH_TIME, false);

        assertTrue(result);
        verify(attachmentIndexingService).index(INDEX_ACTION, PATH, UUID, PUBLISH_TIME, false);
        verifyNoInteractions(pageIndexingServiceMock, productIndexingServiceMock);
    }

    @Test
    public void shouldIndex_unsuccessfullyIndexAttachments() throws IndexingException {
        when(attachmentIndexingService.index(INDEX_ACTION, PATH, UUID, PUBLISH_TIME, false)).thenReturn(false);

        final boolean result = indexingService.index(ProductCatalogConstants.ATTACHMENTS_WORKSPACE, PATH, UUID, PUBLISH_TIME, false);

        assertFalse(result);
        verify(attachmentIndexingService).index(INDEX_ACTION, PATH, UUID, PUBLISH_TIME, false);
        verifyNoInteractions(pageIndexingServiceMock, productIndexingServiceMock);
    }

    @Test
    public void shouldReindex_successfullyReindexAttachments() throws IndexingException {
        when(attachmentIndexingService.index(DEINDEX_ACTION, PATH, UUID, PUBLISH_TIME, true)).thenReturn(true);

        final boolean result = indexingService.deindex(ProductCatalogConstants.ATTACHMENTS_WORKSPACE, PATH, UUID, PUBLISH_TIME);

        assertTrue(result);
        verify(attachmentIndexingService).index(DEINDEX_ACTION, PATH, UUID, PUBLISH_TIME, true);
        verifyNoInteractions(pageIndexingServiceMock, productIndexingServiceMock);
    }

    @Test
    public void shouldReindex_unsuccessfullyReindexAttachments() throws IndexingException {
        when(attachmentIndexingService.index(DEINDEX_ACTION, PATH, UUID, PUBLISH_TIME, true)).thenReturn(false);

        final boolean result = indexingService.deindex(ProductCatalogConstants.ATTACHMENTS_WORKSPACE, PATH, UUID, PUBLISH_TIME);

        assertFalse(result);
        verify(attachmentIndexingService).index(DEINDEX_ACTION, PATH, UUID, PUBLISH_TIME, true);
        verifyNoInteractions(pageIndexingServiceMock, productIndexingServiceMock);
    }

    @Test
    public void shouldUseDefaultIndexingServiceForUnsupportedWorkspace() throws IndexingException {
        assertTrue(indexingService.index("tags", PATH, UUID, PUBLISH_TIME, false));
        assertTrue(indexingService.deindex("tags", PATH, UUID, PUBLISH_TIME));

        verifyNoInteractions(pageIndexingServiceMock, productIndexingServiceMock, attachmentIndexingService);
    }
}
