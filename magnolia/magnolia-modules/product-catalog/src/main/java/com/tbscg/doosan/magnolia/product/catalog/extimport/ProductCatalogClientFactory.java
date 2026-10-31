package com.tbscg.doosan.magnolia.product.catalog.extimport;

import com.tbscg.doosan.magnolia.product.catalog.ProductCatalogModule;
import com.tbscg.doosan.magnolia.product.catalog.extimport.pimpp.DefaultPimppClient;
import com.tbscg.doosan.magnolia.product.catalog.extimport.pimpp.PimppClient;
import com.tbscg.doosan.magnolia.product.catalog.extimport.pimpp.PimppClientFactory;
import com.tbscg.doosan.magnolia.product.catalog.extimport.pimpp.pdd.PddPimppClient;
import com.tbscg.doosan.magnolia.product.catalog.extimport.pimpp.pdd.PddPimppConfig;
import lombok.AllArgsConstructor;

import javax.inject.Provider;

/**
 * {@link PimppClientFactory} implementation for {@link com.tbscg.doosan.magnolia.product.catalog.ProductCatalogModule}.
 * Will fetch client configuration from this module configuration.
 */
@AllArgsConstructor
public class ProductCatalogClientFactory implements PimppClientFactory {

    private final Provider<ProductCatalogModule> productCatalogModuleProvider;

    @Override
    public PimppClient createPimppClient() {
        final PddPimppConfig config = productCatalogModuleProvider.get().getPddPimppConfig();

        if (config != null && config.isEnabled()) {
            return new PddPimppClient(config);
        }

        return new DefaultPimppClient(productCatalogModuleProvider.get().getPimppConfig());
    }
}
