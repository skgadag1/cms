package com.tbscg.doosan.magnolia.dxp.workflow.service.exception;

/**
 * Exception thrown when there is an indexing error.
 */
public class IndexingException extends RuntimeException {
    public IndexingException(final String message) {
        super(message);
    }

    public IndexingException(final String message, final Throwable cause) {
        super(message, cause);
    }
}
