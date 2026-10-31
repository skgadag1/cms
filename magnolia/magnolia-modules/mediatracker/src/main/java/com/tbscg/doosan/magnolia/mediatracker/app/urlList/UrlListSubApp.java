package com.tbscg.doosan.magnolia.mediatracker.app.urlList;

import com.tbscg.doosan.magnolia.mediatracker.app.BaseUrlSubApp;
import com.tbscg.doosan.magnolia.mediatracker.app.MediatrackerNavigator;
import com.tbscg.doosan.magnolia.mediatracker.data.model.*;
import com.tbscg.doosan.magnolia.mediatracker.data.model.request.MediatrackerRequest;
import com.tbscg.doosan.magnolia.mediatracker.data.model.request.ShowRelatedUrlsRequest;
import com.tbscg.doosan.magnolia.mediatracker.data.provider.VanityUrlService;
import info.magnolia.ui.api.app.SubAppContext;
import info.magnolia.ui.api.location.Location;
import info.magnolia.ui.api.shell.Shell;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

/**
 * Url List sub app class
 */
@Slf4j
public class UrlListSubApp extends BaseUrlSubApp<UrlListSubAppView> implements UrlListSubAppView.Listener {

    public UrlListSubApp(SubAppContext subAppContext,
                         UrlListSubAppView view,
                         MediatrackerNavigator navigator,
                         VanityUrlService urlService,
                         Shell shell) {
        super(subAppContext, view, navigator, urlService, shell);
        view.setListener(this);
    }

    @Override
    protected void onSubAppStart() {
        log.info("{} subApp started, location: {}", getSubAppId(), getSubAppContext().getLocation());
    }

    @Override
    public void locationChanged(Location location) {
        super.locationChanged(location);
        log.debug("{} subApp location changed: {}", getSubAppId(), location);
        ShowRelatedUrlsRequest urlsRequest = navigator.getRequestData(ShowRelatedUrlsRequest.class);
        if (urlsRequest != null && StringUtils.isNotBlank(urlsRequest.getCode())) {
            getView().setCodeParam(urlsRequest.getCode());
        }

        MediatrackerRequest mediatrackerRequest = navigator.getRequestData(MediatrackerRequest.class);
        if (mediatrackerRequest.getDivision() != null) {
            getView().setDivision(mediatrackerRequest.getDivision(), getStatusChanged(location));
        }

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
    public void createNewUrl(Division division) {
        this.navigator.urlNew(division);
    }

    @Override
    public void editUrl(VanityUrl url) {
        this.navigator.urlEdit(url.getCode());
    }

    /**
     * Method to update url's status
     *
     * @param url    Url to update
     * @param status Status to set
     * @param domain Domain that given url belongs to
     */
    @Override
    public void setUrlStatus(VanityUrl url, Status status, VanityDomain domain) {
        VanityUrl vu = urlService.getUrl(url.getCode());
        setStartEndDates(vu, status, url.getStartDate(), url.getEndDate());
        super.setUrlStatus(vu, status, domain);
        refreshGrid(new StatusChanged(vu.getCode(), vu.getModifiedTime(), false));
    }

    @Override
    protected void refreshGrid(StatusChanged statusChanged) {
        getView().refreshGrid(statusChanged);
    }

}
