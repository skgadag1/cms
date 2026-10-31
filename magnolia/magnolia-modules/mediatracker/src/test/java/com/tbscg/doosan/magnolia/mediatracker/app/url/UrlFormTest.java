package com.tbscg.doosan.magnolia.mediatracker.app.url;

import com.tbscg.doosan.magnolia.mediatracker.data.model.VanityDomain;
import com.tbscg.doosan.magnolia.mediatracker.data.model.VanityUrl;
import com.tbscg.doosan.magnolia.mediatracker.error.HtmlFormattedMessageException;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.util.Collections;
import java.util.List;

import static org.hamcrest.Matchers.*;

public class UrlFormTest {

    @Rule
    public final ExpectedException expectedException = ExpectedException.none();

    @Test
    public void ctxShouldThrowOnNoDomains() {
        // given
        String anyTitle = "Title";
        VanityUrl anyUrl = new VanityUrl();
        boolean anyShowRightPanel = false;

        List<VanityDomain> domains = Collections.emptyList();

        // expect
        expectedException.expect(HtmlFormattedMessageException.class);
        expectedException.expectMessage(not(emptyOrNullString()));

        // when
        new UrlForm(anyTitle, anyUrl, domains, anyShowRightPanel);
    }

}
