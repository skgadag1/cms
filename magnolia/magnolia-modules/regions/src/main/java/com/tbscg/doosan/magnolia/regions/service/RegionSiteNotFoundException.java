package com.tbscg.doosan.magnolia.regions.service;

/**
 * Exception thrown when a specific {@link com.tbscg.doosan.magnolia.regions.model.RegionSite} is not
 * found by {@link RegionsService}.
 *
 * @author Alejandro Puig Escobar
 */
public class RegionSiteNotFoundException extends Exception {

    public RegionSiteNotFoundException() {
        super();
    }

    public RegionSiteNotFoundException(String message) {
        super(message);
    }

    public RegionSiteNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }

    public RegionSiteNotFoundException(Throwable cause) {
        super(cause);
    }

    protected RegionSiteNotFoundException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
