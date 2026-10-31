package com.tbscg.doosan.magnolia.product.catalog.extimport.gpim;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.tbscg.doosan.magnolia.product.catalog.extimport.PimModel;
import lombok.Builder;
import lombok.Data;
import lombok.extern.jackson.Jacksonized;
import org.apache.commons.collections4.CollectionUtils;

import java.util.List;

/**
 * GPIM implementation for {@link PimModel}
 */
@Data
@Builder(toBuilder = true)
@Jacksonized
@JsonIgnoreProperties(ignoreUnknown = true)
public class GpimProduct implements PimModel {

    public static final String ACTIVE_LIFECYCLE_STATUS = "ACTIVE";
    public static final String PUBLIC_VISIBILITY = "PUBLIC";

    @JsonProperty("@code")
    private String code;

    @JsonProperty("$")
    private String dollarName;

    @JsonProperty("name")
    private String name;

    @JsonProperty("shortName")
    private String shortName;

    private String lifecycleStatus;

    private String visibility;

    private String description;

    @JsonProperty("yearOfProductionFrom")
    private String yearOfProduction;

    @JsonProperty("yearOfProductionTo")
    private String yearOfProductionEnd;

    @JsonProperty("itemtypeCode")
    private String itemtypeCode;

    @JsonProperty("variationsLinks")
    private List<GpimProduct> variants;

    @JsonProperty("compatibleCarriers")
    private List<GpimProduct> compatibleCarriers;

    /**
     * @return product status (current if is public and active, historical otherwise)
     */
    public String getLifecycle() {
        return isActivePublic() ? "Current" : "Historical";
    }

    /**
     * @return true if product is public and active
     */
    public boolean isActivePublic() {
        return ACTIVE_LIFECYCLE_STATUS.equals(getLifecycleStatus())
                && PUBLIC_VISIBILITY.equals(getVisibility());
    }

    /**
     * @return true if product has variants
     */
    public boolean hasVariants() {
        return CollectionUtils.isNotEmpty(variants);
    }

}
