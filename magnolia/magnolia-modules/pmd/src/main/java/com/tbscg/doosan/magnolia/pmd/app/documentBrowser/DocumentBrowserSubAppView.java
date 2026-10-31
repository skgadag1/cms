package com.tbscg.doosan.magnolia.pmd.app.documentBrowser;

import com.tbscg.doosan.magnolia.pmd.data.model.Document;
import info.magnolia.ui.api.view.View;

public interface DocumentBrowserSubAppView extends View {

    void setListener(Listener listener);

    interface Listener {
        void viewDocument(Document document);
    }
}
