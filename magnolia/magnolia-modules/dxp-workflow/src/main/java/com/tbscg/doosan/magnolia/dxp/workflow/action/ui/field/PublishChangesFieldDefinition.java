package com.tbscg.doosan.magnolia.dxp.workflow.action.ui.field;

import info.magnolia.ui.field.StaticFieldViewDefinition;

import static org.apache.commons.lang3.StringUtils.EMPTY;

public class PublishChangesFieldDefinition extends StaticFieldViewDefinition {
    public PublishChangesFieldDefinition() {
        super();
        setImplementationClass(PublishChangesFieldView.class);
    }

    @Override
    public String getValue() {
        return EMPTY;
    }

    @Override
    public void setValue(String value) {
        throw new UnsupportedOperationException();
    }
}
