package com.tbscg.doosan.magnolia.product.catalog.action;

import info.magnolia.ui.api.action.ActionType;
import info.magnolia.ui.api.action.CommandActionDefinition;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * Action definition class for importExternalAction
 */
@Data
@EqualsAndHashCode(callSuper = true)
@ActionType("importExternalAction")
public class ImportExternalModelActionDefinition extends CommandActionDefinition {

    private String targetPath;
    private ImportSource source;
    private String workspace;

    public ImportExternalModelActionDefinition() {
        setImplementationClass(ImportExternalModelAction.class);
    }
}
