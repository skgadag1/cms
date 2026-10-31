  package com.tbscg.doosan.magnolia.pmd.app.documentView;

  import com.tbscg.doosan.magnolia.pmd.data.model.Document;
  import com.tbscg.doosan.magnolia.pmd.data.model.DocumentConnection;
  import com.tbscg.doosan.magnolia.pmd.data.model.DocumentFile;
  import com.tbscg.doosan.magnolia.pmd.data.provider.api.ApiService;
  import com.tbscg.doosan.magnolia.pmd.ui.ButtonBar;
  import com.tbscg.doosan.magnolia.pmd.ui.LocalFileUpload;
  import com.vaadin.icons.VaadinIcons;
  import com.vaadin.server.ExternalResource;
  import com.vaadin.ui.*;
  import lombok.Getter;

@Getter
public class DocumentView extends VerticalLayout {

    private final ApiService apiService;

    private final Document document;
    private final TextField documentName = new TextField("Document");

    private final Grid<DocumentConnection> connectionsGrid = new Grid<>();
    private final Grid<DocumentFile> filesGrid = new Grid<>();

    public DocumentView(ApiService apiService, Document document) {
        setWidth(100, Unit.PERCENTAGE);
        this.apiService = apiService;
        this.document = document;
        createContent();
    }

    private void createContent() {
        addComponent(getHeader(document.getTitle()));
        addComponent(new Label(document.getDescription()));
        HorizontalLayout layout = new HorizontalLayout();
        layout.setSizeFull();
        layout.addComponent(connections());
        layout.addComponent(files());
        addComponent(layout);
    }

    private void reloadConnections() {
        connectionsGrid.setItems(apiService.getDocumentConnections(document.getId()));
    }

    private void reloadFiles() {
        filesGrid.setItems(apiService.getDocumentFiles(document.getId()));
    }

    private Component connections() {
        connectionsGrid.setSelectionMode(Grid.SelectionMode.NONE);
        connectionsGrid.setCaption("Connections:");
        connectionsGrid.setSizeFull();
        connectionsGrid.addColumn(DocumentConnection::getTarget).setCaption("Type").setWidth(100);
        connectionsGrid.addColumn(DocumentConnection::getTargetLabel).setCaption("Target");
        connectionsGrid.addComponentColumn(this::connectionActions).setCaption("Actions").setWidth(120);
        reloadConnections();

        Button addNew = new Button("Add new connection", VaadinIcons.CONNECT);
        addNew.addClickListener(e -> {
//            PmiSelector pmiSelector = new PmiSelector(apiService);
//            getUI().addWindow(new EditWindow("Add new connection", pmiSelector, data -> {
//                ConnectTarget selected = pmiSelector.getSelected();
//                if (selected != null) {
//                    this.apiService.connectDocument(document, selected);
//                    reloadConnections();
//                }
//            }));
        });

        VerticalLayout layout = new VerticalLayout();
        layout.setSizeFull();
        layout.addComponent(new ButtonBar(addNew));
        layout.addComponentsAndExpand(connectionsGrid);
        return layout;
    }

    private Component connectionActions(DocumentConnection connection) {
        MenuBar menu = new MenuBar();
        menu.addItem("unlink", VaadinIcons.UNLINK, item -> {
            this.apiService.disconnectDocument(document.getId(), connection.getId());
            reloadConnections();
        });
        return menu;
    }

    private Component files() {
        filesGrid.setSelectionMode(Grid.SelectionMode.NONE);
        filesGrid.setCaption("Files:");
        filesGrid.setSizeFull();
        filesGrid.addComponentColumn(file -> {
            Link link = new Link(file.getFileName(), new ExternalResource(file.getDownloadUrl()));
            link.setTargetName("_blank");
            return link;
        }).setCaption("Filename");
        filesGrid.addColumn(DocumentFile::getLocale).setCaption("Locale").setWidth(80);
        filesGrid.addColumn(DocumentFile::getContentType).setCaption("Content type").setWidth(150);
        filesGrid.addComponentColumn(this::fileActions).setCaption("Actions").setWidth(120);
        reloadFiles();

        Button uploadButton = new Button("Upload new file", VaadinIcons.UPLOAD);
        uploadButton.addClickListener(e -> getUI().addWindow(new LocalFileUpload(
                data -> apiService.addDocumentFile(document.getId(), data).getUploadUrl(),
                this.apiService.getLocales(),
                this::reloadFiles)));

        VerticalLayout layout = new VerticalLayout();
        layout.setSizeFull();
        layout.addComponent(uploadButton);
        layout.addComponentsAndExpand(filesGrid);
        return layout;
    }

    private Component fileActions(DocumentFile file) {
        MenuBar menu = new MenuBar();
        menu.addItem("delete", VaadinIcons.CLOSE, item -> {
            apiService.deleteDocumentFile(document.getId(), file.getId());
            reloadFiles();
        });
        return menu;
    }

    private Component getHeader(String title) {
        Label titleLabel = new Label(title);
        titleLabel.addStyleName("heading-2");
        VerticalLayout layout = new VerticalLayout();
        layout.addComponent(titleLabel);
        layout.setWidth(100, Unit.PERCENTAGE);
        return layout;
    }

}
