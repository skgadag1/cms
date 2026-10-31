package com.tbscg.doosan.magnolia.dxp.workflow.action.ui.processors;

import javax.jcr.RepositoryException;

public class DxpActionProcessorException extends RuntimeException {
    public DxpActionProcessorException(final String message, RepositoryException cause) {
        super(message, cause);
    }
}
