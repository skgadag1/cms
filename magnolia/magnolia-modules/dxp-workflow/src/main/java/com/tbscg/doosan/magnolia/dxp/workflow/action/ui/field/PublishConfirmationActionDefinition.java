package com.tbscg.doosan.magnolia.dxp.workflow.action.ui.field;

import info.magnolia.ui.contentapp.action.ConfirmationActionDefinition;
import lombok.Getter;
import lombok.Setter;

import java.util.HashMap;
import java.util.Map;

@Setter
@Getter
public class PublishConfirmationActionDefinition extends ConfirmationActionDefinition {
    private Map<String, Object> params = new HashMap<>();

    public PublishConfirmationActionDefinition() {
        super();
        setImplementationClass(PublishConfirmationAction.class);
    }
}
