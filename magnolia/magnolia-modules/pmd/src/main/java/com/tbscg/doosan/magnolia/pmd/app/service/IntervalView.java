package com.tbscg.doosan.magnolia.pmd.app.service;

import com.tbscg.doosan.magnolia.pmd.data.model.*;
import com.tbscg.doosan.magnolia.pmd.data.provider.api.ApiService;
import com.tbscg.doosan.magnolia.pmd.ui.AttributesTable;
import com.tbscg.doosan.magnolia.pmd.ui.ButtonBar;
import com.tbscg.doosan.magnolia.pmd.ui.Header;
import com.tbscg.doosan.magnolia.pmd.ui.service.*;
import com.vaadin.icons.VaadinIcons;
import com.vaadin.ui.*;
import lombok.Getter;
import org.vaadin.dialogs.ConfirmDialog;

import java.util.List;

@Getter
public class IntervalView extends VerticalLayout {

    private final ApiService apiService;
    private final Interval interval;

    private final Panel attrs = new Panel();
    private final Grid<IntervalTrigger> triggerGrid = new Grid<>();
    private final Grid<ServiceAction> actionGrid = new Grid<>();
    private final Grid<ServiceKit> kitGrid = new Grid<>();

    public IntervalView(ApiService apiService, Interval interval) {
        setWidthFull();
        this.apiService = apiService;
        this.interval = interval;
        this.attrs.setContent(new VerticalLayout(
                new Header("Interval details"),
                new AttributesTable(
                        new AttributesTable.Attribute("Interval name", new Label(interval.getName())),
                        new AttributesTable.Attribute("Labour hours", new Label(interval.getLabourHours()))
                )));
        addComponent(attrs);
        addComponent(triggers());
        addComponent(actions());
        addComponent(kits());
    }

    private void reloadTriggers() {
        List<IntervalTrigger> triggers = apiService.getIntervalTriggers(interval.getId());
        triggerGrid.setItems(triggers);
        triggerGrid.setHeightByRows(triggers.isEmpty() ? 1 : triggers.size());
    }

    private void reloadActions() {
        List<ServiceAction> actions = apiService.getServiceActions(interval.getId());
        actionGrid.setItems(actions);
        actionGrid.setHeightByRows(actions.isEmpty() ? 1 : actions.size());
    }

    private void reloadKits() {
        List<ServiceKit> kits = apiService.getServiceKits(interval.getId());
        kitGrid.setItems(kits);
        kitGrid.setHeightByRows(kits.isEmpty() ? 1 : kits.size());
    }

    private Component triggers() {
        triggerGrid.setSelectionMode(Grid.SelectionMode.NONE);
        triggerGrid.setWidthFull();
        triggerGrid.addColumn(IntervalTrigger::getTypeCode).setCaption("Type");
        triggerGrid.addColumn(IntervalTrigger::getInterval).setCaption("Interval");
        triggerGrid.addComponentColumn(this::triggerActions).setCaption("Actions");

        Button addNew = new Button("Add new interval trigger", VaadinIcons.PLUS);
        addNew.addClickListener(e -> {
            getUI().addWindow(TriggerEditForm.newTrigger(apiService, interval, trigger -> reloadTriggers()));
        });

        VerticalLayout layout = new VerticalLayout();
        layout.setWidthFull();
        layout.addComponent(new ButtonBar("Interval triggers:", addNew));
        layout.addComponentsAndExpand(triggerGrid);

        reloadTriggers();

        return layout;
    }

    private Component actions() {
        actionGrid.setSelectionMode(Grid.SelectionMode.NONE);
        actionGrid.setWidthFull();
        actionGrid.addColumn(ServiceAction::getTypeCode).setCaption("Type");
        actionGrid.addColumn(ServiceAction::getPartNumber).setCaption("Part number");
        actionGrid.addColumn(ServiceAction::getQuantity).setCaption("Quantity");
        actionGrid.addComponentColumn(this::actionActions).setCaption("Actions");

        Button addNew = new Button("Add new service action", VaadinIcons.PLUS);
        addNew.addClickListener(e -> {
            getUI().addWindow(ActionEditForm.newAction(apiService, interval, action -> reloadActions()));
        });

        VerticalLayout layout = new VerticalLayout();
        layout.setWidthFull();
        layout.addComponent(new ButtonBar("Service actions:", addNew));
        layout.addComponentsAndExpand(actionGrid);

        reloadActions();

        return layout;
    }

    private Component kits() {
        kitGrid.setSelectionMode(Grid.SelectionMode.NONE);
        kitGrid.setWidthFull();
        kitGrid.addColumn(ServiceKit::getPartNumber).setCaption("Part number");
        kitGrid.addComponentColumn(this::kitActions).setCaption("Actions");

        Button addNew = new Button("Add new service kit", VaadinIcons.PLUS);
        addNew.addClickListener(e -> {
            getUI().addWindow(KitEditForm.newKit(apiService, interval, kit -> reloadKits()));
        });

        VerticalLayout layout = new VerticalLayout();
        layout.setWidthFull();
        layout.addComponent(new ButtonBar("Service kits:", addNew));
        layout.addComponentsAndExpand(kitGrid);

        reloadKits();

        return layout;
    }

    private Component triggerActions(IntervalTrigger trigger) {
        MenuBar menu = new MenuBar();
        menu.addItem("delete", VaadinIcons.CLOSE, item -> {
            ConfirmDialog.show(getUI(), "Delete interval trigger?", dialog -> {
                if (dialog.isConfirmed()) {
                    this.apiService.deleteIntervalTrigger(interval, trigger.getId());
                    reloadTriggers();
                }
            });
        });
        return menu;
    }

    private Component actionActions(ServiceAction action) {
        MenuBar menu = new MenuBar();
        menu.addItem("delete", VaadinIcons.CLOSE, item -> {
            ConfirmDialog.show(getUI(), "Delete service action?", dialog -> {
                if (dialog.isConfirmed()) {
                    this.apiService.deleteServiceAction(interval, action.getId());
                    reloadActions();
                }
            });
        });
        return menu;
    }

    private Component kitActions(ServiceKit kit) {
        MenuBar menu = new MenuBar();
        menu.addItem("delete", VaadinIcons.CLOSE, item -> {
            ConfirmDialog.show(getUI(), "Delete service kit?", dialog -> {
                if (dialog.isConfirmed()) {
                    this.apiService.deleteServiceKit(interval, kit.getId());
                    reloadKits();
                }
            });
        });
        return menu;
    }
}