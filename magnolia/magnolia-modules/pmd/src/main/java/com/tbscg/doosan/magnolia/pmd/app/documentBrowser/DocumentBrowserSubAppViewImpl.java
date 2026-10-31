package com.tbscg.doosan.magnolia.pmd.app.documentBrowser;

import com.tbscg.doosan.magnolia.pmd.data.model.ConnectTarget;
import com.tbscg.doosan.magnolia.pmd.data.model.Document;
import com.tbscg.doosan.magnolia.pmd.data.provider.api.ApiService;
import com.tbscg.doosan.magnolia.pmd.ui.DocumentEditForm;
import com.tbscg.doosan.magnolia.pmd.ui.pmi.PmiSelector;
import com.vaadin.icons.VaadinIcons;
import com.vaadin.server.Sizeable;
import com.vaadin.ui.*;

import javax.inject.Inject;

public class DocumentBrowserSubAppViewImpl implements DocumentBrowserSubAppView {

    private final ApiService apiService;
    private final Panel mainPanel = new Panel();
    private final PmiSelector pmiSelect;
    private final Grid<Document> documents = getDocumentsGrid();

    private Listener listener;

    @Inject
    public DocumentBrowserSubAppViewImpl(ApiService apiService) {
        this.apiService = apiService;
        this.pmiSelect = new PmiSelector(apiService);

        Button addNewDoc = new Button("Add new document", e -> {
            mainPanel.getUI().addWindow(DocumentEditForm.newDocument(apiService, newDoc -> {
                ConnectTarget connectTarget = pmiSelect.getSelected();
                if (connectTarget != null) {
                    apiService.connectDocument(newDoc, connectTarget);
                }
                reloadDocuments();
                listener.viewDocument(newDoc);
            }));
        });

        HorizontalLayout layout = new HorizontalLayout();
        layout.setSizeFull();
        layout.setMargin(true);
        layout.setSpacing(true);

        VerticalLayout sidebar = new VerticalLayout();
        sidebar.setWidth(500, Sizeable.Unit.PIXELS);
        sidebar.addComponent(pmiSelect);
        sidebar.addComponent(addNewDoc);
        sidebar.addComponentsAndExpand(new VerticalLayout());

        layout.addComponent(sidebar);
        layout.addComponentsAndExpand(documents);

        pmiSelect.setSelectionHandler((selected) -> reloadDocuments());
        reloadDocuments();

        mainPanel.setSizeFull();
        mainPanel.setContent(layout);
    }

    private void reloadDocuments() {
        ConnectTarget selected = pmiSelect.getSelected();
        if (selected != null) {
            documents.setItems(apiService.getDocuments(selected.getType() + "_id", selected.getId()));
        } else {
            documents.setItems(apiService.getDocuments());
        }
    }

    private Grid<Document> getDocumentsGrid() {
        Grid<Document> grid = new Grid<>();
        grid.addColumn(Document::getId).setCaption("ID");
        grid.addColumn(Document::getTitle).setCaption("Title");
        grid.addColumn(Document::getDescription).setCaption("Description");
        grid.addColumn(Document::getType).setCaption("Type");
        grid.addComponentColumn(this::getDocumentActions).setCaption("Actions");
        grid.setSizeFull();
        return grid;
    }

    private Component getDocumentActions(Document doc) {
        MenuBar menu = new MenuBar();
        menu.addItem("view", VaadinIcons.EYE, item -> listener.viewDocument(doc));
        menu.addItem("edit", VaadinIcons.EDIT, item -> {
            mainPanel.getUI().addWindow(DocumentEditForm.editDocument(apiService, doc, updated -> reloadDocuments()));
        });
        return menu;
    }

    @Override
    public void setListener(Listener listener) {
        this.listener = listener;
    }

    @Override
    public Component asVaadinComponent() {
        return mainPanel;
    }
}
