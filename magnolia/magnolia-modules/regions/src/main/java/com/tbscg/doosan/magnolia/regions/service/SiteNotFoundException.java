package com.tbscg.doosan.magnolia.regions.service;

/**
 * Exception thrown when no {@link info.magnolia.module.site.Site} is found
 * by {@link SiteService}.
 *
 * @author Alejandro Puig Escobar
 */
public class SiteNotFoundException extends Exception {

    public SiteNotFoundException() {
        super();
    }

    public SiteNotFoundException(String message) {
        super(message);
    }

    public SiteNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }

    public SiteNotFoundException(Throwable cause) {
        super(cause);
    }

    protected SiteNotFoundException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
