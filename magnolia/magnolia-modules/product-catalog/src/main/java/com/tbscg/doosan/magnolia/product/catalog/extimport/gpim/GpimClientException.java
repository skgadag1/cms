package com.tbscg.doosan.magnolia.product.catalog.extimport.gpim;

/**
 * Exception of GPIM client.
 */
public class GpimClientException extends Exception {
    public GpimClientException() {
    }

    public GpimClientException(final String message) {
        super(message);
    }

    public GpimClientException(final String message, final Throwable cause) {
        super(message, cause);
    }

    public GpimClientException(final Throwable cause) {
        super(cause);
    }
}
