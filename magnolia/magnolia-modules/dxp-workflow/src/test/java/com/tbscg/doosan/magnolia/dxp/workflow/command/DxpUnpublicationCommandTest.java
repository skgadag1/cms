package com.tbscg.doosan.magnolia.dxp.workflow.command;

import com.tbscg.doosan.magnolia.dxp.core.ActionType;
import com.tbscg.doosan.magnolia.dxp.workflow.constant.WorkflowConstant;
import com.tbscg.doosan.magnolia.dxp.workflow.service.EventHandlingService;
import com.tbscg.doosan.magnolia.dxp.workflow.service.IndexingService;
import com.tbscg.doosan.magnolia.dxp.workflow.service.exception.IndexingException;
import info.magnolia.context.Context;
import info.magnolia.context.ContextFactory;
import info.magnolia.context.MgnlContext;
import info.magnolia.context.SystemContext;
import info.magnolia.jcr.util.NodeTypes;
import info.magnolia.objectfactory.ComponentProvider;
import info.magnolia.objectfactory.Components;
import info.magnolia.publishing.exception.PublicationException;
import info.magnolia.publishing.operation.SendOperation;
import info.magnolia.publishing.sender.Sender;
import info.magnolia.test.mock.MockComponentProvider;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.Session;
import javax.jcr.nodetype.NodeType;
import java.time.Instant;
import java.util.Collections;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.Silent.class)
public class DxpUnpublicationCommandTest {

    private static final String PAGE_PATH = "/site/na/index";
    private static final String PAGE_UUID = "page-uuid";
    private static final String NON_DXP_WORKSPACE = "tags";

    @Mock
    private ComponentProvider componentProvider;

    @Mock
    private IndexingService indexingService;

    @Mock
    private EventHandlingService eventHandlingService;

    @Mock
    private Sender sender;

    @Mock
    private SystemContext systemContext;

    @Mock
    private Context context;

    @Mock
    private Session session;

    @Mock
    private Node node;

    @Mock
    private NodeType nodeType;

    @Mock
    private NodeIterator nodeIterator;

    private DxpUnpublicationCommand command;

    @Before
    public void setUp() throws Exception {
        command = new DxpUnpublicationCommand(componentProvider, indexingService, eventHandlingService);
        command.setRepository(WorkflowConstant.WEBSITE_WORKSPACE);
        command.setPath(PAGE_PATH);
        command.setUuid(PAGE_UUID);

        final MockComponentProvider mockComponentProvider = new MockComponentProvider();
        mockComponentProvider.setInstance(ContextFactory.class, new ContextFactory());
        mockComponentProvider.setInstance(SystemContext.class, systemContext);
        Components.setComponentProvider(mockComponentProvider);
        MgnlContext.setInstance(context);

        when(systemContext.getJCRSession(anyString())).thenReturn(session);
        when(session.getNodeByIdentifier(PAGE_UUID)).thenReturn(node);
        when(session.getNode(PAGE_PATH)).thenReturn(node);
        when(node.getPrimaryNodeType()).thenReturn(nodeType);
        when(nodeType.getName()).thenReturn(NodeTypes.Page.NAME);
        when(node.getPath()).thenReturn(PAGE_PATH);
        when(node.getNodes()).thenReturn(nodeIterator);
        when(nodeIterator.hasNext()).thenReturn(false);
        doReturn(sender).when(componentProvider).newInstance(eq(Sender.class), eq(context), eq(componentProvider));
        when(sender.unpublish(anyList())).thenReturn(Collections.emptyList());
        when(indexingService.deindex(anyString(), anyString(), anyString(), anyString())).thenReturn(true);
    }

    @After
    public void tearDown() {
        Components.setComponentProvider(null);
        MgnlContext.setInstance(null);
    }

    @Test
    public void shouldUnpublishThenHandleEventAndDeindex() throws Exception {
        assertTrue(command.execute(context));

        final ArgumentCaptor<String> publishTime = ArgumentCaptor.forClass(String.class);
        final InOrder inOrder = inOrder(sender, eventHandlingService, indexingService);
        inOrder.verify(sender).unpublish(Collections.singletonList(node));
        inOrder.verify(eventHandlingService).handleEvent(
                ActionType.UNPUBLISH,
                WorkflowConstant.WEBSITE_WORKSPACE,
                PAGE_PATH,
                PAGE_UUID,
                true);
        inOrder.verify(indexingService).deindex(
                eq(WorkflowConstant.WEBSITE_WORKSPACE),
                eq(PAGE_PATH),
                eq(PAGE_UUID),
                publishTime.capture());
        assertThat(Instant.parse(publishTime.getValue()), notNullValue());
    }

    @Test
    public void shouldReturnFalseWhenDeindexReturnsFalse() throws Exception {
        when(indexingService.deindex(anyString(), anyString(), anyString(), anyString())).thenReturn(false);

        assertFalse(command.execute(context));

        verify(sender).unpublish(Collections.singletonList(node));
        verify(eventHandlingService).handleEvent(
                ActionType.UNPUBLISH,
                WorkflowConstant.WEBSITE_WORKSPACE,
                PAGE_PATH,
                PAGE_UUID,
                true);
    }

    @Test
    public void shouldSkipEventAndDeindexForNonDxpWorkspace() throws Exception {
        command.setRepository(NON_DXP_WORKSPACE);

        assertTrue(command.execute(context));

        verify(sender).unpublish(Collections.singletonList(node));
        verify(eventHandlingService, never()).handleEvent(anyString(), anyString(), anyString(), anyString(), anyBoolean());
        verify(indexingService, never()).deindex(anyString(), anyString(), anyString(), anyString());
    }

    @Test
    public void shouldPropagatePublicationExceptionWhenDefaultUnpublishFails() {
        final SendOperation.OperationResult result = SendOperation.OperationResult.error()
                .exception(new Exception("error"))
                .build();
        when(sender.unpublish(anyList())).thenReturn(List.of(result));

        assertThrows(PublicationException.class, () -> command.execute(context));

        verify(eventHandlingService, never()).handleEvent(anyString(), anyString(), anyString(), anyString(), anyBoolean());
        verify(indexingService, never()).deindex(anyString(), anyString(), anyString(), anyString());
    }

    @Test
    public void shouldPropagateIndexingException() {
        when(indexingService.deindex(anyString(), anyString(), anyString(), anyString()))
                .thenThrow(new IndexingException("error"));

        assertThrows(IndexingException.class, () -> command.execute(context));

        verify(sender).unpublish(Collections.singletonList(node));
        verify(eventHandlingService).handleEvent(
                ActionType.UNPUBLISH,
                WorkflowConstant.WEBSITE_WORKSPACE,
                PAGE_PATH,
                PAGE_UUID,
                true);
    }
}
