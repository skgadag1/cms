package com.tbscg.doosan.magnolia.mediatracker.data.provider.elasticsearch;

import com.tbscg.doosan.magnolia.mediatracker.MediatrackerModule;
import com.tbscg.doosan.magnolia.mediatracker.data.model.VanityDomain;
import com.tbscg.doosan.magnolia.mediatracker.data.model.VanityUrl;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.junit.Assert.*;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class ElasticsearchServiceTest {

    @InjectMocks
    private ElasticsearchService elasticsearchService;

    @Mock
    private ElasticsearchHttpService httpService;

    @Mock
    private MediatrackerModule mediatrackerModule;

    @Captor
    private ArgumentCaptor<String> jsonCaptor;

    private final String urlQuery = "{\"size\":10000,\"query\":{\"bool\":{\"must\":[{\"wildcard\":{\"path.keyword\":{\"case_insensitive\":true,\"value\":\"*/path*\"}}},{\"match\":{\"vanityDomain\":{\"query\":\"domain\",\"operator\":\"and\"}}}]}}}";
    private final String domainQuery = "{\"size\":10000,\"query\":{\"bool\":{\"must\":[{\"match\":{\"fullDomainName\":{\"query\":\"www.domain.com\",\"operator\":\"and\"}}}]}}}";

    @Before
    public void setUp() {
        MediatrackerModule.ElasticsearchConfig config = mediatrackerModule.new ElasticsearchConfig();
        config.setUrlsTable("VanityUrls");
        config.setDomainsTable("VanityDomains");
        when(mediatrackerModule.getElasticsearchConfig()).thenReturn(mediatrackerModule.new ElasticsearchConfig());
        when(httpService.getDocument(anyString())).thenReturn("");
    }

    //----------------URL-------------------------//

    @Test
    public void shouldReturnNullWhenUrlPathIsNull() {
        VanityUrl url = elasticsearchService.getUrlByPath(null, "domain");

        assertNull(url);
    }

    @Test
    public void shouldReturnNullWhenUrlPathIsEmpty() {
        VanityUrl url = elasticsearchService.getUrlByPath("", "domain");

        assertNull(url);
    }

    @Test
    public void shouldCallElasticSearchFotUrlWithRightQuery() {
        elasticsearchService.getUrlByPath("/path", "domain");

        verify(httpService).getDocument(jsonCaptor.capture());
        String query = jsonCaptor.getValue();
        assertEquals(urlQuery, query);
    }

    //----------------DOMAIN-------------------------//

    @Test
    public void shouldReturnNullWhenDomainNameIsNull() {
        VanityDomain domain = elasticsearchService.getDomainByDomainName(null);

        assertNull(domain);
    }

    @Test
    public void shouldReturnNullWhenDomainNameIsEmpty() {
        VanityDomain domain = elasticsearchService.getDomainByDomainName("");

        assertNull(domain);
    }

    @Test
    public void shouldCallElasticSearchForDomainWithRightQuery() {
        elasticsearchService.getDomainByDomainName("www.domain.com");

        verify(httpService).getDocument(jsonCaptor.capture());
        String query = jsonCaptor.getValue();
        assertEquals(domainQuery, query);
    }
}
