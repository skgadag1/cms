package com.tbscg.doosan.magnolia.pmd.app.schematicBrowser;

import com.tbscg.doosan.magnolia.pmd.data.model.Schematic;
import info.magnolia.ui.api.view.View;

public interface SchematicBrowserSubAppView extends View {

    void setListener(Listener listener);

    interface Listener {
        void viewSchematic(Schematic schematic);
    }
}
