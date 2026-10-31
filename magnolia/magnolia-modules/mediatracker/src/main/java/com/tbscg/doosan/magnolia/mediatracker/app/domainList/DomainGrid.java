package com.tbscg.doosan.magnolia.mediatracker.app.domainList;

import com.tbscg.doosan.magnolia.mediatracker.data.model.Status;
import com.tbscg.doosan.magnolia.mediatracker.data.model.VanityDomain;
import com.tbscg.doosan.magnolia.mediatracker.util.DateFormatUtil;
import com.vaadin.data.provider.DataProvider;
import com.vaadin.ui.Component;
import com.vaadin.ui.Grid;
import com.vaadin.ui.MenuBar;

import java.util.Comparator;
import java.util.function.Supplier;

import static com.vaadin.icons.VaadinIcons.*;

public class DomainGrid extends Grid<VanityDomain> {

    private final Supplier<DomainListSubAppView.Listener> listener;

    public DomainGrid(DataProvider<VanityDomain, ?> dataProvider, Supplier<DomainListSubAppView.Listener> listener) {
        this.listener = listener;
        setDataProvider(dataProvider);
        setSizeFull();
        addColumn(VanityDomain::getCode).setCaption("Code");
        addColumn(VanityDomain::getFullDomainName).setCaption("Domain");
        addColumn(d -> DateFormatUtil.showDateOnly(d.getStartDate())).setCaption("Start");
        addColumn(d -> DateFormatUtil.showDateOnly(d.getEndDate())).setCaption("End");
        addColumn(VanityDomain::getDisplayedStatus).setCaption("Status");
        addColumn(VanityDomain::getCreatedBy).setCaption("CreatedBy");
        addComponentColumn(this::getDomainActions).setCaption("Actions");

        addItemClickListener(event -> {
            if (event.getMouseEventDetails().isDoubleClick()) {
                listener.get().editDomain(event.getItem());
            }
        });

        getDataCommunicator();
    }

    private Component getDomainActions(VanityDomain domain) {
        MenuBar menu = new MenuBar();
        menu.addItem("edit", EDIT, item -> listener.get().editDomain(domain));
        menu.addItem("URLs", EDIT, item -> listener.get().showRelatedUrls(domain));
        if(domain.getStatus() != Status.APPROVED)
            menu.addItem("publish", CHECK_SQUARE_O, item -> listener.get().setDomainStatus(domain, Status.APPROVED));
        if(domain.getStatus() == Status.APPROVED)
            menu.addItem("archive", ARCHIVE, item -> listener.get().setDomainStatus(domain, Status.ARCHIVED));
        if(domain.getStatus() != Status.APPROVED)
            menu.addItem("delete", CLOSE, item -> listener.get().deleteDomain(domain));
        return menu;
    }

}
