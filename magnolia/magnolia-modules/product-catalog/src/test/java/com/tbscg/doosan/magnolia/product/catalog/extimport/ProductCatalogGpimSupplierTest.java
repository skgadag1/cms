package com.tbscg.doosan.magnolia.product.catalog.extimport;

import com.tbscg.doosan.magnolia.product.catalog.ProductCatalogModule;
import com.tbscg.doosan.magnolia.product.catalog.extimport.gpim.DefaultGpimClient;
import com.tbscg.doosan.magnolia.product.catalog.extimport.gpim.GpimClient;
import com.tbscg.doosan.magnolia.product.catalog.extimport.gpim.GpimConfig;
import org.junit.Test;
import org.mockito.Mockito;

import javax.inject.Provider;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class ProductCatalogGpimSupplierTest {

    public static final String ENDPOINT = "endpoint";

    @Test
    public void get() {
        // given
        ProductCatalogModule pcm = mock(ProductCatalogModule.class);
        when(pcm.getGpimConfig()).thenReturn(mkGpimConfig(ENDPOINT));

        //noinspection unchecked
        Provider<ProductCatalogModule> productCatalogModuleProvider = Mockito.mock(Provider.class);
        when(productCatalogModuleProvider.get()).thenReturn(pcm);

        // when
        final GpimClient gpimClient = new ProductCatalogGpimFactory(productCatalogModuleProvider).createGpimClient();

        // then
        assertThat(gpimClient, notNullValue(GpimClient.class));
        assertThat(gpimClient, instanceOf(DefaultGpimClient.class));
        assertThat(((DefaultGpimClient) gpimClient).getGpimConfig(), equalTo(mkGpimConfig(ENDPOINT)));
    }

    private static GpimConfig mkGpimConfig(String endpoint) {
        final GpimConfig gpimConfig = new GpimConfig();
        gpimConfig.setEndpoint(endpoint);
        return gpimConfig;
    }

}
