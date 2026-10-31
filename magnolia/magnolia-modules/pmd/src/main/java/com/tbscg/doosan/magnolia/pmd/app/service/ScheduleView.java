package com.tbscg.doosan.magnolia.pmd.app.service;

import com.tbscg.doosan.magnolia.pmd.data.model.Interval;
import com.tbscg.doosan.magnolia.pmd.data.model.Schedule;
import com.tbscg.doosan.magnolia.pmd.data.model.ScheduleConnection;
import com.tbscg.doosan.magnolia.pmd.data.provider.api.ApiService;
import com.tbscg.doosan.magnolia.pmd.ui.*;
import com.tbscg.doosan.magnolia.pmd.ui.AttributesTable.Attribute;
import com.tbscg.doosan.magnolia.pmd.ui.service.IntervalEditForm;
import com.tbscg.doosan.magnolia.pmd.ui.service.ScheduleConnectionForm;
import com.tbscg.doosan.magnolia.pmd.ui.service.ScheduleEditForm;
import com.vaadin.icons.VaadinIcons;
import com.vaadin.ui.*;
import lombok.Getter;
import org.apache.commons.lang3.StringUtils;
import org.vaadin.dialogs.ConfirmDialog;

import java.util.List;

@Getter
public class ScheduleView extends HorizontalLayout {

    private final ApiService apiService;
    private Schedule schedule;

    private final Panel attrs = new Panel();
    private final Grid<ScheduleConnection> connectionsGrid = new Grid<>();
    private final Grid<Interval> intervalsGrid = new Grid<>();
    private final Panel rightPanel = new Panel();

    public ScheduleView(ApiService apiService, Schedule schedule) {
        setSizeFull();
        this.apiService = apiService;
        this.schedule = schedule;

        VerticalLayout leftPanelContent = new VerticalLayout();
        leftPanelContent.setWidthFull();
        leftPanelContent.addComponent(this.attrs);
        leftPanelContent.addComponent(connections());
        leftPanelContent.addComponent(intervals());

        Panel leftPanel = new Panel();
        leftPanel.setSizeFull();
        leftPanel.setContent(leftPanelContent);

        rightPanel.setSizeFull();
        addComponentsAndExpand(leftPanel, rightPanel);

        updateSchedule(schedule);
    }

    private void updateSchedule(Schedule schedule) {
        this.schedule = schedule;
        this.attrs.setContent(new VerticalLayout(
                new Header("Schedule details"),
                new AttributesTable(
                        new Attribute("Schedule name", new Label(schedule.getName())),
                        new Attribute("Business unit", new Label(schedule.getBusinessUnit())),
                        new Attribute("Document link", getScheduleLink(schedule))
                ),
                new Button("Edit attributes", e -> {
                    getUI().addWindow(ScheduleEditForm.editSchedule(apiService, schedule, this::updateSchedule));
                })
        ));
    }

    private Component getScheduleLink(Schedule schedule) {
        String url = schedule.getMaintenanceInstructionUrl();
        Component link;
        if (StringUtils.isNotBlank(url)) {
            link = new ExternalLink("click to open", url);
        } else {
            link = new Label("N/A");
        }
        return link;
    }

    private void reloadConnections() {
        List<ScheduleConnection> connections = apiService.getScheduleConnections(schedule.getId());
        connectionsGrid.setItems(connections);
        connectionsGrid.setHeightByRows(connections.isEmpty() ? 1 : connections.size());
    }

    private void reloadIntervals() {
        List<Interval> intervals = apiService.getIntervals(schedule.getId());
        intervalsGrid.setItems(intervals);
        intervalsGrid.setHeightByRows(intervals.isEmpty() ? 1 : intervals.size());
    }

