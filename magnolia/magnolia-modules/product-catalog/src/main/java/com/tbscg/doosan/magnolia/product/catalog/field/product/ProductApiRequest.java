package com.tbscg.doosan.magnolia.product.catalog.field.product;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ProductApiRequest {
    private String productId;
    private String businessUnit;
    private String locale;
    private Integer specLevel;
    private String modelType;
    private String region;
    private String productDetailsDataSource;
    private String instance;
} 