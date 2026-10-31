package com.tbscg.doosan.magnolia.product.catalog.extimport.gpim;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.tbscg.doosan.magnolia.product.catalog.extimport.PimGroup;
import lombok.Builder;
import lombok.Data;
import lombok.extern.jackson.Jacksonized;

import java.util.List;

/**
 * GPIM implementation for {@link PimGroup}
 */
@Data
@Builder
@Jacksonized
@JsonIgnoreProperties(ignoreUnknown = true)
public class GpimProductGroup implements PimGroup {

    public static final String ACTIVE_LIFECYCLE_STATUS = "ACTIVE";
    public static final String PUBLIC_VISIBILITY = "PUBLIC";

    private String groupName;

    private List<GpimProduct> products;

    private String lifecycleStatus;

    private String visibility;

    /**
     * @return true if product is historical
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
}
