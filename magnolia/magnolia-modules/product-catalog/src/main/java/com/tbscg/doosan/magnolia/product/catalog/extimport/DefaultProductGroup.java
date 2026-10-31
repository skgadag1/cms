package com.tbscg.doosan.magnolia.product.catalog.extimport;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class DefaultProductGroup implements PimGroup {

    private String groupName;

    private List<? extends PimModel> products;

}
