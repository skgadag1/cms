package com.tbscg.doosan.magnolia.mediatracker.data.provider;

import com.tbscg.doosan.magnolia.mediatracker.data.model.VanityDomain;
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
public class VanityDomainServiceTest {

    @InjectMocks
    private DomainServiceImpl domainService;

    @Mock
    private ElasticsearchService elasticsearchService;

    @Test
    public void shouldReturnFalseWhenDomainIsFound() {
        VanityDomain foundDomain = new VanityDomain();
        foundDomain.setCode("code1");
        foundDomain.setFullDomainName("domain");

        when(elasticsearchService.getDomainByDomainName("domain")).thenReturn(foundDomain);

        VanityDomain domainToCheck = new VanityDomain();
        domainToCheck.setCode("code");
        domainToCheck.setFullDomainName("domain");

        boolean result = domainService.isDomainNameNotTaken(domainToCheck);

        assertFalse(result);
    }

    @Test
    public void shouldReturnTrueWhenUrlIsNotFound() {
        when(elasticsearchService.getDomainByDomainName("domain")).thenReturn(null);

        VanityDomain domainToCheck = new VanityDomain();
        domainToCheck.setCode("code");
        domainToCheck.setFullDomainName("domain");

        boolean result = domainService.isDomainNameNotTaken(domainToCheck);

        assertTrue(result);
    }

    @Test
    public void shouldReturnTrueWhenTheSameUrlIsFound() {
        VanityDomain foundDomain = new VanityDomain();
        foundDomain.setCode("code");
        foundDomain.setFullDomainName("domain");

        when(elasticsearchService.getDomainByDomainName("domain")).thenReturn(foundDomain);

        VanityDomain domainToCheck = new VanityDomain();
        domainToCheck.setCode("code");
        domainToCheck.setFullDomainName("domain");

        boolean result = domainService.isDomainNameNotTaken(domainToCheck);

        assertTrue(result);
    }
}