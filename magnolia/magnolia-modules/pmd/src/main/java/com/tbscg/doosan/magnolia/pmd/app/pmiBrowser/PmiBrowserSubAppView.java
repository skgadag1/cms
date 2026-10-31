package com.tbscg.doosan.magnolia.pmd.app.pmiBrowser;

import info.magnolia.ui.api.view.View;

public interface PmiBrowserSubAppView extends View {

    void setListener(Listener listener);

    interface Listener {
    }
}
