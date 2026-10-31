package com.tbscg.doosan.magnolia.product.catalog.extimport.pimpp;

import com.tbscg.doosan.magnolia.product.catalog.extimport.PimModel;
import com.tbscg.doosan.magnolia.product.catalog.extimport.gpim.GpimProduct;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class PimppProduct implements PimModel {

    private String name;
    private String status;
    private String code;

    @Override
    public String getYearOfProduction() {
        return null;
    }

    @Override
    public String getYearOfProductionEnd() {
        return null;
    }

    @Override
    public List<GpimProduct> getCompatibleCarriers() {
        return null;
    }

    @Override
    public String getDescription() {
        return "";
    }

    /**
     * @return product status (current if is public and active, historical otherwise)
     */
    public String getLifecycle() {
        return ("current".equalsIgnoreCase(this.status) || "ACTIVE".equals(this.status)) ? "Current" : "Historical";
    }
}
