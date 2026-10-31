package com.tbscg.doosan.magnolia.mediatracker.app.urllist;

import com.tbscg.doosan.magnolia.mediatracker.app.BaseMediaTrackerSubAppTest;
import com.tbscg.doosan.magnolia.mediatracker.app.urlList.UrlListSubApp;
import com.tbscg.doosan.magnolia.mediatracker.app.urlList.UrlListSubAppView;
import com.tbscg.doosan.magnolia.mediatracker.data.model.Status;
import com.tbscg.doosan.magnolia.mediatracker.data.model.VanityDomain;
import com.tbscg.doosan.magnolia.mediatracker.data.model.VanityUrl;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.time.OffsetDateTime;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class UrlListSubAppTest extends BaseMediaTrackerSubAppTest {

    @Mock
    private UrlListSubAppView subAppView;

    @InjectMocks
    private UrlListSubApp urlListSubApp;

    @Before
    public void setUp() {
        super.setUp();
        when(urlService.getUrl(any())).thenReturn(new VanityUrl());
    }

    @Test
    public void shouldSetUrlStatusToApproved() {
        urlListSubApp.setUrlStatus(url6, Status.APPROVED, new VanityDomain());

        verify(urlService).createNewOrReplace(urlCaptor.capture());
        VanityUrl capturedUrl = urlCaptor.getValue();

        verify(urlService, times(1)).createNewOrReplace(capturedUrl);
        assertTrue(capturedUrl != null);
        assertEquals(Status.APPROVED, capturedUrl.getStatus());
        assertEquals(castDateToDays(OffsetDateTime.now()), castDateToDays(capturedUrl.getStartDate()));
        assertEquals(null, capturedUrl.getEndDate());
        assertEquals("testUser", capturedUrl.getModifiedBy());
    }

    @Test
    public void shouldSetUrlStatusToArchived() {
        urlListSubApp.setUrlStatus(url6, Status.ARCHIVED, new VanityDomain());

        verify(urlService).createNewOrReplace(urlCaptor.capture());
        VanityUrl capturedUrl = urlCaptor.getValue();

        assertTrue(capturedUrl != null);
        assertEquals(Status.ARCHIVED, capturedUrl.getStatus());
        assertEquals(castDateToDays(OffsetDateTime.now()), castDateToDays(capturedUrl.getStartDate()));
        assertEquals(castDateToDays(OffsetDateTime.now()), castDateToDays(capturedUrl.getEndDate()));
    }

    @Test
    public void shouldCopyUrlDatesWhenPublished() {
        OffsetDateTime start = url2.getStartDate();
        OffsetDateTime end = url2.getEndDate();

        urlListSubApp.setUrlStatus(url2, Status.APPROVED, new VanityDomain());

        verify(urlService).createNewOrReplace(urlCaptor.capture());
        VanityUrl capturedUrl = urlCaptor.getValue();

        assertTrue(capturedUrl != null);
        assertEquals(Status.APPROVED, capturedUrl.getStatus());
        assertEquals(start, capturedUrl.getStartDate());
        assertEquals(end, capturedUrl.getEndDate());
    }

    @Test
    public void shouldCopyUrlDatesWhenArchived() {
        OffsetDateTime start = url3.getStartDate();
        OffsetDateTime end = url3.getEndDate();

        urlListSubApp.setUrlStatus(url3, Status.ARCHIVED, new VanityDomain());

        verify(urlService).createNewOrReplace(urlCaptor.capture());
        VanityUrl capturedUrl = urlCaptor.getValue();

        assertTrue(capturedUrl != null);
        assertEquals(Status.ARCHIVED, capturedUrl.getStatus());
        assertEquals(start, capturedUrl.getStartDate());
        assertEquals(end, capturedUrl.getEndDate());
    }

    public void shouldChangeUrlDatesWhenArchived() {
        urlListSubApp.setUrlStatus(url2, Status.ARCHIVED, new VanityDomain());

        verify(urlService).createNewOrReplace(urlCaptor.capture());
        VanityUrl capturedUrl = urlCaptor.getValue();

        assertTrue(capturedUrl != null);
        assertEquals(Status.ARCHIVED, capturedUrl.getStatus());
        assertEquals(castDateToDays(OffsetDateTime.now()), castDateToDays(capturedUrl.getStartDate()));
        assertEquals(castDateToDays(OffsetDateTime.now()), castDateToDays(capturedUrl.getEndDate()));
    }

    public void shouldChangeUrlEndDateWhenArchived() {
        OffsetDateTime start = url1.getStartDate();

        urlListSubApp.setUrlStatus(url1, Status.ARCHIVED, new VanityDomain());

        verify(urlService).createNewOrReplace(urlCaptor.capture());
        VanityUrl capturedUrl = urlCaptor.getValue();

        assertTrue(capturedUrl != null);
        assertEquals(Status.ARCHIVED, capturedUrl.getStatus());
        assertEquals(start, capturedUrl.getStartDate());
        assertEquals(castDateToDays(OffsetDateTime.now()), castDateToDays(capturedUrl.getEndDate()));
    }
}