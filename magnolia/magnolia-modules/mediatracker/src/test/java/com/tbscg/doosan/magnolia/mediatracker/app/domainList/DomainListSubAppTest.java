package com.tbscg.doosan.magnolia.mediatracker.app.domainList;

import com.tbscg.doosan.magnolia.mediatracker.app.BaseMediaTrackerSubAppTest;
import com.tbscg.doosan.magnolia.mediatracker.data.model.Status;
import com.tbscg.doosan.magnolia.mediatracker.data.model.VanityDomain;
import com.tbscg.doosan.magnolia.mediatracker.data.model.VanityUrl;
import com.tbscg.doosan.magnolia.mediatracker.data.provider.VanityDomainService;
import info.magnolia.ui.api.shell.ConfirmationHandler;
import org.apache.commons.lang3.mutable.MutableBoolean;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.mockito.stubbing.Answer;

import java.time.OffsetDateTime;
import java.util.Arrays;
import java.util.Collections;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.anyObject;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class DomainListSubAppTest extends BaseMediaTrackerSubAppTest {

    @Mock
    private VanityDomainService domainService;

    @Mock
    private DomainListSubAppView subAppView;

    @InjectMocks
    private DomainListSubApp domainListSubApp;

    @Before
    public void setUp() {
        super.setUp();
        when(domainService.getDomain(any())).thenReturn(new VanityDomain());
        doNothing().when(domainService).saveDomain(anyObject());
        mockConfirming();
    }

    private void mockConfirming() {
        final MutableBoolean shellCalledToConfirm = new MutableBoolean(false);
        doAnswer((Answer<Void>) invocation -> {
            shellCalledToConfirm.setValue(true);
            ConfirmationHandler o = (ConfirmationHandler) invocation.getArguments()[1];
            o.onConfirm();
            return null;
        }).when(shell).askForConfirmation(any(), any(ConfirmationHandler.class));
    }

    @Test
    public void shouldUpdateAssociatedUrlsWhenArchiving() {

        when(urlService.getUrlsByDomainCode(any())).thenReturn(Arrays.asList(url1, url2, url3, url4, url5, url6));

        domainListSubApp.setDomainStatus(new VanityDomain(), Status.ARCHIVED);

        verify(urlService, times(1)).getUrlsByDomainCode(any());
        verify(urlService, times(4)).createNewOrReplace(anyObject());
        verify(domainService, times(1)).getDomain(any());
        verify(domainService, times(1)).saveDomain(anyObject());
    }

    @Test
    public void shouldCopyUrlDatesWhenArchived() {
        OffsetDateTime start = url3.getStartDate();
        OffsetDateTime end = url3.getEndDate();
        when(urlService.getUrlsByDomainCode(any())).thenReturn(Collections.singletonList(url3));

        domainListSubApp.setDomainStatus(new VanityDomain(), Status.ARCHIVED);

        verify(urlService).createNewOrReplace(urlCaptor.capture());
        VanityUrl capturedUrl = urlCaptor.getValue();

        assertTrue(capturedUrl != null);
        assertEquals(Status.ARCHIVED, capturedUrl.getStatus());
        assertEquals(start, capturedUrl.getStartDate());
        assertEquals(end, capturedUrl.getEndDate());
    }

    public void shouldChangeUrlDatesWhenArchived() {
        when(urlService.getUrlsByDomainCode(any())).thenReturn(Collections.singletonList(url2));

        domainListSubApp.setDomainStatus(new VanityDomain(), Status.ARCHIVED);

        verify(urlService).createNewOrReplace(urlCaptor.capture());
        VanityUrl capturedUrl = urlCaptor.getValue();

        assertTrue(capturedUrl != null);
        assertEquals(Status.ARCHIVED, capturedUrl.getStatus());
        assertEquals(castDateToDays(OffsetDateTime.now()), castDateToDays(capturedUrl.getStartDate()));
        assertEquals(castDateToDays(OffsetDateTime.now()), castDateToDays(capturedUrl.getEndDate()));
    }

    public void shouldChangeUrlEndDateWhenArchived() {
        OffsetDateTime start = url1.getStartDate();
        when(urlService.getUrlsByDomainCode(any())).thenReturn(Collections.singletonList(url1));

        domainListSubApp.setDomainStatus(new VanityDomain(), Status.ARCHIVED);

        verify(urlService).createNewOrReplace(urlCaptor.capture());
        VanityUrl capturedUrl = urlCaptor.getValue();

        assertTrue(capturedUrl != null);
        assertEquals(Status.ARCHIVED, capturedUrl.getStatus());
        assertEquals(start, capturedUrl.getStartDate());
        assertEquals(castDateToDays(OffsetDateTime.now()), castDateToDays(capturedUrl.getEndDate()));
    }

    public void shouldSetUrlDatesWhenArchived() {
        when(urlService.getUrlsByDomainCode(any())).thenReturn(Collections.singletonList(url6));

        domainListSubApp.setDomainStatus(new VanityDomain(), Status.ARCHIVED);

        verify(urlService).createNewOrReplace(urlCaptor.capture());
        VanityUrl capturedUrl = urlCaptor.getValue();

        assertTrue(capturedUrl != null);
        assertEquals(Status.ARCHIVED, capturedUrl.getStatus());
        assertEquals(castDateToDays(OffsetDateTime.now()), castDateToDays(capturedUrl.getStartDate()));
        assertEquals(castDateToDays(OffsetDateTime.now()), castDateToDays(capturedUrl.getEndDate()));
    }

    @Test
    public void shouldntUpdateAssociatedUrlsWhenNotArchiving() {
        domainListSubApp.setDomainStatus(new VanityDomain(), Status.APPROVED);

        verify(urlService, never()).getUrlsByDomainCode(any());
        verify(urlService, never()).createNewOrReplace(anyObject());
        verify(domainService, times(1)).getDomain(any());
        verify(domainService, times(1)).saveDomain(anyObject());
    }

    @Test
    public void shouldntUpdateWhenNoAssociatedUrl() {
        when(urlService.getUrlsByDomainCode(any())).thenReturn(Collections.EMPTY_LIST);
        domainListSubApp.setDomainStatus(new VanityDomain(), Status.ARCHIVED);

        verify(urlService, never()).createNewOrReplace(anyObject());
        verify(urlService, times(1)).getUrlsByDomainCode(any());
        verify(domainService, times(1)).getDomain(any());
        verify(domainService, times(1)).saveDomain(anyObject());
    }
}