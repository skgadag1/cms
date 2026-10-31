package com.tbscg.doosan.magnolia.regions.service;

/**
 * Exception thrown when a specific {@link com.tbscg.doosan.magnolia.regions.model.Region} is not
 * found by {@link RegionsService}.
 *
 * @author Alejandro Puig Escobar
 */
public class RegionNotFoundException extends Exception {

    public RegionNotFoundException() {
        super();
    }

    public RegionNotFoundException(String message) {
        super(message);
    }

    public RegionNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }

    public RegionNotFoundException(Throwable cause) {
        super(cause);
    }

    protected RegionNotFoundException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
