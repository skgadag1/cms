package com.tbscg.doosan.magnolia.mediatracker.app.domainList;

import com.tbscg.doosan.magnolia.mediatracker.app.BaseMediatrackerListSubAppViewImpl;
import com.tbscg.doosan.magnolia.mediatracker.app.MediatrackerNavigator;
import com.tbscg.doosan.magnolia.mediatracker.data.export.ExportResource;
import com.tbscg.doosan.magnolia.mediatracker.data.model.*;
import com.tbscg.doosan.magnolia.mediatracker.data.model.request.MediatrackerRequest;
import com.tbscg.doosan.magnolia.mediatracker.data.provider.VanityDomainService;
import com.tbscg.doosan.magnolia.mediatracker.ui.ButtonBar;
import com.tbscg.doosan.magnolia.mediatracker.ui.pagination.ButtonType;
import com.vaadin.data.provider.ConfigurableFilterDataProvider;
import com.vaadin.data.provider.Query;
import com.vaadin.icons.VaadinIcons;
import com.vaadin.server.FileDownloader;
import com.vaadin.shared.ui.grid.HeightMode;
import com.vaadin.ui.*;

import javax.inject.Inject;
import java.util.stream.Collectors;

/**
 * Domain List Sub App view implementation
 */
public class DomainListSubAppViewImpl extends BaseMediatrackerListSubAppViewImpl implements DomainListSubAppView {

    private final Panel mainPanel = new Panel();
    private final VerticalLayout layout = new VerticalLayout();
    private final ConfigurableFilterDataProvider<VanityDomain, Void, DomainFilters> dataProvider;
    private final DomainFilterForm filterForm;
    private final Button exportButton;
    private Listener listener;
    private final VanityDomainService domainService;
    private final DomainGrid domainGrid;
    private FileDownloader fileDownloader;
    private ExportResource<VanityDomain> exportResource;

    @Inject
    public DomainListSubAppViewImpl(VanityDomainService domainService, MediatrackerNavigator navigator) {
        MediatrackerRequest request = navigator.getRequestData(MediatrackerRequest.class);
        this.dataProvider = domainService.withConfigurableFilter();

        this.domainService = domainService;
        this.filterForm = new DomainFilterForm("Manage Domains", request.getDivision());
        this.filterForm.addDivisionChangeListener(event -> {
            if (listener != null) {
                event.getSelectedItem().ifPresent(division -> {
                    listener.updateLocation(division);
                    refresh();
                });
            }
        });

        Button searchButton = new Button("Search", event -> refresh());
        searchButton.setIcon(VaadinIcons.SEARCH);

        Button createButton = new Button("Create new domain", event -> {
            this.listener.createNewDomain(this.filterForm.getFilters().getDivision());
        });
        createButton.setIcon(VaadinIcons.FILE_ADD);

        this.exportButton = new Button("Export");
        this.exportButton.setIcon(VaadinIcons.FILE_TABLE);
        createDownloadEvent(exportButton);

        this.domainGrid = new DomainGrid(this.dataProvider, () -> listener);
        this.domainGrid.setHeightMode(HeightMode.UNDEFINED);

        refresh();

        layout.setSizeFull();
        layout.setMargin(true);
        layout.setSpacing(true);
        layout.addComponent(filterForm);
        layout.addComponent(new ButtonBar(
                searchButton,
                createButton,
                exportButton
        ));
        layout.addComponent(domainGrid);
        layout.addComponent(pagination);

        mainPanel.setContent(layout);
    }

    private void createDownloadEvent(Button exportButton) {
        if (fileDownloader != null) {
            exportButton.removeExtension(fileDownloader);
        }

        exportResource = new ExportResource<>("domain.export.csv",
                () -> this.fetchAll().getItems(),
                new DomainExportGenerator());
        fileDownloader = new FileDownloader(exportResource);
        fileDownloader.extend(exportButton);
    }

    @Override
    protected void refreshUpdatedItems(StatusChanged statusChanged) {
        VanityDataSearchResult<VanityDomain> result = fetchUpdatedItems(false, statusChanged);
        refreshPagination(result);
        domainGrid.setItems(result.getItems());
        createDownloadEvent(exportButton);
    }

    protected void refresh() {
        refreshUpdatedItems(null);
    }

    @Override
    public void setListener(Listener listener) {
        this.listener = listener;
    }

    @Override
    public void setDivision(Division division, StatusChanged statusChanged) {
        if (this.filterForm.getFilters().getDivision() != division) {
            this.filterForm.setDivision(division);
        }
        refreshUpdatedItems(statusChanged);
    }

    @Override
    public void refreshGrid(StatusChanged statusChanged) {
        refreshUpdatedItems(statusChanged);
    }

    @Override
    public Component asVaadinComponent() {
        return mainPanel;
    }

    @Override
    protected VanityDataSearchResult<VanityDomain> fetchForPagination(ButtonType buttonType) {
        DomainFilters filters = filterForm.getFilters();
        filters.setSize(this.pagination.getLimit());
        this.setFiltersForButtonEvent(buttonType, filters);
        Query<VanityDomain, DomainFilters> query = new Query<>(filters);
        return this.domainService.fetchWithPagination(query);

    }

    @Override
    protected VanityDataSearchResult<VanityDomain> fetchAll() {
        return fetchUpdatedItems(true, null);
    }

    @Override
    protected VanityDataSearchResult<VanityDomain> fetchUpdatedItems(boolean shouldFetchAll, StatusChanged statusChanged) {
        DomainFilters filters = filterForm.getFilters();
        filters.setStatusChanged(statusChanged);
        this.addPaginationToFilters(shouldFetchAll, filters);
        Query<VanityDomain, DomainFilters> query = new Query<>(filters);
        return this.domainService.fetchWithPagination(query);
    }

    @Override
    protected Grid getGrid() {
        return domainGrid;
    }
}
