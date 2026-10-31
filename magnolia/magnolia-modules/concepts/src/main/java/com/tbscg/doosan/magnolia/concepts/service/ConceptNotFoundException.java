package com.tbscg.doosan.magnolia.concepts.service;

/**
 * Exception thrown when a specific concept is not
 * found by {@link ConceptsService}.
 *
 * @author Alejandro Puig Escobar
 */
public class ConceptNotFoundException extends Exception {

    public ConceptNotFoundException() {
        super();
    }

    public ConceptNotFoundException(String message) {
        super(message);
    }

    public ConceptNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }

    public ConceptNotFoundException(Throwable cause) {
        super(cause);
    }

    protected ConceptNotFoundException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
