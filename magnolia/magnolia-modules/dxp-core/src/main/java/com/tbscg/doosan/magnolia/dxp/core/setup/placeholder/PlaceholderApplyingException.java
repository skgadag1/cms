package com.tbscg.doosan.magnolia.dxp.core.setup.placeholder;

/**
 * Exception thrown in run-time when a problem occurs when applying resolved placeholders.
 */
public class PlaceholderApplyingException extends RuntimeException {

    public PlaceholderApplyingException() {
        super();
    }

    public PlaceholderApplyingException(String message) {
        super(message);
    }

    public PlaceholderApplyingException(String message, Throwable cause) {
        super(message, cause);
    }

    public PlaceholderApplyingException(Throwable cause) {
        super(cause);
    }

    protected PlaceholderApplyingException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
