package com.tbscg.doosan.magnolia.pmd.app.documentView;

import com.tbscg.doosan.magnolia.pmd.app.PmdNavigator;
import com.tbscg.doosan.magnolia.pmd.data.model.Document;
import com.tbscg.doosan.magnolia.pmd.data.model.request.DocumentViewRequest;
import com.tbscg.doosan.magnolia.pmd.data.provider.api.ApiService;
import com.vaadin.server.Sizeable;
import com.vaadin.ui.Component;
import com.vaadin.ui.Panel;
import com.vaadin.ui.VerticalLayout;
import info.magnolia.ui.api.app.SubAppContext;

import javax.inject.Inject;

public class DocumentViewSubAppViewImpl implements DocumentViewSubAppView {

    private final VerticalLayout layout = new VerticalLayout();
    private final DocumentView documentView;
    private Listener listener;

    @Inject
    public DocumentViewSubAppViewImpl(
            SubAppContext ctx,
            ApiService apiService,
            PmdNavigator navigator) {

        DocumentViewRequest request = navigator.getRequestData(DocumentViewRequest.class);
        Document doc = apiService.getDocument(request.getId());
        documentView = new DocumentView(apiService, doc);

        layout.setWidthFull();
        layout.setMargin(true);
        layout.setSpacing(true);
        layout.addComponent(this.documentView);
    }

    @Override
    public void setListener(Listener listener) {
        this.listener = listener;
    }

    @Override
    public Component asVaadinComponent() {
        Panel panel = new Panel(layout);
        layout.setWidthFull();
        return panel;
    }
}
