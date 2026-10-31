package com.tbscg.doosan.magnolia.passport.app.documentView;

import com.tbscg.doosan.magnolia.passport.app.PassportNavigator;
import com.tbscg.doosan.magnolia.passport.data.model.PassportDocumentWithDetails;
import com.tbscg.doosan.magnolia.passport.data.model.request.PassportDocumentViewRequest;
import com.tbscg.doosan.magnolia.passport.data.provider.api.ApiService;
import com.vaadin.ui.Component;
import com.vaadin.ui.Panel;
import com.vaadin.ui.VerticalLayout;
import info.magnolia.ui.api.app.SubAppContext;
import lombok.extern.slf4j.Slf4j;

import javax.inject.Inject;
@Slf4j
public class DocumentViewSubAppViewImpl implements DocumentViewSubAppView {

    private final VerticalLayout layout = new VerticalLayout();
    private final DocumentView documentView;
    private Listener listener;

    @Inject
    public DocumentViewSubAppViewImpl(
            SubAppContext ctx,
            ApiService apiService,
            PassportNavigator navigator) {

        PassportDocumentViewRequest request = navigator.getRequestData(PassportDocumentViewRequest.class);
        PassportDocumentWithDetails document = apiService.getDocument(request.getId());
        documentView = new DocumentView(apiService, document);

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
