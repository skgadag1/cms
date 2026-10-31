package com.tbscg.doosan.magnolia.passport.app.documentBrowser;

import com.tbscg.doosan.magnolia.passport.data.model.*;
import com.tbscg.doosan.magnolia.passport.data.provider.api.ApiService;
import com.tbscg.doosan.magnolia.passport.ui.documents.DocumentEditForm;
import com.tbscg.doosan.magnolia.passport.util.DocumentConstants;
import com.vaadin.annotations.StyleSheet;
import com.vaadin.data.provider.ConfigurableFilterDataProvider;
import com.vaadin.data.provider.DataProvider;
import com.vaadin.data.provider.Query;
import com.vaadin.icons.VaadinIcons;
import com.vaadin.server.Sizeable;
import com.vaadin.ui.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.vaadin.dialogs.ConfirmDialog;

import javax.inject.Inject;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@StyleSheet("vaadin://themes/passport/passport-v-menubar.css")
public class DocumentBrowserSubAppViewImpl implements DocumentBrowserSubAppView {

    private static final int ES_ROWS_LIMIT = 10000;
    private static final int SEARCH_BAR_WIDTH = 400;
    private static final int NEW_DOCUMENT_BUTTON_WIDTH = 160;
    private static final int FILTER_WIDTH = 150;
    private static final int APPLICATION_FILTER_WIDTH = 200;
    private final ApiService apiService;
    private final Panel mainPanel = new Panel();
    private final TextField queryByNameAndDescriptionField = new TextField();
    private final List<PropertySimple> brands;
    private final List<PropertySimple> regions;
    private final List<PropertySimple> applications;
    private final List<DocumentConstants.Subject> subject;
    private final ComboBox<PropertySimple> queryByBrandField = new ComboBox<>();
    private final ComboBox<PropertySimple> queryByRegionField = new ComboBox<>();
    private final ComboBox<PropertySimple> queryByApplicationField = new ComboBox<>();
    private final ComboBox<DocumentConstants.Subject> queryBySubjectField = new ComboBox<>();
    private final Grid<PassportDocument> documents = getDocumentsList();
    private Listener listener;

    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM/dd/yy - hh:mm a");

    @Inject
    public DocumentBrowserSubAppViewImpl(ApiService apiService) {
        this.apiService = apiService;

        ConfigurableFilterDataProvider<PassportDocument, Void, PassportDocumentListFilter> dataProvider =
                DataProvider.<PassportDocument, PassportDocumentListFilter>fromFilteringCallbacks(
                        query1 -> queryDocuments(query1).getPassportDocuments().stream(),
                        query1 -> queryDocuments(query1).getResults()
                ).withConfigurableFilter();
        this.documents.setDataProvider(dataProvider);

        Button addNewDocument = new Button("Add new document", e -> {
            this.mainPanel.getUI().addWindow(DocumentEditForm.newDocument(apiService, doc -> {
                listener.viewDocument(doc);
            }));
        });
        addNewDocument.setWidth(NEW_DOCUMENT_BUTTON_WIDTH, Sizeable.Unit.PIXELS);

        HorizontalLayout searchbar = new HorizontalLayout();
        searchbar.setWidthFull();
        searchbar.setDefaultComponentAlignment(Alignment.BOTTOM_LEFT);

        this.queryByNameAndDescriptionField.setWidth(SEARCH_BAR_WIDTH, Sizeable.Unit.PIXELS);
        this.queryByNameAndDescriptionField.setWidthFull();
        this.queryByNameAndDescriptionField.setPlaceholder("Search document title or description");
        searchbar.addComponent(this.queryByNameAndDescriptionField);

        this.brands = apiService.getBrands();
        this.queryByBrandField.setItemCaptionGenerator(PropertySimple::getName);
        this.queryByBrandField.setItems(this.brands);
        this.queryByBrandField.setWidth(FILTER_WIDTH, Sizeable.Unit.PIXELS);
        this.queryByBrandField.setPlaceholder("Select brand");
        searchbar.addComponent(this.queryByBrandField);

        this.regions = apiService.getRegions();
        this.queryByRegionField.setItemCaptionGenerator(PropertySimple::getName);
        this.queryByRegionField.setItems(this.regions);
        this.queryByRegionField.setWidth(FILTER_WIDTH, Sizeable.Unit.PIXELS);
        this.queryByRegionField.setPlaceholder("Select region");
        searchbar.addComponent(this.queryByRegionField);

        this.applications = apiService.getBaseApplications();
        this.queryByApplicationField.setItemCaptionGenerator(PropertySimple::getName);
        this.queryByApplicationField.setItems(this.applications);
        this.queryByApplicationField.setWidth(APPLICATION_FILTER_WIDTH, Sizeable.Unit.PIXELS);
        this.queryByApplicationField.setPlaceholder("Select application");
        searchbar.addComponent(this.queryByApplicationField);

        this.subject = Arrays.asList(DocumentConstants.Subject.values());
        this.queryBySubjectField.setItems(this.subject);
        this.queryBySubjectField.setWidth(FILTER_WIDTH, Sizeable.Unit.PIXELS);
        this.queryBySubjectField.setPlaceholder("Select subject");
        searchbar.addComponent(this.queryBySubjectField);

        searchbar.addComponent(new Button("Search", e -> {
            PassportDocumentListFilter filter = new PassportDocumentListFilter();
            if (StringUtils.isNotBlank(this.queryByNameAndDescriptionField.getValue())) {
                filter.setDescription(this.queryByNameAndDescriptionField.getValue());
            }
            if (this.queryBySubjectField.getValue() != null) {
                filter.setSubject(this.queryBySubjectField.getValue().name());
            }
            if (this.queryByRegionField.getValue() != null) {
                filter.setRegionIds(String.valueOf(this.queryByRegionField.getValue().getId()));
            }
            if (this.queryByBrandField.getValue() != null) {
                filter.setBrandIds(String.valueOf(this.queryByBrandField.getValue().getId()));
            }
            if (this.queryByApplicationField.getValue() != null) {
                filter.setApplicationBaseIds(String.valueOf(this.queryByApplicationField.getValue().getId()));
            }
            dataProvider.setFilter(filter);
        }));

        searchbar.addComponent(addNewDocument);
        searchbar.setComponentAlignment(addNewDocument, Alignment.BOTTOM_RIGHT);

        VerticalLayout contentpane = new VerticalLayout();
        contentpane.setSizeFull();
        contentpane.addComponent(searchbar);
        contentpane.addComponentsAndExpand(this.documents);

        mainPanel.setSizeFull();
        mainPanel.setContent(contentpane);
    }

