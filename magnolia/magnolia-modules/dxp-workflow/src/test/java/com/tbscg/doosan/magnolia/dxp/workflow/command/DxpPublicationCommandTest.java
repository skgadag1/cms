package com.tbscg.doosan.magnolia.dxp.workflow.command;

import com.google.common.collect.ImmutableMap;
import com.tbscg.doosan.magnolia.bobcat.field.filter.NodeFilter;
import com.tbscg.doosan.magnolia.dxp.core.ActionType;
import com.tbscg.doosan.magnolia.dxp.workflow.constant.WorkflowConstant;
import com.tbscg.doosan.magnolia.dxp.workflow.service.EventHandlingService;
import com.tbscg.doosan.magnolia.dxp.workflow.service.IndexingService;
import com.tbscg.doosan.magnolia.dxp.workflow.service.exception.IndexingException;
import com.tbscg.doosan.magnolia.dxp.workflow.service.relatedNodesFinding.RelatedNodesFinder;
import com.tbscg.doosan.magnolia.dxp.workflow.util.TestUtils;
import com.tbscg.doosan.magnolia.dxp.workflow.util.WorkflowExecutionSettings;
import com.tbscg.doosan.magnolia.product.catalog.util.ProductCatalogConstants;
import info.magnolia.cms.core.version.VersionManager;
import info.magnolia.context.Context;
import info.magnolia.context.MgnlContext;
import info.magnolia.context.SystemContext;
import info.magnolia.objectfactory.ComponentProvider;
import info.magnolia.objectfactory.Components;
import info.magnolia.publishing.exception.PublicationException;
import info.magnolia.publishing.operation.SendOperation;
import info.magnolia.publishing.sender.Sender;
import info.magnolia.test.mock.MockComponentProvider;
import info.magnolia.test.mock.MockContext;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import javax.jcr.Node;
import javax.jcr.Property;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import java.util.Collections;
import java.util.List;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertFalse;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.Silent.class)
public class DxpPublicationCommandTest {

    private static final String PAGE_PATH = "/site/na/index";
    private static final String PAGE_UUID = "page-uuid";
    private static final String PUBLISH_TIME = "2026-04-27T12:00:00Z";

    @Mock
    private VersionManager versionManager;

    @Mock
    private ComponentProvider componentProvider;

    @Mock
    private IndexingService indexingService;

    @Mock
    private EventHandlingService eventHandlingService;

    @Mock
    private RelatedNodesFinder relatedNodesFinder;

    @Mock
    private Sender sender;

    @Mock
    private SystemContext systemContext;

    private DxpPublicationCommand command;
    private Context context;

    @Before
    public void setUp() throws Exception {
        command = new DxpPublicationCommand(versionManager, componentProvider, indexingService, eventHandlingService, relatedNodesFinder);
        final MockComponentProvider mockComponentProvider = new MockComponentProvider();
        mockComponentProvider.setInstance(SystemContext.class, systemContext);
        Components.setComponentProvider(mockComponentProvider);
        final Session websiteSession = mockWebsiteSession();
        final MockContext mockContext = spy(new MockContext());
        context = mockContext;
        mockContext.addSession(WorkflowConstant.WEBSITE_WORKSPACE, websiteSession);
        MgnlContext.setInstance(context);
        when(systemContext.getJCRSession(anyString())).thenReturn(websiteSession);

        setCommandScope(WorkflowConstant.WEBSITE_WORKSPACE);
        doReturn(sender).when(componentProvider).newInstance(any(), any());
        when(sender.publish(any(), any())).thenReturn(Collections.emptyList());
        when(indexingService.index(anyString(), anyString(), anyString(), anyString(), anyBoolean())).thenReturn(true);
        when(indexingService.deindex(anyString(), anyString(), anyString(), anyString())).thenReturn(true);
        when(relatedNodesFinder.findRelatedNodesForPublication(anyString(), anyBoolean(), anyString())).thenReturn(Collections.emptyList());
    }

