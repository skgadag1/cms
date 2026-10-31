package com.tbscg.doosan.magnolia.regions.replication;

/**
 * Exception thrown in run-time when a problem occurs when replicating event actions.
 *
 * @author Alejandro Puig Escobar
 */
public class ReplicationException extends RuntimeException {

    public ReplicationException() {
        super();
    }

    public ReplicationException(String message) {
        super(message);
    }

    public ReplicationException(String message, Throwable cause) {
        super(message, cause);
    }

    public ReplicationException(Throwable cause) {
        super(cause);
    }

    protected ReplicationException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