    private Component connections() {
        connectionsGrid.setSelectionMode(Grid.SelectionMode.NONE);
        connectionsGrid.setWidthFull();
        connectionsGrid.addColumn(ScheduleConnection::getFamilyCode).setCaption("Family");
        connectionsGrid.addColumn(ScheduleConnection::getSubfamilyCode).setCaption("Subfamily");
        connectionsGrid.addColumn(ScheduleConnection::getModelCode).setCaption("Model");
        connectionsGrid.addColumn(ScheduleConnection::getPrefix).setCaption("Prefix").setWidth(100);
        connectionsGrid.addColumn(c -> {
            if (StringUtils.isBlank(c.getSuffixStart()) && StringUtils.isBlank(c.getSuffixEnd())) return "";
            return StringUtils.defaultString(c.getSuffixStart()) + "-" + StringUtils.defaultString(c.getSuffixEnd());
        }).setCaption("Suffix").setWidth(150);
        connectionsGrid.addComponentColumn(this::connectionActions).setCaption("Actions");

        Button addNew = new Button("Add new connection", VaadinIcons.CONNECT);
        addNew.addClickListener(e -> {
            ScheduleConnectionForm form = new ScheduleConnectionForm(apiService);
            getUI().addWindow(new EditWindow<>("Add new connection", form, data -> {
                this.apiService.connectSchedule(schedule, data);
                reloadConnections();
            }));
        });

        VerticalLayout layout = new VerticalLayout();
        layout.setSizeFull();
        layout.addComponent(new ButtonBar("PMI connections:", addNew));
        layout.addComponentsAndExpand(connectionsGrid);

        reloadConnections();

        return layout;
    }

    private Component intervals() {
        intervalsGrid.setSelectionMode(Grid.SelectionMode.SINGLE);
        intervalsGrid.setWidthFull();
        intervalsGrid.addColumn(Interval::getName).setCaption("Name");
        intervalsGrid.addColumn(Interval::getLabourHours).setCaption("Labour hours");
        intervalsGrid.addComponentColumn(this::intervalActions).setCaption("Actions");
        intervalsGrid.addSelectionListener(e -> {
            loadIntervalDetails(e.getFirstSelectedItem().orElse(null));
        });

        Button addNew = new Button("Add new interval", VaadinIcons.PLUS);
        addNew.addClickListener(e -> {
            getUI().addWindow(IntervalEditForm.newInterval(apiService, schedule, interval -> {
                reloadIntervals();
                this.intervalsGrid.select(interval);
            }));
        });

        VerticalLayout layout = new VerticalLayout();
        layout.setSizeFull();
        layout.addComponent(new ButtonBar("Intervals:", addNew));
        layout.addComponentsAndExpand(intervalsGrid);

        reloadIntervals();

        return layout;
    }

    private Component connectionActions(ScheduleConnection connection) {
        MenuBar menu = new MenuBar();
        menu.addItem("unlink", VaadinIcons.UNLINK, item -> {
            String message = "Unlink " + connection.getType() + " " + connection.getLabel() + " ?";
            ConfirmDialog.show(getUI(), message, dialog -> {
                if (dialog.isConfirmed()) {
                    this.apiService.disconnectSchedule(schedule.getId(), connection.getId());
                    reloadConnections();
                }
            });
        });
        return menu;
    }

    private void loadIntervalDetails(Interval interval) {
        Component content = interval != null ? new IntervalView(this.apiService, interval) : new Label("");
        this.rightPanel.setContent(content);
    }

    private Component intervalActions(Interval interval) {
        MenuBar menu = new MenuBar();
        menu.addItem("edit", VaadinIcons.EDIT, item -> {
            getUI().addWindow(IntervalEditForm.editInterval(apiService, interval, data -> reloadIntervals()));
        });
        menu.addItem("delete", VaadinIcons.CLOSE, item -> {
            ConfirmDialog.show(getUI(), "Delete interval " + interval.getName() + "?\n" +
                    "All related data (triggers, actions, kits) will be removed too.", dialog -> {
                if (dialog.isConfirmed()) {
                    this.apiService.deleteInterval(interval.getId());
                    reloadIntervals();
                }
            });
        });
        menu.addItem("view details", VaadinIcons.EYE, item -> {
            this.intervalsGrid.select(interval);
        });
        return menu;
    }

}
