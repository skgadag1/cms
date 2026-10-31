package com.tbscg.doosan.magnolia.mediatracker.app.domainList;

import com.tbscg.doosan.magnolia.mediatracker.app.BaseDomainSubApp;
import com.tbscg.doosan.magnolia.mediatracker.app.MediatrackerNavigator;
import com.tbscg.doosan.magnolia.mediatracker.data.model.Division;
import com.tbscg.doosan.magnolia.mediatracker.data.model.VanityDomain;
import com.tbscg.doosan.magnolia.mediatracker.data.model.*;
import com.tbscg.doosan.magnolia.mediatracker.data.model.request.MediatrackerRequest;
import com.tbscg.doosan.magnolia.mediatracker.data.provider.VanityDomainService;
import com.tbscg.doosan.magnolia.mediatracker.data.provider.VanityUrlService;
import info.magnolia.ui.api.app.SubAppContext;
import info.magnolia.ui.api.location.Location;
import info.magnolia.ui.api.shell.Shell;
import lombok.extern.slf4j.Slf4j;

import javax.inject.Inject;

/**
 * Domain List sub app class
 */
@Slf4j
public class DomainListSubApp extends BaseDomainSubApp<DomainListSubAppView> implements DomainListSubAppView.Listener {
    
    @Inject
    public DomainListSubApp(SubAppContext subAppContext,
                            DomainListSubAppView view,
                            MediatrackerNavigator navigator,
                            VanityDomainService domainService,
                            VanityUrlService vanityUrlService, Shell shell) {

        super(subAppContext, view, navigator, domainService, vanityUrlService, shell);
        view.setListener(this);
    }

    @Override
    protected void onSubAppStart() {
        log.info("{} subApp started, location: {}", getSubAppId(), getSubAppContext().getLocation());
    }

    @Override
    public void locationChanged(Location location) {
        super.locationChanged(location);
        log.info("{} subApp location changed: {}", getSubAppId(), location);
        MediatrackerRequest request = navigator.getRequestData(MediatrackerRequest.class);
        if (request.getDivision() != null) {
            getView().setDivision(request.getDivision(), getStatusChanged(location));
        }
    }

    @Override
    protected void refreshGrid(StatusChanged statusChanged) {
        getView().refreshGrid(statusChanged);
    }

    @Override
    public boolean supportsLocation(Location location) {
        return true;
    }

    @Override
    public void updateLocation(Division division) {
        this.navigator.setParameter(new MediatrackerRequest(division));
    }

    @Override
    public void createNewDomain(Division division) {
        this.navigator.domainNew(division);
    }

    @Override
    public void editDomain(VanityDomain domain) {
        this.navigator.domainEdit(domain);
    }


    @Override
    public void showRelatedUrls(VanityDomain domain) {
        this.navigator.urlList(domain);
    }
}