    private PassportDocumentResponse queryDocuments(Query<PassportDocument, PassportDocumentListFilter> query) {
        List<Object> params = new ArrayList<>();
        int allowedRowsPerPage = (query.getLimit() <= ES_ROWS_LIMIT) ? query.getLimit() : ES_ROWS_LIMIT;
        params.add("rows_per_page");
        params.add(allowedRowsPerPage);
        params.add("page");
        params.add((query.getOffset() < 1) ? 1 : query.getOffset());
        params.add("language");
        params.add(DocumentConstants.getSystemOrDefaultLocaleCode());

        query.getFilter().ifPresent(f -> {
            if (StringUtils.isNotBlank(f.getDescription())) {
                params.add("text");
                params.add(f.getDescription());
            }
            if (StringUtils.isNotBlank(f.getSubject())) {
                params.add("subject");
                params.add(f.getSubject());
            }
            if (StringUtils.isNotBlank(f.getRegionIds())) {
                params.add("region");
                params.add(f.getRegionIds());
            }
            if (StringUtils.isNotBlank(f.getBrandIds())) {
                params.add("brand");
                params.add(f.getBrandIds());
            }
            if (StringUtils.isNotBlank(f.getApplicationBaseIds())) {
                params.add("application_base_ids");
                params.add(f.getApplicationBaseIds());
            }
        });

        PassportDocumentResponse response = apiService.getPassportDocuments(params.toArray());
        if (response != null) {
            return response;
        }
        return new PassportDocumentResponse().withPassportDocuments(Collections.emptyList()).withResults(0);
    }

    private Grid<PassportDocument> getDocumentsList() {
        Grid<PassportDocument> grid = new Grid<>();
        grid.addColumn(PassportDocument::getId).setCaption("Id");
        grid.addColumn(PassportDocument::getTitle).setCaption("Title");
        grid.addColumn(PassportDocument::getDescription).setCaption("Description");
        grid.addColumn(this::getBrandNames).setCaption("Brands");
        grid.addColumn(this::getRegionNames).setCaption("Regions");
        grid.addColumn(this::getApplicationNames).setCaption("Applications");
        grid.addColumn(PassportDocument::getSubject).setCaption("Subject");
        grid.addColumn(doc-> Instant.ofEpochSecond(doc.getStartDateTime())
                .atZone(ZoneId.systemDefault()).format(formatter)).setCaption("Start date");
        grid.addColumn(doc-> Instant.ofEpochSecond(doc.getEndDateTime())
                .atZone(ZoneId.systemDefault()).format(formatter)).setCaption("End date");
        grid.addComponentColumn(this::getDocumentActions).setCaption("Actions");
        grid.setSizeFull();
        return grid;
    }

    private Component getDocumentActions(PassportDocument doc) {
        MenuBar menu = new MenuBar();
        menu.addItem("settings", VaadinIcons.EYE, item -> listener.viewDocument(doc));
        menu.addItem("edit", VaadinIcons.EDIT, item -> {
            mainPanel.getUI().addWindow(DocumentEditForm.editDocument(apiService, doc, listener::viewDocument));
        });
        menu.addItem("delete", VaadinIcons.CLOSE, item -> {
            ConfirmDialog.show(mainPanel.getUI(), "Remove document " + doc.getId() + "?", dialog -> {
                if (dialog.isConfirmed()) {
                    apiService.deleteDocument(doc.getId());
                    this.documents.getDataProvider().refreshAll();
                }
            });
        });
        return menu;
    }

    private String getBrandNames(PassportDocument doc){
        return this.apiService.getBrands().stream()
                .filter(t -> doc.getBrandIds().contains(Long.valueOf(t.getId())))
                .map( b -> b.getName()).collect(Collectors.joining( ", " ));
    }

    private String getRegionNames(PassportDocument doc){
        return this.apiService.getRegions().stream()
                .filter(r -> doc.getRegionIds().contains(Long.valueOf(r.getId())))
                .map( r -> r.getName()).collect(Collectors.joining( ", " ));
    }

    private String getApplicationNames(PassportDocument doc) {
        return this.apiService.getBaseApplications().stream()
                .filter(app -> doc.getApplicationBaseIds().contains(app.getId()))
                .map(PropertySimple::getName)
                .collect(Collectors.joining(", "));
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
