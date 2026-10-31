package com.tbscg.doosan.magnolia.mediatracker.data.model.location;

import com.tbscg.doosan.magnolia.mediatracker.data.model.RequestParameter;
import info.magnolia.ui.api.location.DefaultLocation;

import java.util.Map;

/**
 * Location implementation. Extends the Default Location by adding additional parameters map.
 */
public class ExtendedLocation extends DefaultLocation {

    private Map<RequestParameter, Object> additionalParams;

    public ExtendedLocation(String appType, String appName, String subAppId, String parameter, Map<RequestParameter, Object> additionalParams) {
        super(appType, appName, subAppId, parameter);
        this.additionalParams = additionalParams;
    }

    public Map<RequestParameter, Object> getAdditionalParams() {
        return additionalParams;
    }
}
