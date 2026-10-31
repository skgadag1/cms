package com.tbscg.doosan.magnolia.pmd.app.documentView;

import info.magnolia.ui.api.view.View;

public interface DocumentViewSubAppView extends View {

    void setListener(Listener listener);

    interface Listener {
    }
}
