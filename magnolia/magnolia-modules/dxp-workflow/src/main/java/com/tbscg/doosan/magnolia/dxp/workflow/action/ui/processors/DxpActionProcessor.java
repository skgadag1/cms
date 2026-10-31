package com.tbscg.doosan.magnolia.dxp.workflow.action.ui.processors;

import info.magnolia.ui.editor.FormDefinition;

import javax.jcr.Node;

public interface DxpActionProcessor {
    void process(Node item, FormDefinition<?> formDefinition) throws DxpActionProcessorException;
}
