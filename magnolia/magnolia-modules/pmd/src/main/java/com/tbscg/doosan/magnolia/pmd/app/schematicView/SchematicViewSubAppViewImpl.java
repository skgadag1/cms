package com.tbscg.doosan.magnolia.pmd.app.schematicView;

import com.tbscg.doosan.magnolia.pmd.app.PmdNavigator;
import com.tbscg.doosan.magnolia.pmd.data.model.Schematic;
import com.tbscg.doosan.magnolia.pmd.data.model.request.SchematicViewRequest;
import com.tbscg.doosan.magnolia.pmd.data.provider.api.ApiService;
import com.vaadin.server.Sizeable;
import com.vaadin.ui.Component;
import com.vaadin.ui.Panel;
import com.vaadin.ui.VerticalLayout;
import info.magnolia.ui.api.app.SubAppContext;

import javax.inject.Inject;

public class SchematicViewSubAppViewImpl implements SchematicViewSubAppView {

    private final VerticalLayout layout = new VerticalLayout();
    private final SchematicView schematicView;
    private Listener listener;

    @Inject
    public SchematicViewSubAppViewImpl(
            SubAppContext ctx,
            ApiService apiService,
            PmdNavigator navigator) {

        SchematicViewRequest request = navigator.getRequestData(SchematicViewRequest.class);
        Schematic schematic = apiService.getSchematic(request.getId());
        schematicView = new SchematicView(apiService, schematic);

        layout.setWidthFull();
        layout.setMargin(true);
        layout.setSpacing(true);
        layout.addComponent(this.schematicView);
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
