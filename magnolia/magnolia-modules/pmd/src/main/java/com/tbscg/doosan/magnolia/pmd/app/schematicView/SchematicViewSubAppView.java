package com.tbscg.doosan.magnolia.pmd.app.schematicView;

import info.magnolia.ui.api.view.View;

public interface SchematicViewSubAppView extends View {

    void setListener(Listener listener);

    interface Listener {
    }
}
