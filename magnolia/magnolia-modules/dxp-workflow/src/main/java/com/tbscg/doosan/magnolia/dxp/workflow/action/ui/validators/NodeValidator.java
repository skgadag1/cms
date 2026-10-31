package com.tbscg.doosan.magnolia.dxp.workflow.action.ui.validators;

import info.magnolia.ui.datasource.jcr.JcrNodeWrapper;

import java.util.stream.Stream;

public interface NodeValidator<T extends JcrNodeWrapper> {
    boolean validate(Stream<T> source, Stream<T> destination);
}