    @After
    public void tearDown() {
        Components.setComponentProvider(null);
        MgnlContext.setInstance(null);
    }

    @Test
    public void shouldPublishThenHandleEventsAndIndex() throws Exception {
        assertTrue(command.execute(context));

        final InOrder inOrder = inOrder(sender, eventHandlingService, relatedNodesFinder, indexingService);
        inOrder.verify(sender).publish(any(), any());
        inOrder.verify(eventHandlingService).handleEvent(
                eq(ActionType.PUBLISH),
                eq(WorkflowConstant.WEBSITE_WORKSPACE),
                eq(PAGE_PATH),
                eq(PAGE_UUID),
                eq(false),
                anyLong());
        inOrder.verify(relatedNodesFinder).findRelatedNodesForPublication(
                WorkflowConstant.WEBSITE_WORKSPACE,
                false,
                PAGE_PATH);
        inOrder.verify(indexingService).index(
                eq(WorkflowConstant.WEBSITE_WORKSPACE),
                eq(PAGE_PATH),
                eq(PAGE_UUID),
                eq(PUBLISH_TIME),
                eq(false));
    }

    @Test
    public void shouldReturnFalseWhenIndexingReturnsFalse() throws Exception {
        when(indexingService.index(anyString(), anyString(), anyString(), anyString(), anyBoolean())).thenReturn(false);

        assertFalse(command.execute(context));

        verify(sender).publish(any(), any());
        verify(eventHandlingService).handleEvent(anyString(), anyString(), anyString(), anyString(), anyBoolean(), anyLong());
    }

    @Test(expected = PublicationException.class)
    public void shouldThrowWhenDefaultPublishFails() throws Exception {
        final SendOperation.OperationResult result = SendOperation.OperationResult.error()
                .exception(new Exception("error"))
                .build();
        when(sender.publish(any(), any())).thenReturn(List.of(result));

        command.execute(context);
    }

    @Test
    public void shouldHandleDeletionByDeindexingAfterDeletionEventAndDefaultPublish() throws Exception {
        doReturn(true).when(context).get("isDeletion");

        assertTrue(command.execute(context));

        verify(eventHandlingService).handleEvent(
                ActionType.DELETE,
                WorkflowConstant.WEBSITE_WORKSPACE,
                PAGE_PATH,
                PAGE_UUID,
                true);
        verify(indexingService).deindex(WorkflowConstant.WEBSITE_WORKSPACE, PAGE_PATH, PAGE_UUID, PUBLISH_TIME);
        verify(sender).publish(any(), any());
    }

    @Test
    public void shouldSwallowDeletionIndexingExceptionAfterDefaultPublish() throws Exception {
        doReturn(true).when(context).get("isDeletion");
        when(indexingService.deindex(anyString(), anyString(), anyString(), anyString()))
                .thenThrow(new IndexingException("error"));

        assertTrue(command.execute(context));

        verify(sender).publish(any(), any());
        verify(indexingService).deindex(WorkflowConstant.WEBSITE_WORKSPACE, PAGE_PATH, PAGE_UUID, PUBLISH_TIME);
    }

    @Test
    public void shouldSkipPostPublishSideEffectsForNonIndexableAndNonEventWorkspace() throws Exception {
        setCommandScope("tags");

        assertTrue(command.execute(context));

        verify(eventHandlingService, never()).handleEvent(anyString(), anyString(), anyString(), anyString(), anyBoolean(), anyLong());
        verify(indexingService, never()).index(anyString(), anyString(), anyString(), anyString(), anyBoolean());
        verify(indexingService, never()).deindex(anyString(), anyString(), anyString(), anyString());
    }

