package com.tbscg.doosan.magnolia.dxp.core.setup.placeholder;

/**
 * Exception thrown in run-time when a problem occurs when retrieving placeholders from YAML files.
 */
public class PlaceholderRetrievingException extends RuntimeException {

    public PlaceholderRetrievingException() {
        super();
    }

    public PlaceholderRetrievingException(String message) {
        super(message);
    }

    public PlaceholderRetrievingException(String message, Throwable cause) {
        super(message, cause);
    }

    public PlaceholderRetrievingException(Throwable cause) {
        super(cause);
    }

    protected PlaceholderRetrievingException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
