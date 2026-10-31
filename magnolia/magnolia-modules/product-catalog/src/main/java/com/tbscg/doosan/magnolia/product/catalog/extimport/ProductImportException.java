package com.tbscg.doosan.magnolia.product.catalog.extimport;

public class ProductImportException extends RuntimeException {

    public ProductImportException(String message) {
        super(message);
    }

    public ProductImportException(String message, Throwable cause) {
        super(message, cause);
    }

}
