package com.tbscg.doosan.magnolia.cloudinary.contentview.column;

import info.magnolia.ui.workbench.column.definition.AbstractColumnDefinition;

public class AssetPathColumnDefinition extends AbstractColumnDefinition {

    private boolean includeFileName = false;

    public boolean isIncludeFileName() {
        return includeFileName;
    }

    public void setIncludeFileName(boolean includeFileName) {
        this.includeFileName = includeFileName;
    }
}
