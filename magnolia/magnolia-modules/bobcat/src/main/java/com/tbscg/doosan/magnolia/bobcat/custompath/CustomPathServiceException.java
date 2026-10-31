package com.tbscg.doosan.magnolia.bobcat.custompath;

public class CustomPathServiceException extends RuntimeException {
    public CustomPathServiceException(final String message) {
        super(message);
    }

    public CustomPathServiceException(String message, Throwable cause) {
        super(message, cause);
    }
}
