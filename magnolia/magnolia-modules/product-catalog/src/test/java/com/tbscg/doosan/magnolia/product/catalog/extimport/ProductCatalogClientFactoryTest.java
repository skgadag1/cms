package com.tbscg.doosan.magnolia.product.catalog.extimport;

import com.tbscg.doosan.magnolia.product.catalog.ProductCatalogModule;
import com.tbscg.doosan.magnolia.product.catalog.extimport.pimpp.DefaultPimppClient;
import com.tbscg.doosan.magnolia.product.catalog.extimport.pimpp.PimppClient;
import com.tbscg.doosan.magnolia.product.catalog.extimport.pimpp.PimppConfig;
import org.junit.Test;

import javax.inject.Provider;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class ProductCatalogClientFactoryTest {

    public static final String ENDPOINT = "endpoint";

    @Test
    public void createPimppClient() {
        // given
        final PimppConfig pcmPimppConfig = new PimppConfig();
        pcmPimppConfig.setEndpoint(ENDPOINT);

        ProductCatalogModule pcm = mock(ProductCatalogModule.class);
        when(pcm.getPimppConfig()).thenReturn(pcmPimppConfig);

        //noinspection unchecked
        Provider<ProductCatalogModule> productCatalogModuleProvider = mock(Provider.class);
        when(productCatalogModuleProvider.get()).thenReturn(pcm);

        // when
        final PimppClient pimppClient = new ProductCatalogClientFactory(productCatalogModuleProvider).createPimppClient();

        // then
        final PimppConfig expectedPimppConfig = new PimppConfig();
        expectedPimppConfig.setEndpoint(ENDPOINT);

        assertThat(pimppClient, notNullValue());
        assertThat(pimppClient, instanceOf(DefaultPimppClient.class));
        assertThat(((DefaultPimppClient) pimppClient).getPimppConfig(), equalTo(expectedPimppConfig));

    }

}
