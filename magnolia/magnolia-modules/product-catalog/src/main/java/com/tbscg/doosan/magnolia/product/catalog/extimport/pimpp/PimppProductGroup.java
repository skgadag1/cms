package com.tbscg.doosan.magnolia.product.catalog.extimport.pimpp;

import com.tbscg.doosan.magnolia.product.catalog.extimport.PimGroup;
import lombok.Builder;
import lombok.Data;

import java.util.List;

/**
 * Pimpp implementation for {@link PimGroup}
 */
@Data
@Builder
public class PimppProductGroup implements PimGroup {

    private String groupName;

    private List<PimppProduct> products;

}
