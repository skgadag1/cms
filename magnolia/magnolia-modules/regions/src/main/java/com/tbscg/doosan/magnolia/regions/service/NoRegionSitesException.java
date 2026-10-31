package com.tbscg.doosan.magnolia.regions.service;

/**
 * Exception thrown when no {@link com.tbscg.doosan.magnolia.regions.model.RegionSite} is found
 * by {@link RegionsService}.
 *
 * @author Alejandro Puig Escobar
 */
public class NoRegionSitesException extends Exception {

    public NoRegionSitesException() {
        super();
    }

    public NoRegionSitesException(String message) {
        super(message);
    }

    public NoRegionSitesException(String message, Throwable cause) {
        super(message, cause);
    }

    public NoRegionSitesException(Throwable cause) {
        super(cause);
    }

    protected NoRegionSitesException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
