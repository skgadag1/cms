package com.tbscg.doosan.magnolia.product.catalog.setup;

import com.tbscg.doosan.magnolia.dxp.core.setup.AutomaticDefaultDxpVersionHandler;
import info.magnolia.module.InstallContext;
import info.magnolia.module.delta.Task;

import javax.inject.Inject;
import java.util.Collections;
import java.util.List;

/**
 * Version Handler for Product Catalog Module.
 */
public class ProductCatalogVersionHandler extends AutomaticDefaultDxpVersionHandler {

    @Inject
    public ProductCatalogVersionHandler() {
        super();
    }

    @Override
    protected List<Task> getExtraInstallTasks(final InstallContext installContext) {
        return Collections.emptyList();
    }
}
