package com.tbscg.doosan.magnolia.pmd.app.schematicBrowser;

import com.tbscg.doosan.magnolia.pmd.data.model.*;
import com.tbscg.doosan.magnolia.pmd.data.provider.api.ApiService;
import com.tbscg.doosan.magnolia.pmd.ui.schematics.SchematicEditForm;
import com.tbscg.doosan.magnolia.pmd.ui.pmi.PmiSelectorTree;
import com.vaadin.data.provider.ConfigurableFilterDataProvider;
import com.vaadin.data.provider.DataProvider;
import com.vaadin.data.provider.Query;
import com.vaadin.icons.VaadinIcons;
import com.vaadin.server.Sizeable;
import com.vaadin.ui.*;
import org.apache.commons.lang3.StringUtils;
import org.vaadin.dialogs.ConfirmDialog;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class SchematicBrowserSubAppViewImpl implements SchematicBrowserSubAppView {

    private final ApiService apiService;
    private final Panel mainPanel = new Panel();
    private final PmiSelectorTree prefixSelect;
    private final TextField queryField = new TextField("Search by serial number");
    private final Grid<Schematic> schematics = getSchematicsGrid();

    private Listener listener;

    @Inject
    public SchematicBrowserSubAppViewImpl(ApiService apiService) {
        this.apiService = apiService;
        this.prefixSelect = new PmiSelectorTree(apiService, Prefix.class);
        this.prefixSelect.setWidth(400, Sizeable.Unit.PIXELS);
        this.prefixSelect.setHeightFull();

        ConfigurableFilterDataProvider<Schematic, Void, SchematicListFilter> dataProvider =
                DataProvider.<Schematic, SchematicListFilter>fromFilteringCallbacks(
                        query1 -> querySchematics(query1).getSchematics().stream(),
                        query1 -> querySchematics(query1).getTotal()
                ).withConfigurableFilter();
        this.schematics.setDataProvider(dataProvider);

        Button addNewSchematic = new Button("Add new schematic", e -> {
            this.mainPanel.getUI().addWindow(SchematicEditForm.newSchematic(apiService, s -> {
                schematics.getDataProvider().refreshItem(s);
                listener.viewSchematic(s);
            }));
        });

        HorizontalLayout layout = new HorizontalLayout();
        layout.setSizeFull();
        layout.setMargin(true);
        layout.setSpacing(true);
        layout.addComponent(this.prefixSelect);

        HorizontalLayout searchbar = new HorizontalLayout();
        searchbar.setDefaultComponentAlignment(Alignment.BOTTOM_LEFT);
        searchbar.setWidthFull();
        this.queryField.setWidthFull();
        searchbar.addComponent(this.queryField);
        searchbar.addComponent(new Button("Search", e -> {
            SchematicListFilter filter = new SchematicListFilter();
            if (StringUtils.isNotBlank(this.queryField.getValue())) {
                filter.setSerial(this.queryField.getValue());
                prefixSelect.setValue(null);
            }
            dataProvider.setFilter(filter);
        }));
        searchbar.addComponent(addNewSchematic);
        searchbar.setComponentAlignment(addNewSchematic, Alignment.BOTTOM_RIGHT);

        VerticalLayout contentpane = new VerticalLayout();
        contentpane.setSizeFull();
        contentpane.addComponent(searchbar);
        contentpane.addComponentsAndExpand(this.schematics);
        layout.addComponentsAndExpand(contentpane);

        this.prefixSelect.addValueChangeListener(e -> {
            ConnectTarget value = e.getValue();
            if (value instanceof Prefix) {
                Prefix prefix = (Prefix) value;
                this.queryField.setValue("");
                SchematicListFilter filter = new SchematicListFilter();
                if (prefix != null) {
                    filter.setPmiType("prefix");
                    filter.setPmiId(prefix.getId());
                }
                dataProvider.setFilter(filter);
            }
        });

        this.mainPanel.setSizeFull();
        this.mainPanel.setContent(layout);
    }

    private SchematicsResponse querySchematics(Query<Schematic, SchematicListFilter> query) {
        List<Object> params = new ArrayList<>();
        params.add("limit");
        params.add(query.getLimit());
        params.add("offset");
        params.add(query.getOffset());
        query.getFilter().ifPresent(f -> {
            if (StringUtils.isNotBlank(f.getSerial())) {
                params.add("serial");
                params.add(f.getSerial());
            }
            if (StringUtils.isNotBlank(f.getPmiType()) && f.getPmiId() != null) {
                params.add(f.getPmiType() + "_id");
                params.add(f.getPmiId());
            }
        });
        SchematicsResponse response = apiService.getSchematics(params.toArray());
        if (response != null) {
            return response;
        }
        return new SchematicsResponse().withSchematics(Collections.emptyList()).withTotal(0);
    }

    private Grid<Schematic> getSchematicsGrid() {
        Grid<Schematic> grid = new Grid<>();
        grid.addColumn(Schematic::getName).setCaption("Name");
        grid.addColumn(Schematic::getType).setCaption("Type");
        grid.addComponentColumn(this::getActions).setCaption("Actions");
        grid.setSizeFull();
        return grid;
    }

    private Component getActions(Schematic schematic) {
        MenuBar menu = new MenuBar();
        menu.addItem("view", VaadinIcons.EYE, item -> listener.viewSchematic(schematic));
        menu.addItem("edit", VaadinIcons.EDIT, item -> {
            mainPanel.getUI().addWindow(SchematicEditForm.editSchematic(apiService, schematic, s -> {
                this.schematics.getDataProvider().refreshAll();
            }));
        });
        menu.addItem("delete", VaadinIcons.CLOSE, item -> {
            ConfirmDialog.show(mainPanel.getUI(), "Remove schematic " + schematic.getName() + "?", dialog -> {
                if (dialog.isConfirmed()) {
                    apiService.deleteSchematic(schematic.getId());
                    this.schematics.getDataProvider().refreshAll();
                }
            });
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
