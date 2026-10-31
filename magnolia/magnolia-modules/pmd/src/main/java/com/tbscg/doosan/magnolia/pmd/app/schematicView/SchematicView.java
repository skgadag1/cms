package com.tbscg.doosan.magnolia.pmd.app.schematicView;

import com.tbscg.doosan.magnolia.pmd.data.model.*;
import com.tbscg.doosan.magnolia.pmd.data.provider.api.ApiService;
import com.tbscg.doosan.magnolia.pmd.ui.*;
import com.tbscg.doosan.magnolia.pmd.ui.schematics.SchematicConnectionForm;
import com.tbscg.doosan.magnolia.pmd.ui.schematics.SchematicEditForm;
import com.vaadin.icons.VaadinIcons;
import com.vaadin.ui.*;
import lombok.Getter;
import org.vaadin.dialogs.ConfirmDialog;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Getter
public class SchematicView extends VerticalLayout {

    private final ApiService apiService;

    private Schematic schematic;
    private final TextField schematicName = new TextField("Schematic");

    private final Grid<SchematicConnection> connectionsGrid = new Grid<>();
    private final Grid<DocumentFile> filesGrid = new Grid<>();
    private List<DocumentFile> files = Collections.emptyList();

    private final Header header = new Header();
    private final Label partNumbers = new Label();

    public SchematicView(ApiService apiService, Schematic schematic) {
        setWidth(100, Unit.PERCENTAGE);
        this.apiService = apiService;
        this.schematic = schematic;
        createContent();
    }

    private void createContent() {
        addComponent(this.header);

        this.partNumbers.setCaption("Part numbers");
        addComponent(this.partNumbers);

        addComponent(new Button("Edit attributes", e -> {
            getUI().addWindow(SchematicEditForm.editSchematic(apiService, schematic, this::updateSchematic));
        }));

        HorizontalLayout layout = new HorizontalLayout();
        layout.setSizeFull();
        layout.addComponent(connections());
        layout.addComponent(files());
        addComponent(layout);

        updateSchematic(schematic);
    }

    private void updateSchematic(Schematic schematic) {
        this.schematic = schematic;
        this.header.setValue(schematic.getName());
        this.partNumbers.setValue(schematic.getPartNumbers());
    }

    private void reloadConnections() {
        connectionsGrid.setItems(apiService.getSchematicConnections(schematic.getId()));
    }

    private void reloadFiles() {
        List<DocumentFile> files = apiService.getSchematicFiles(schematic.getId());
        this.files = files;
        this.filesGrid.setItems(files);
    }

    private Component connections() {
        connectionsGrid.setSelectionMode(Grid.SelectionMode.NONE);
        connectionsGrid.setCaption("Assigned ranges:");
        connectionsGrid.setSizeFull();
        connectionsGrid.addColumn(SchematicConnection::getFamilyCode).setCaption("Family");
        connectionsGrid.addColumn(SchematicConnection::getSubfamilyCode).setCaption("Subfamily");
        connectionsGrid.addColumn(SchematicConnection::getModelCode).setCaption("Model");
        connectionsGrid.addColumn(SchematicConnection::getPrefix).setCaption("Prefix").setWidth(100);
        connectionsGrid.addColumn(SchematicConnection::getSuffixStart).setCaption("Suffix from").setWidth(100);
        connectionsGrid.addColumn(SchematicConnection::getSuffixEnd).setCaption("Suffix to").setWidth(100);
        connectionsGrid.addComponentColumn(this::connectionActions).setCaption("Actions");
        reloadConnections();

        Button addNew = new Button("Add new connection", VaadinIcons.CONNECT);
        addNew.addClickListener(e -> {
            SchematicConnectionForm form = new SchematicConnectionForm(apiService);
            getUI().addWindow(new EditWindow<>("Add new connection", form, data -> {
                this.apiService.connectSchematic(schematic, data);
                reloadConnections();
            }));
        });

        VerticalLayout layout = new VerticalLayout();
        layout.setSizeFull();
        layout.addComponent(new ButtonBar(addNew));
        layout.addComponentsAndExpand(connectionsGrid);
        return layout;
    }

    private Component connectionActions(SchematicConnection connection) {
        MenuBar menu = new MenuBar();
        menu.addItem("unlink", VaadinIcons.UNLINK, item -> {
            String range = connection.getPrefix() + connection.getSuffixStart() + " - "
                         + connection.getPrefix() + connection.getSuffixEnd();
            ConfirmDialog.show(getUI(), "Unlink range " + range + " ?", dialog -> {
                if (dialog.isConfirmed()) {
                    this.apiService.disconnectSchematic(schematic.getId(), connection.getId());
                    reloadConnections();
                }
            });
        });
        return menu;
    }

    private Component files() {
        filesGrid.setSelectionMode(Grid.SelectionMode.NONE);
        filesGrid.setCaption("Files:");
        filesGrid.setSizeFull();
        filesGrid.addComponentColumn(f -> new ExternalLink(f.getFileName(), f.getDownloadUrl())).setCaption("Filename");
        filesGrid.addColumn(DocumentFile::getLocale).setCaption("Locale").setWidth(80);
        filesGrid.addColumn(DocumentFile::getContentType).setCaption("Content type").setWidth(150);
        filesGrid.addComponentColumn(this::fileActions).setCaption("Actions").setWidth(120);
        reloadFiles();

        Button uploadButton = new Button("Upload new file", VaadinIcons.UPLOAD);
        uploadButton.addClickListener(e -> {
            List<Locale> selectableFileLocales = this.apiService.getLocales().stream()
                    .filter(l -> this.files.stream().noneMatch(f -> f.getLocale().equals(l.getCode())))
                    .collect(Collectors.toList());
            getUI().addWindow(new LocalFileUpload(
                    data -> this.apiService.addSchematicFile(schematic.getId(), data).getUploadUrl(),
                    selectableFileLocales,
                    this::reloadFiles));
        });

        VerticalLayout layout = new VerticalLayout();
        layout.setSizeFull();
        layout.addComponent(uploadButton);
        layout.addComponentsAndExpand(filesGrid);
        return layout;
    }

    private Component fileActions(DocumentFile file) {
        MenuBar menu = new MenuBar();
        menu.addItem("delete", VaadinIcons.CLOSE, item -> {
            ConfirmDialog.show(getUI(), "Remove file " + file.getFileName() + "?", dialog -> {
                if (dialog.isConfirmed()) {
                    apiService.deleteSchematicFile(schematic.getId(), file.getId());
                    reloadFiles();
                }
            });
        });
        return menu;
    }
}
