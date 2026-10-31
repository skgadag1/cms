package com.tbscg.doosan.magnolia.dxp.core.action.ui;

public class UnprocessedEventException extends RuntimeException {

    public UnprocessedEventException(String message) {
        super(message);
    }

    public UnprocessedEventException(String message, Throwable cause) {
        super(message, cause);
    }
}
