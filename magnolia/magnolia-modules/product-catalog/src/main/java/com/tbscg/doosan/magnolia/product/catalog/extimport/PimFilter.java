package com.tbscg.doosan.magnolia.product.catalog.extimport;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class PimFilter {
    boolean includeHistorical;
    @lombok.NonNull
    PimModelType modelType;
}
