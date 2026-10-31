package com.tbscg.doosan.magnolia.dxp.workflow.service.eventhandling;

import com.tbscg.doosan.magnolia.dxp.workflow.service.exception.EventHandlingException;

public interface EventHandleable {

    boolean handleEvent(final String eventAction,
                        final String nodePath,
                        final String nodeUuid,
                        final boolean isRecursive,
                        final long publishTime,
                        final String workspace) throws EventHandlingException;
}
