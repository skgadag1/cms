package com.tbscg.doosan.magnolia.dxp.core.converter;

/**
 * Exception thrown in runtime when an error occurs during {@link BeanConverter} execution.
 *
 * @author Alejandro Puig Escobar
 */
public class BeanConversionException extends RuntimeException {

    public BeanConversionException() {
        super();
    }

    public BeanConversionException(String message) {
        super(message);
    }

    public BeanConversionException(String message, Throwable cause) {
        super(message, cause);
    }

    public BeanConversionException(Throwable cause) {
        super(cause);
    }

    protected BeanConversionException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
