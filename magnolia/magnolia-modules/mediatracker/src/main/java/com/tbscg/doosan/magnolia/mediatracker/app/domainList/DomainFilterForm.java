package com.tbscg.doosan.magnolia.mediatracker.app.domainList;

import com.tbscg.doosan.magnolia.mediatracker.MediatrackerModule;
import com.tbscg.doosan.magnolia.mediatracker.data.model.Division;
import com.tbscg.doosan.magnolia.mediatracker.data.model.DomainFilters;
import com.tbscg.doosan.magnolia.mediatracker.data.model.Status;
import com.tbscg.doosan.magnolia.mediatracker.ui.DateRangeField;
import com.tbscg.doosan.magnolia.mediatracker.ui.Header;
import com.vaadin.event.selection.SingleSelectionListener;
import com.vaadin.server.Sizeable;
import com.vaadin.ui.*;
import info.magnolia.objectfactory.Components;
import java.util.Arrays;

public class DomainFilterForm extends VerticalLayout {

    private final Header header;

    private final TextField domainName = new TextField("Domain");
    private final ComboBox<Status> statusSelect = new ComboBox<>("Status", Arrays.asList(Status.values()));

    private final DateRangeField startRange = new DateRangeField("Start");
    private final DateRangeField endRange = new DateRangeField("End");
    private final DateRangeField createdRange = new DateRangeField("Created");
    private final DateRangeField modifiedRange = new DateRangeField("Modified");

    public DomainFilterForm(String title, Division division) {
        setWidth(100, Sizeable.Unit.PERCENTAGE);

        MediatrackerModule moduleConfig = Components.getComponent(MediatrackerModule.class);
        String[] divisions = null;
        if (moduleConfig != null) {
            divisions = moduleConfig.getDivisions().split(",");
            this.header = new Header(title, Division.fromList(divisions));
        } else {
            this.header = new Header(title, Division.values());
        }
        addComponent(this.header);

        if (division != null) {
            setDivision(division);
        } else if (divisions != null && divisions.length >= 1) {
            setDivision(Division.valueOf(divisions[0]));
        } else {
            setDivision(Division.getDefault());
        }

        FormLayout left = new FormLayout();
        left.setMargin(false);
        left.setWidth(100, Sizeable.Unit.PERCENTAGE);
        left.addComponent(this.domainName);
        left.addComponent(this.statusSelect);

        FormLayout right = new FormLayout();
        right.setMargin(false);
        right.setWidth(100, Sizeable.Unit.PERCENTAGE);
        right.addComponent(this.startRange);
        right.addComponent(this.endRange);
        right.addComponent(this.createdRange);
        right.addComponent(this.modifiedRange);

        HorizontalLayout layout = new HorizontalLayout(left, right);
        layout.setWidth(100, Sizeable.Unit.PERCENTAGE);
        int height = 55 * Math.max(left.getComponentCount(), right.getComponentCount());
        layout.setHeight(height, Sizeable.Unit.PIXELS);
        addComponent(layout);
    }

    public void addDivisionChangeListener(SingleSelectionListener<Division> listener) {
        this.header.getDivisionSelect().addSelectionListener(listener);
    }

    public void setDivision(Division division) {
        this.header.getDivisionSelect().setValue(division);
    }

    public DomainFilters getFilters() {
        DomainFilters filters = new DomainFilters();
        filters.setDivision(this.header.getDivisionSelect().getValue());
        filters.setName(this.domainName.getValue());
        filters.setStatus(this.statusSelect.getValue());
        filters.setStartFrom(this.startRange.getFrom().getValue());
        filters.setStartTo(this.startRange.getTo().getValue());
        filters.setEndFrom(this.endRange.getFrom().getValue());
        filters.setEndTo(this.endRange.getTo().getValue());
        filters.setCreatedFrom(this.createdRange.getFrom().getValue());
        filters.setCreatedTo(this.createdRange.getTo().getValue());
        filters.setModifiedFrom(this.modifiedRange.getFrom().getValue());
        filters.setModifiedTo(this.modifiedRange.getTo().getValue());
        return filters;
    }
}
