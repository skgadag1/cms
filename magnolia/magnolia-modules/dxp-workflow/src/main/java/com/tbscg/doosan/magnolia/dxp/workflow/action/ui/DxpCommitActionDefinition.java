package com.tbscg.doosan.magnolia.dxp.workflow.action.ui;

import info.magnolia.ui.api.action.ActionType;
import info.magnolia.ui.contentapp.action.CommitActionDefinition;
import lombok.extern.slf4j.Slf4j;

import java.util.Collections;
import java.util.List;

import static java.util.Collections.emptyList;

@SuppressWarnings("unused")
@Slf4j
@ActionType("dxpCommit")
public class DxpCommitActionDefinition extends CommitActionDefinition {

    private List<String> customOnSaveProcessors = emptyList();

    public DxpCommitActionDefinition() {
        setImplementationClass(DxpCommitAction.class);
    }

    public void setCustomOnSaveProcessors(List<String> customOnSaveProcessors) {
        if (customOnSaveProcessors != null) {
            this.customOnSaveProcessors = Collections.unmodifiableList(customOnSaveProcessors);
        } else {
            this.customOnSaveProcessors = emptyList();
        }
        if (log.isDebugEnabled()) {
            log.debug("customOnSaveProcessors: {}", String.join(", ", this.customOnSaveProcessors));
        }
    }

    public List<String> getCustomOnSaveProcessors() {
        return customOnSaveProcessors;
    }

}
