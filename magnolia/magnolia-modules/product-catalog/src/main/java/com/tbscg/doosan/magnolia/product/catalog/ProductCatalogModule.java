package com.tbscg.doosan.magnolia.product.catalog;

import com.tbscg.doosan.magnolia.product.catalog.extimport.buildandquote.BuildAndQuoteConfig;
import com.tbscg.doosan.magnolia.product.catalog.extimport.gpim.GpimConfig;
import com.tbscg.doosan.magnolia.product.catalog.extimport.pimpp.PimppConfig;
import com.tbscg.doosan.magnolia.product.catalog.extimport.pimpp.pdd.PddPimppConfig;
import info.magnolia.module.ModuleLifecycle;
import info.magnolia.module.ModuleLifecycleContext;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

/**
 * Product Catalog Module main class that holds its configuration.
 */
@Slf4j
@Getter
@Setter
public class ProductCatalogModule implements ModuleLifecycle {

    private GpimConfig gpimConfig;
    private PimppConfig pimppConfig;
    private PddPimppConfig pddPimppConfig;
    private BuildAndQuoteConfig buildAndQuoteConfig;

    @Override
    public void start(ModuleLifecycleContext moduleLifecycleContext) {
        log.debug("START productCatalog module. Config is : GPIM: {}, PIM++: {}, PDD PIM++: {}, BuildAndQuote: {}",
                gpimConfig, pimppConfig, pddPimppConfig, buildAndQuoteConfig);
    }

    @Override
    public void stop(ModuleLifecycleContext moduleLifecycleContext) {
        log.debug("STOP productCatalog module");
    }

}
