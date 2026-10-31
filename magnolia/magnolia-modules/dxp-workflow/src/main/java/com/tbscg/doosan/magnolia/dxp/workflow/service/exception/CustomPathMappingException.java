package com.tbscg.doosan.magnolia.dxp.workflow.service.exception;

public class CustomPathMappingException extends RuntimeException {

    public CustomPathMappingException(final String message) {
        super(message);
    }

    public CustomPathMappingException(final String message, final Throwable cause) {
        super(message, cause);
    }

}
