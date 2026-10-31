package com.tbscg.doosan.magnolia.mediatracker.app.urlList;

import com.tbscg.doosan.magnolia.mediatracker.MediatrackerModule;
import com.tbscg.doosan.magnolia.mediatracker.data.model.*;
import com.tbscg.doosan.magnolia.mediatracker.ui.DateRangeField;
import com.tbscg.doosan.magnolia.mediatracker.ui.Header;
import com.vaadin.event.selection.SingleSelectionListener;
import com.vaadin.ui.*;
import info.magnolia.objectfactory.Components;
import lombok.Getter;
import lombok.Setter;

import java.util.Arrays;

@Getter
@Setter
public class UrlFilterForm extends VerticalLayout {

    private final Header header;

    private final ComboBox<Region> region = new ComboBox<>("Region", Arrays.asList(Region.values()));
    private final TextField path = new TextField("Path");
    private ComboBox<VanityDomain> domain;
    private final TextField placement = new TextField("Placement");
    private final TextField tracking = new TextField("Tracking parameter");
    private final ComboBox<Status> status = new ComboBox<>("Status", Arrays.asList(Status.values()));

    private final TextField redirectLocation = new TextField("Redirect Location");
    private final TextField redirectLocationMobile = new TextField("Mobile");
    private final TextField redirectLocationDesktop = new TextField("Desktop");
    private final TextField redirectLocationTablet = new TextField("Tablet");
    private final TextField redirectLocationOther = new TextField("Other");

    private final DateRangeField startRange = new DateRangeField("Start");
    private final DateRangeField endRange = new DateRangeField("End");
    private final DateRangeField createdRange = new DateRangeField("Created");
    private final DateRangeField modifiedRange = new DateRangeField("Modified");

    public UrlFilterForm(String title, Division division) {
        setWidth(100, Unit.PERCENTAGE);
        MediatrackerModule moduleConfig = Components.getComponent(MediatrackerModule.class);
        String[] divisions = null;
        if(moduleConfig!=null) {
            divisions = moduleConfig.getDivisions().split(",");
            this.header = new Header(title, Division.fromList(divisions));
        } else {
            this.header = new Header(title, Division.values());
        }
        addComponent(this.header);
        setDivision(division != null ? division : divisions != null ? Division.valueOf(divisions[0]) : Division.getDefault());

        FormLayout left = new FormLayout();
        left.setMargin(false);
        left.setWidth(100, Unit.PERCENTAGE);
        left.addComponent(this.region);
        left.addComponent(this.path);
        this.domain = new ComboBox("Domain");
        this.domain.setItemCaptionGenerator(d -> d.getFullDomainName());
        this.domain.setEmptySelectionAllowed(true);
        left.addComponent(this.domain);
        left.addComponent(this.placement);
        left.addComponent(this.tracking);
        left.addComponent(this.status);

        FormLayout middle = new FormLayout();
        middle.setMargin(false);
        middle.setWidth(100, Unit.PERCENTAGE);
        middle.addComponent(this.redirectLocation);
        middle.addComponent(this.redirectLocationMobile);
        middle.addComponent(this.redirectLocationDesktop);
        middle.addComponent(this.redirectLocationTablet);
        middle.addComponent(this.redirectLocationOther);

        FormLayout right = new FormLayout();
        right.setMargin(false);
        right.setWidth(100, Unit.PERCENTAGE);
        right.addComponent(this.startRange);
        right.addComponent(this.endRange);
        right.addComponent(this.createdRange);
        right.addComponent(this.modifiedRange);

        HorizontalLayout layout = new HorizontalLayout(left, middle, right);
        layout.setWidth(100, Unit.PERCENTAGE);
        layout.setHeight(calculateHeight(left, middle, right), Unit.PIXELS);
        addComponent(layout);
    }

    private int calculateHeight(ComponentContainer... innerLayouts) {
        int maxComponentCount = 1;
        for (ComponentContainer c : innerLayouts) {
            maxComponentCount = Math.max(maxComponentCount, c.getComponentCount());
        }
        return 55 * maxComponentCount;
    }

    public void addDivisionChangeListener(SingleSelectionListener<Division> listener) {
        this.header.getDivisionSelect().addSelectionListener(listener);
    }

    public void setDivision(Division division) {
        this.header.getDivisionSelect().setValue(division);
    }

    public UrlFilters getFilters() {
        UrlFilters filters = new UrlFilters();
        filters.setDivision(this.header.getDivisionSelect().getValue());

        filters.setRegion(this.region.getValue());
        filters.setPath(this.path.getValue());
        filters.setDomain(this.domain.getValue() != null ? this.domain.getValue().getFullDomainName() : "");
        filters.setDomainCode(this.domain.getValue() != null ? this.domain.getValue().getCode() : "");
        filters.setPlacement(this.placement.getValue());
        filters.setTrackingParameter(this.tracking.getValue());
        filters.setStatus(this.status.getValue());

        filters.setRedirectLocation(this.redirectLocation.getValue());
        filters.setRedirectLocationMobile(this.redirectLocationMobile.getValue());
        filters.setRedirectLocationDesktop(this.redirectLocationDesktop.getValue());
        filters.setRedirectLocationTablet(this.redirectLocationTablet.getValue());
        filters.setRedirectLocationOther(this.redirectLocationOther.getValue());

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
