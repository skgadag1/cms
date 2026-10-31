package com.tbscg.doosan.magnolia.mediatracker.data.provider;

import com.tbscg.doosan.magnolia.mediatracker.data.model.VanityDomain;
import com.tbscg.doosan.magnolia.mediatracker.data.model.VanityUrl;
import com.tbscg.doosan.magnolia.mediatracker.data.provider.elasticsearch.ElasticsearchService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class VanityUrlServiceTest {

    @InjectMocks
    private UrlServiceImpl urlService;

    @Mock
    private ElasticsearchService elasticsearchService;

    @Test
    public void shouldReturnFalseWhenUrlIsFound() {
        VanityUrl foundUrl = VanityUrl.builder()
                .code("code")
                .path("path")
                .build();

        when(elasticsearchService.getUrlByPath("/path", "code", true)).thenReturn(foundUrl);

        VanityDomain domain = new VanityDomain();
        domain.setFullDomainName("domain");
        domain.setCode("code");

        VanityUrl urlToCheck = VanityUrl.builder()
                .code("code1")
                .path("/path")
                .vanityDomain(domain)
                .build();

        boolean result = urlService.isUrlPathNotTaken(urlToCheck);

        assertFalse(result);
    }

    @Test
    public void shouldReturnTrueWhenUrlIsNotFound() {
        //when(elasticsearchService.getUrlByPath("/path", null)).thenReturn(null);

        VanityUrl urlToCheck = VanityUrl.builder()
                .code("code1")
                .path("/path")
                .build();

        boolean result = urlService.isUrlPathNotTaken(urlToCheck);

        assertTrue(result);
    }

    @Test
    public void shouldReturnTrueWhenTheSameUrlIsFound() {
        VanityUrl foundUrl = VanityUrl.builder()
                .code("code")
                .path("path")
                .build();

        //when(elasticsearchService.getUrlByPath("/path", "code")).thenReturn(foundUrl);

        VanityDomain domain = new VanityDomain();
        domain.setFullDomainName("domain");
        domain.setCode("code");

        VanityUrl urlToCheck = VanityUrl.builder()
                .code("code")
                .path("/path")
                .vanityDomain(domain)
                .build();

        boolean result = urlService.isUrlPathNotTaken(urlToCheck);

        assertTrue(result);
    }
}