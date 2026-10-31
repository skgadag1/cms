package com.tbscg.doosan.magnolia.regions.service;

/**
 * Exception thrown when no {@link info.magnolia.module.site.Site} is found
 * by {@link SiteService}.
 *
 * @author Alejandro Puig Escobar
 */
public class NoSitesException extends Exception {

    public NoSitesException() {
        super();
    }

    public NoSitesException(String message) {
        super(message);
    }

    public NoSitesException(String message, Throwable cause) {
        super(message, cause);
    }

    public NoSitesException(Throwable cause) {
        super(cause);
    }

    protected NoSitesException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