    @Test
    public void setNodeFilterParams_shouldSetFilter() {
        final String propertyName = ProductCatalogConstants.Properties.LIFECYCLE;
        final String propertyValue = ProductCatalogConstants.Lifecycle.HISTORICAL;
        final String propertyDefaultValue = ProductCatalogConstants.Lifecycle.CURRENT;
        final String nodeType = ProductCatalogConstants.NodeTypes.Product.NAME;
        final boolean hideEmpty = true;
        final String nodeName = "nodeName";

        command.setNodeFilterParams(ImmutableMap.<String, Object>builder()
                .put(NodeFilter.PROPERTY_NAME, propertyName)
                .put(NodeFilter.PROPERTY_VALUE, propertyValue)
                .put(NodeFilter.PROPERTY_DEFAULT_VALUE, propertyDefaultValue)
                .put(NodeFilter.NODE_TYPE, nodeType)
                .put(NodeFilter.HIDE_EMPTY, hideEmpty)
                .put(NodeFilter.NODE_NAME, nodeName)
                .build());

        final NodeFilter nodeFilter = command.getNodeFilter();
        assertThat(nodeFilter, not(nullValue()));
        assertThat(nodeFilter.getPropertyName(), equalTo(propertyName));
        assertThat(nodeFilter.getPropertyValue(), equalTo(propertyValue));
        assertThat(nodeFilter.getPropertyDefaultValue(), equalTo(propertyDefaultValue));
        assertThat(nodeFilter.getNodeType(), equalTo(nodeType));
        assertThat(nodeFilter.isHideEmpty(), equalTo(hideEmpty));
        assertThat(nodeFilter.getNodeName(), equalTo(nodeName));
    }

    @Test
    public void setNodeFilterParams_shouldNotSetFilter_onNullInput() {
        command.setNodeFilterParams(null);

        assertThat(command.getNodeFilter(), nullValue());
    }

    @Test
    public void setNodeFilterParams_shouldHandle_emptyInput() {
        command.setNodeFilterParams(Collections.emptyMap());

        final NodeFilter nodeFilter = command.getNodeFilter();
        assertThat(nodeFilter, not(nullValue()));
        assertThat(nodeFilter.getNodeName(), nullValue());
        assertThat(nodeFilter.getNodeType(), nullValue());
        assertThat(nodeFilter.getPropertyValue(), nullValue());
        assertThat(nodeFilter.getPropertyName(), nullValue());
        assertThat(nodeFilter.getPropertyDefaultValue(), nullValue());
        assertThat(nodeFilter.isHideEmpty(), equalTo(false));
    }

    private void setCommandScope(final String workspace) throws Exception {
        TestUtils.setFieldInSuperclass(command, "repository", workspace);
        TestUtils.setFieldInSuperclass(command, "path", DxpPublicationCommandTest.PAGE_PATH);
        TestUtils.setFieldInSuperclass(command, "uuid", DxpPublicationCommandTest.PAGE_UUID);
        TestUtils.setFieldInSuperclass(command, "recursive", false);
    }

    private Session mockWebsiteSession() throws RepositoryException {
        final Session session = org.mockito.Mockito.mock(Session.class);
        final Node page = org.mockito.Mockito.mock(Node.class);
        final Property versionTime = org.mockito.Mockito.mock(Property.class);
        when(session.getNode(PAGE_PATH)).thenReturn(page);
        when(session.getNodeByIdentifier(PAGE_UUID)).thenReturn(page);
        when(page.getPath()).thenReturn(PAGE_PATH);
        when(page.getIdentifier()).thenReturn(PAGE_UUID);
        when(page.getSession()).thenReturn(session);
        when(page.isLocked()).thenReturn(false);
        when(page.getParent()).thenReturn(null);
        when(page.hasProperty(WorkflowExecutionSettings.VERSION_TIME_PROPERTY)).thenReturn(true);
        when(page.getProperty(WorkflowExecutionSettings.VERSION_TIME_PROPERTY)).thenReturn(versionTime);
        when(versionTime.getString()).thenReturn(PUBLISH_TIME);
        return session;
    }
}
