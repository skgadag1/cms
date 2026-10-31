package com.tbscg.doosan.magnolia.mediatracker.app.urlList;

import com.tbscg.doosan.magnolia.mediatracker.app.BaseMediatrackerListSubAppViewImpl;
import com.tbscg.doosan.magnolia.mediatracker.app.MediatrackerNavigator;
import com.tbscg.doosan.magnolia.mediatracker.data.export.ExportResource;
import com.tbscg.doosan.magnolia.mediatracker.data.model.*;
import com.tbscg.doosan.magnolia.mediatracker.data.model.request.MediatrackerRequest;
import com.tbscg.doosan.magnolia.mediatracker.data.provider.VanityDomainService;
import com.tbscg.doosan.magnolia.mediatracker.data.provider.VanityUrlService;
import com.tbscg.doosan.magnolia.mediatracker.ui.ButtonBar;
import com.tbscg.doosan.magnolia.mediatracker.ui.pagination.ButtonType;
import com.vaadin.data.provider.ConfigurableFilterDataProvider;
import com.vaadin.data.provider.Query;
import com.vaadin.icons.VaadinIcons;
import com.vaadin.server.FileDownloader;
import com.vaadin.shared.ui.grid.HeightMode;
import com.vaadin.ui.*;
import org.apache.commons.lang3.StringUtils;

import javax.inject.Inject;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Url List Sub App view implementation
 */
public class UrlListSubAppViewImpl extends BaseMediatrackerListSubAppViewImpl implements UrlListSubAppView {

    private final Panel mainPanel = new Panel();
    private final VerticalLayout layout = new VerticalLayout();
    private final ConfigurableFilterDataProvider<VanityUrl, Void, UrlFilters> dataProvider;
    private final UrlFilterForm filterForm;
    private final VanityDomainService vanityDomainService;
    private final MediatrackerNavigator navigator;
    private final VanityUrlService urlService;
    private ExportResource<VanityUrl> exportResource;
    private final Button exportButton;
    private FileDownloader fileDownloader;

    private List<VanityDomain> domainsForDivision;
    private Listener listener;
    private final UrlGrid urlGrid;
    private String codeParam;

    @Inject
    public UrlListSubAppViewImpl(VanityUrlService urlService, MediatrackerNavigator navigator, VanityDomainService vanityDomainService) {
        this.vanityDomainService = vanityDomainService;
        this.urlService = urlService;
        this.dataProvider = urlService.withConfigurableFilter();
        this.navigator = navigator;

        MediatrackerRequest request = navigator.getRequestData(MediatrackerRequest.class);
        this.filterForm = new UrlFilterForm("Manage URLs", request.getDivision());
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

        Button createButton = new Button("Create new URL", event -> {
            this.listener.createNewUrl(this.filterForm.getFilters().getDivision());
        });
        createButton.setIcon(VaadinIcons.FILE_ADD);

        exportButton = new Button("Export");
        exportButton.setIcon(VaadinIcons.FILE_TABLE);
        createDownloadEvent(exportButton);

        this.urlGrid = new UrlGrid(this.dataProvider, () -> listener);
        this.urlGrid.setHeightMode(HeightMode.UNDEFINED);

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
        layout.addComponent(urlGrid);
        layout.addComponent(pagination);

        mainPanel.setContent(layout);
    }

    private void createDownloadEvent(Button exportButton) {
        if (fileDownloader != null) {
            exportButton.removeExtension(fileDownloader);
        }
        exportResource = new ExportResource<>("url.export.csv",
                this::fetchLinkedVanityUrl,
                new UrlExportGenerator());
        fileDownloader = new FileDownloader(exportResource);
        fileDownloader.extend(exportButton);
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
    public void setCodeParam(String code) {
        this.codeParam = code;
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
    protected void refreshUpdatedItems(StatusChanged statusChanged) {
        this.domainsForDivision = this.vanityDomainService.getDomains(this.filterForm.getHeader().getDivisionSelect().getSelectedItem().get());
        this.filterForm.getDomain().setItems(domainsForDivision);
        if (StringUtils.isNotBlank(this.codeParam)) {
            Optional<VanityDomain> domain = this.domainsForDivision.stream().filter(d -> d.getCode().equals(this.codeParam)).findFirst();
            if (domain.isPresent()) {
                this.filterForm.getDomain().setSelectedItem(domain.get());
            }
            this.codeParam = "";
        }
        this.urlGrid.setDomains(domainsForDivision);
        VanityDataSearchResult<VanityUrl> result = fetchUpdatedItems(false, statusChanged);
        refreshPagination(result);
        urlGrid.setItems(result.getItems());
        createDownloadEvent(exportButton);
    }

    @Override
    protected VanityDataSearchResult<VanityUrl> fetchForPagination(ButtonType buttonType) {
        UrlFilters filters = filterForm.getFilters();
        filters.setSize(this.pagination.getLimit());
        this.setFiltersForButtonEvent(buttonType, filters);
        Query<VanityUrl, UrlFilters> query = new Query<>(filters);
        return this.urlService.fetchWithPagination(query);
    }

    @Override
    protected VanityDataSearchResult<VanityUrl> fetchAll() {
        return fetchUpdatedItems(true, null);
    }

    @Override
    protected VanityDataSearchResult<VanityUrl> fetchUpdatedItems(boolean shouldFetchAll, StatusChanged statusChanged) {
        UrlFilters filters = filterForm.getFilters();
        filters.setStatusChanged(statusChanged);
        this.addPaginationToFilters(shouldFetchAll, filters);
        Query<VanityUrl, UrlFilters> query = new Query<>(filters);
        return this.urlService.fetchWithPagination(query);
    }

    @Override
    protected void refresh() {
        refreshUpdatedItems(null);
    }

    private List<VanityUrl> fetchLinkedVanityUrl() {
        VanityDataSearchResult<VanityUrl> vanityUrls = fetchAll();
        for (VanityUrl vanityUrl : vanityUrls.getItems()) {
            Optional<VanityDomain> vanityDomain = this.domainsForDivision.stream().filter(d -> d.getCode().equals(vanityUrl.getVanityDomain().getCode())).findFirst();
            vanityDomain.ifPresent(vanityUrl::setVanityDomain);
        }
        return vanityUrls.getItems();
    }

    @Override
    protected Grid getGrid() {
        return urlGrid;
    }
}
