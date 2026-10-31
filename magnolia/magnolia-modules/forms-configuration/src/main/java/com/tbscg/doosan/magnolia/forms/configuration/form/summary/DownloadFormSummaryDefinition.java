package com.tbscg.doosan.magnolia.forms.configuration.form.summary;

import info.magnolia.ui.api.action.ActionType;
import info.magnolia.ui.api.action.CommandActionDefinition;

@ActionType("downloadFormSummary")
public class DownloadFormSummaryDefinition extends CommandActionDefinition {
    public DownloadFormSummaryDefinition() {
        setImplementationClass(DownloadFormSummary.class);
    }
}
