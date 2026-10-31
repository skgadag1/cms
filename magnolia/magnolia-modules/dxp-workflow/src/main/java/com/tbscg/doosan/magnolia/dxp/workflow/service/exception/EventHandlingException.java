package com.tbscg.doosan.magnolia.dxp.workflow.service.exception;

/**
 * Exception thrown when there is an indexing error.
 */
public class EventHandlingException extends RuntimeException {
    public EventHandlingException(final String message) {
        super(message);
    }

    public EventHandlingException(final String message, final Throwable cause) {
        super(message, cause);
    }
}
