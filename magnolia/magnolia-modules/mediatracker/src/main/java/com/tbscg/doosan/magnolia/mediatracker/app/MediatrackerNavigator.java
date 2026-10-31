package com.tbscg.doosan.magnolia.mediatracker.app;

import com.tbscg.doosan.magnolia.mediatracker.data.model.*;
import com.tbscg.doosan.magnolia.mediatracker.data.model.location.ExtendedLocation;
import com.tbscg.doosan.magnolia.mediatracker.data.model.request.EditRequest;
import com.tbscg.doosan.magnolia.mediatracker.data.model.request.MediatrackerRequest;
import com.tbscg.doosan.magnolia.mediatracker.data.model.request.SaveRequest;
import com.tbscg.doosan.magnolia.mediatracker.data.model.request.ShowRelatedUrlsRequest;
import com.tbscg.doosan.magnolia.mediatracker.util.ObjectEncodeUtils;
import info.magnolia.ui.api.location.DefaultLocation;
import info.magnolia.ui.api.location.Location;
import info.magnolia.ui.api.location.LocationController;

import javax.inject.Inject;
import java.util.Collections;

/**
 * Class for navigating between sub apps.
 */
public class MediatrackerNavigator {

    private final LocationController locationController;

    private enum SubApp {
        domain,
        domainList,
        url,
        urlList
    }

    @Inject
    public MediatrackerNavigator(LocationController locationController) {
        this.locationController = locationController;
    }

    /**
     * Navigates to new location with provided request params
     * @param requestData params to pass
     */
    public void setParameter(MediatrackerRequest requestData) {
        Location location = locationController.getWhere();
        locationController.goTo(new DefaultLocation(
                location.getAppType(),
                location.getAppName(),
                location.getSubAppId(),
                ObjectEncodeUtils.encode(requestData)));
    }

    /**
     * Navigates to new location with provided request params
     * @param requestData params to pass
     */
    public void setParameter(ShowRelatedUrlsRequest requestData) {
        Location location = locationController.getWhere();
        locationController.goTo(new DefaultLocation(
                location.getAppType(),
                location.getAppName(),
                location.getSubAppId(),
                ObjectEncodeUtils.encode(requestData)));
    }

    /**
     * Navigates user to domain list sub app
     * @param division parameter to pass
     */
    public void domainList(Division division) {
        navigate(SubApp.domainList, new MediatrackerRequest(division));
    }

    /**
     * Navigates user to domain list sub app
     *
     * @param domain saved domain
     */
    public void domainList(VanityDomain domain, boolean isDeleted) {
        navigate(SubApp.domainList, new SaveRequest(domain, isDeleted));
    }

    /**
     * Navigates user to domain sub app
     * @param division parameter to pass
     */
    public void domainNew(Division division) {
        navigate(SubApp.domain, new EditRequest(division, null));
    }

    /**
     * Navigates user to domain sub app
     * @param domain parameter to pass
     */
    public void domainEdit(VanityDomain domain) {
        navigate(SubApp.domain, new EditRequest(domain));
    }

    /**
     * Navigates user to url list sub app
     * @param division value of division parameter
     */
    public void urlList(Division division) {
        navigate(SubApp.urlList, new MediatrackerRequest(division));
    }

    /**
     * Navigates user to url list sub app
     *
     * @param url saved url
     */
    public void urlList(VanityUrl url, boolean isDeleted) {
        navigate(SubApp.urlList, new SaveRequest(url, isDeleted));
    }

    /**
     * Navigates user to url list sub app
     *
     * @param domain value of domain parameter
     */
    public void urlList(VanityDomain domain) {
        navigate(SubApp.urlList, new ShowRelatedUrlsRequest(domain));
    }

    /**
     * Navigates user to url sub app
     * @param division value of division parameter
     */
    public void urlNew(Division division) {
        navigate(SubApp.url, new EditRequest(division, null));
    }

    /**
     * Navigates user to url sub app
     * @param code value of url code parameter
     */
    public void urlEdit(String code) {
        navigate(SubApp.url, new EditRequest(null, code));
    }

    /**
     * Extract parameter from URL
     */
    public <C> C getRequestData(Class<C> dataClass) {
        String parameter = locationController.getWhere().getParameter();
        return ObjectEncodeUtils.decode(parameter, dataClass);
    }

    private void navigate(SubApp subApp, SaveRequest requestData) {
        MediatrackerRequest request = new MediatrackerRequest(requestData.getDivision());
        Location location = locationController.getWhere();
        locationController.goTo(new ExtendedLocation(
                location.getAppType(),
                location.getAppName(),
                subApp.name(),
                ObjectEncodeUtils.encode(request),
                Collections.singletonMap(
                        RequestParameter.STATUS_CHANGED,
                        new StatusChanged(
                                requestData.getCode(),
                                requestData.getLastUpdatedTime(),
                                requestData.getDeleted())
                )));
    }

    private void navigate(SubApp subApp, MediatrackerRequest requestData) {
        Location location = locationController.getWhere();
        locationController.goTo(new DefaultLocation(
                location.getAppType(),
                location.getAppName(),
                subApp.name(),
                ObjectEncodeUtils.encode(requestData)));
    }

    private void navigate(SubApp subApp, ShowRelatedUrlsRequest requestData) {
        Location location = locationController.getWhere();
        locationController.goTo(new DefaultLocation(
                location.getAppType(),
                location.getAppName(),
                subApp.name(),
                ObjectEncodeUtils.encode(requestData)));
    }

}
