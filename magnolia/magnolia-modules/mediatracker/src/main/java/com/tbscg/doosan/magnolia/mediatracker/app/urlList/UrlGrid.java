package com.tbscg.doosan.magnolia.mediatracker.app.urlList;

import com.tbscg.doosan.magnolia.mediatracker.data.model.Status;
import com.tbscg.doosan.magnolia.mediatracker.data.model.VanityDomain;
import com.tbscg.doosan.magnolia.mediatracker.data.model.VanityUrl;
import com.tbscg.doosan.magnolia.mediatracker.util.DateFormatUtil;
import com.vaadin.data.provider.DataProvider;
import com.vaadin.ui.Component;
import com.vaadin.ui.Grid;
import com.vaadin.ui.MenuBar;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import java.util.Comparator;
import java.util.List;
import java.util.function.Supplier;

import static com.vaadin.icons.VaadinIcons.*;

@Getter
@Setter
@Slf4j
public class UrlGrid extends Grid<VanityUrl> {

    private final Supplier<UrlListSubAppView.Listener> listener;
    private List<VanityDomain> domains;

    public UrlGrid(DataProvider<VanityUrl, ?> dataProvider,
                   Supplier<UrlListSubAppView.Listener> listener) {

        this.listener = listener;

        setDataProvider(dataProvider);
        setSizeFull();
        addColumn(VanityUrl::getCode).setCaption("Code");
        addColumn(url -> findFullName(url.getVanityDomain().getCode())).setCaption("Domain");
        addColumn(VanityUrl::getPath).setCaption("Path");
        addColumn(VanityUrl::getRegion).setCaption("Region");
        addColumn(url -> DateFormatUtil.showDateOnly(url.getStartDate())).setCaption("Start");
        addColumn(url -> DateFormatUtil.showDateOnly(url.getEndDate())).setCaption("End");
        addColumn(VanityUrl::getDisplayedStatus).setCaption("Status");
        addColumn(VanityUrl::getCreatedBy).setCaption("CreatedBy");
        addComponentColumn(this::getUrlActions).setCaption("Actions");

        addItemClickListener(event -> {
            if (event.getMouseEventDetails().isDoubleClick()) {
                listener.get().editUrl(event.getItem());
            }
        });
        getDataCommunicator();
    }

    public String findFullName(String code) {
        VanityDomain domain = this.domains.stream()
                .filter(d -> d.getCode().equals(code))
                .findAny()
                .orElse(null);
        return domain != null ? domain.getFullDomainName() : code;
    }

    private Component getUrlActions(VanityUrl url) {
        MenuBar menu = new MenuBar();
        VanityDomain domain = url.scanForDomain(this.domains);
        if(domain != null) {
            menu.addItem("edit", EDIT, item -> listener.get().editUrl(url));
            if(url.getStatus() != Status.APPROVED && domain.getStatus() != Status.ARCHIVED && domain.getStatus() != Status.DRAFT) {
                menu.addItem("publish", CHECK_SQUARE_O, item -> listener.get().setUrlStatus(url, Status.APPROVED, domain));
            }
            if(url.getStatus() == Status.APPROVED) {
                menu.addItem("archive", ARCHIVE, item -> listener.get().setUrlStatus(url, Status.ARCHIVED, domain));
            }
            if(url.getStatus() != Status.APPROVED) {
                menu.addItem("delete", CLOSE, item -> listener.get().deleteUrl(url, domain));
            }
        }
        return menu;
    }
}
