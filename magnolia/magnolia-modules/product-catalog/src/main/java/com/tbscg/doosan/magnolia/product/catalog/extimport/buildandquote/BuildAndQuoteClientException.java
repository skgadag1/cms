package com.tbscg.doosan.magnolia.product.catalog.extimport.buildandquote;

@SuppressWarnings("unused")
public class BuildAndQuoteClientException extends RuntimeException {

    public BuildAndQuoteClientException(final String message) {
        super(message);
    }

    public BuildAndQuoteClientException(final String message, final Throwable cause) {
        super(message, cause);
    }

}
