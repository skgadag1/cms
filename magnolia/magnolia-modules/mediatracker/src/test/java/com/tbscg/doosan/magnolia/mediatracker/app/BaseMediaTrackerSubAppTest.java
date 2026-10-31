package com.tbscg.doosan.magnolia.mediatracker.app;

import com.tbscg.doosan.magnolia.mediatracker.data.model.Status;
import com.tbscg.doosan.magnolia.mediatracker.data.model.VanityUrl;
import com.tbscg.doosan.magnolia.mediatracker.data.provider.VanityUrlService;
import info.magnolia.cms.security.User;
import info.magnolia.context.MgnlContext;
import info.magnolia.test.mock.MockContext;
import info.magnolia.ui.api.app.AppContext;
import info.magnolia.ui.api.app.SubAppContext;
import info.magnolia.ui.api.shell.Shell;
import org.junit.After;
import org.junit.Before;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;

import java.time.OffsetDateTime;
import java.time.temporal.ChronoUnit;

import static org.mockito.Matchers.anyObject;
import static org.mockito.Mockito.*;

public abstract class BaseMediaTrackerSubAppTest {

    @Mock
    protected Shell shell;

    @Mock
    protected VanityUrlService urlService;

    @Mock
    protected SubAppContext subAppContext;

    @Mock
    protected User user;

    @Mock
    protected AppContext appContext;

    @Captor
    protected ArgumentCaptor<VanityUrl> urlCaptor;

    @Before
    public void setUp() {
        mockContext();
        doNothing().when(urlService).createNewOrReplace(anyObject());
    }

    @After
    public void tearDown() {
        MgnlContext.setInstance(null);
    }

    private void mockContext() {
        MockContext ctx = mock(MockContext.class);
        when(user.getName()).thenReturn("testUser");
        when(ctx.getUser()).thenReturn(user);
        when(subAppContext.getAppContext()).thenReturn(appContext);
        MgnlContext.setInstance(ctx);
    }

    protected VanityUrl url1 = VanityUrl.builder()
            .startDate(OffsetDateTime.now().minusDays(1))
            .endDate(OffsetDateTime.now().plusDays(2))
            .status(Status.APPROVED)
            .build();
    protected VanityUrl url2 = VanityUrl.builder()
            .startDate(OffsetDateTime.now().plusDays(1))
            .endDate(OffsetDateTime.now().plusDays(2))
            .status(Status.APPROVED)
            .build();
    protected VanityUrl url3 = VanityUrl.builder()
            .startDate(OffsetDateTime.now().minusDays(2))
            .endDate(OffsetDateTime.now().minusDays(1))
            .status(Status.DRAFT)
            .build();
    protected VanityUrl url4 = VanityUrl.builder()
            .startDate(OffsetDateTime.now().minusDays(1))
            .endDate(OffsetDateTime.now().plusDays(2))
            .status(Status.ARCHIVED)
            .build();
    protected VanityUrl url5 = VanityUrl.builder()
            .startDate(OffsetDateTime.now().minusDays(1))
            .endDate(OffsetDateTime.now().plusDays(2))
            .status(Status.ARCHIVED)
            .build();
    protected VanityUrl url6 = VanityUrl.builder()
            .status(Status.DRAFT)
            .build();

    protected OffsetDateTime castDateToDays(OffsetDateTime now) {
        return now.truncatedTo(ChronoUnit.DAYS);
    }
}
