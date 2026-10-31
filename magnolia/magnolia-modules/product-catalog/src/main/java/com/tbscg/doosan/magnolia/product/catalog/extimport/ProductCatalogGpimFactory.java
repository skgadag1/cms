package com.tbscg.doosan.magnolia.product.catalog.extimport;

import com.tbscg.doosan.magnolia.product.catalog.ProductCatalogModule;
import com.tbscg.doosan.magnolia.product.catalog.extimport.gpim.DefaultGpimClient;
import com.tbscg.doosan.magnolia.product.catalog.extimport.gpim.GpimClient;
import com.tbscg.doosan.magnolia.product.catalog.extimport.gpim.GpimClientFactory;
import com.tbscg.doosan.magnolia.product.catalog.extimport.gpim.GpimConfig;

import javax.inject.Inject;
import javax.inject.Provider;

/**
 * {@link GpimClient} factory that uses configuration provided by {@link ProductCatalogModule}
 */
public class ProductCatalogGpimFactory implements GpimClientFactory {

    private final GpimConfig gpimConfig;

    /**
     * Creates new instance based on {@link ProductCatalogModule} provider
     *
     * @param productCatalogModuleProvider {@link ProductCatalogModule} provider instance
     */
    @Inject
    public ProductCatalogGpimFactory(final Provider<ProductCatalogModule> productCatalogModuleProvider) {
        gpimConfig = productCatalogModuleProvider.get().getGpimConfig();
    }

    @Override
    public GpimClient createGpimClient() {
        return new DefaultGpimClient(gpimConfig);
    }
}
