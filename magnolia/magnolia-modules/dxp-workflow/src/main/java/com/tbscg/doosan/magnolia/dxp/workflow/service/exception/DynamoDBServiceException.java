package com.tbscg.doosan.magnolia.dxp.workflow.service.exception;

public class DynamoDBServiceException extends Exception {
    public DynamoDBServiceException() {
    }

    public DynamoDBServiceException(String message) {
        super(message);
    }

    public DynamoDBServiceException(String message, Throwable cause) {
        super(message, cause);
    }

    public DynamoDBServiceException(Throwable cause) {
        super(cause);
    }
}
