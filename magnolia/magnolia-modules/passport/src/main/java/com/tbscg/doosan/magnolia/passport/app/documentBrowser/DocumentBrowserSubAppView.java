package com.tbscg.doosan.magnolia.passport.app.documentBrowser;

import com.tbscg.doosan.magnolia.passport.data.model.PassportDocument;
import info.magnolia.ui.api.view.View;

public interface DocumentBrowserSubAppView extends View {

    void setListener(Listener listener);

    interface Listener {
        void viewDocument(PassportDocument document);
    }
}
