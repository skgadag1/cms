package com.tbscg.doosan.magnolia.product.catalog.extimport.pimpp;

/**
 * Exception class thrown by {@link PimppClient} implementations.
 */
@SuppressWarnings("unused")
public class PimppClientException extends Exception {
    public PimppClientException() {
    }

    public PimppClientException(final String message, final Throwable e) {
        super(message, e);
    }

    public PimppClientException(final Throwable cause) {
        super(cause);
    }

    public PimppClientException(final String message) {
        super(message);
    }
}
