package com.tbscg.doosan.magnolia.pmd.app.service;

import com.tbscg.doosan.magnolia.pmd.data.model.*;
import com.tbscg.doosan.magnolia.pmd.data.model.ScheduleListFilter;
import com.tbscg.doosan.magnolia.pmd.data.model.SchedulesResponse;
import com.tbscg.doosan.magnolia.pmd.data.provider.api.ApiService;
import com.tbscg.doosan.magnolia.pmd.ui.service.ScheduleEditForm;
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

public class ScheduleBrowserSubAppViewImpl implements ScheduleBrowserSubAppView {

    private final ApiService apiService;
    private final Panel mainPanel = new Panel();
    private final PmiSelectorTree pmiSelect;
    private final TextField queryField = new TextField("Search by serial number");
    private final Grid<Schedule> schedules = getSchedulesGrid();

    private Listener listener;

    @Inject
    public ScheduleBrowserSubAppViewImpl(ApiService apiService) {
        this.apiService = apiService;

        this.pmiSelect = new PmiSelectorTree(apiService, Model.class, Prefix.class, Range.class);
        this.pmiSelect.setWidth(400, Sizeable.Unit.PIXELS);
        this.pmiSelect.setHeightFull();

        ConfigurableFilterDataProvider<Schedule, Void, ScheduleListFilter> dataProvider =
                DataProvider.<Schedule, ScheduleListFilter>fromFilteringCallbacks(
                        q -> querySchedules(q).getSchedules().stream(),
                        q -> querySchedules(q).getTotal()
                ).withConfigurableFilter();
        this.schedules.setDataProvider(dataProvider);

        Button addNewSchedule = new Button("Add new schedule", e -> {
            this.mainPanel.getUI().addWindow(ScheduleEditForm.newSchedule(apiService, s -> {
                schedules.getDataProvider().refreshItem(s);
                listener.viewSchedule(s);
            }));
        });

        HorizontalLayout layout = new HorizontalLayout();
        layout.setSizeFull();
        layout.setMargin(true);
        layout.setSpacing(true);
        layout.addComponent(this.pmiSelect);

        HorizontalLayout searchbar = new HorizontalLayout();
        searchbar.setDefaultComponentAlignment(Alignment.BOTTOM_LEFT);
        searchbar.setWidthFull();
        this.queryField.setWidthFull();
        searchbar.addComponent(this.queryField);
        searchbar.addComponent(new Button("Search", e -> {
            ScheduleListFilter filter = new ScheduleListFilter();
            if (StringUtils.isNotBlank(this.queryField.getValue())) {
                filter.setSerial(this.queryField.getValue());
                pmiSelect.setValue(null);
            }
            dataProvider.setFilter(filter);
        }));
        searchbar.addComponent(addNewSchedule);
        searchbar.setComponentAlignment(addNewSchedule, Alignment.BOTTOM_RIGHT);

        VerticalLayout contentpane = new VerticalLayout();
        contentpane.setSizeFull();
        contentpane.addComponent(searchbar);
        contentpane.addComponentsAndExpand(this.schedules);
        layout.addComponentsAndExpand(contentpane);

        this.pmiSelect.addValueChangeListener(e -> {
            ConnectTarget selected = e.getValue();
            this.queryField.setValue("");
            ScheduleListFilter filter = new ScheduleListFilter();
            if (selected != null) {
                filter.setPmiType(selected.getType());
                filter.setPmiId(selected.getId());
            }
            dataProvider.setFilter(filter);
        });

        this.mainPanel.setSizeFull();
        this.mainPanel.setContent(layout);
    }

    private SchedulesResponse querySchedules(Query<Schedule, ScheduleListFilter> query) {
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
                params.add(f.getPmiType().toLowerCase() + "_id");
                params.add(f.getPmiId());
            }
        });
        SchedulesResponse response = apiService.getSchedules(params.toArray());
        if (response != null) {
            return response;
        }
        return new SchedulesResponse().withSchedules(Collections.emptyList()).withTotal(0);
    }

    private Grid<Schedule> getSchedulesGrid() {
        Grid<Schedule> grid = new Grid<>();
        grid.addColumn(Schedule::getName).setCaption("Name");
        grid.addColumn(Schedule::getBusinessUnit).setCaption("Business Unit");
        grid.addComponentColumn(this::getActions).setCaption("Actions");
        grid.setSizeFull();
        return grid;
    }

    private Component getActions(Schedule schedule) {
        MenuBar menu = new MenuBar();
        menu.addItem("view", VaadinIcons.EYE, item -> listener.viewSchedule(schedule));
        menu.addItem("edit", VaadinIcons.EDIT, item -> {
            mainPanel.getUI().addWindow(ScheduleEditForm.editSchedule(apiService, schedule, s -> {
                this.schedules.getDataProvider().refreshAll();
            }));
        });
        menu.addItem("delete", VaadinIcons.CLOSE, item -> {
            List<Interval> intervals = apiService.getIntervals(schedule.getId());
            if (intervals != null && !intervals.isEmpty()) {
                Notification.show(
                        "Can't remove a schedule with intervals defined.\nRemove all interval data first.",
                        Notification.Type.ERROR_MESSAGE
                );
            } else {
                ConfirmDialog.show(mainPanel.getUI(), "Remove schedule " + schedule.getName() + "?", dialog -> {
                    if (dialog.isConfirmed()) {
                        apiService.deleteSchedule(schedule.getId());
                        this.schedules.getDataProvider().refreshAll();
                    }
                });
            }
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
