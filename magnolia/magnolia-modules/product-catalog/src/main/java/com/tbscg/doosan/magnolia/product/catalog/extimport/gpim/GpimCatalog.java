package com.tbscg.doosan.magnolia.product.catalog.extimport.gpim;

import com.tbscg.doosan.magnolia.product.catalog.extimport.PimCatalog;
import lombok.Builder;
import lombok.Data;

import java.util.Collection;

/**
 * {@link PimCatalog} implementation for GPIM. Holds list categories.
 */
@Data
@Builder
public class GpimCatalog implements PimCatalog {

    private final Collection<GpimCategory> categories;
}
