package com.tbscg.doosan.magnolia.pmd.app.service;

import com.tbscg.doosan.magnolia.pmd.data.provider.api.ApiService;
import info.magnolia.ui.api.app.SubAppContext;
import info.magnolia.ui.api.location.DefaultLocation;
import info.magnolia.ui.api.location.Location;
import info.magnolia.ui.framework.app.BaseSubApp;

import javax.inject.Inject;

public class ScheduleViewSubApp extends BaseSubApp<ScheduleViewSubAppView> implements ScheduleViewSubAppView.Listener {

    private final ApiService apiService;

    @Inject
    public ScheduleViewSubApp(SubAppContext subAppContext,
                              ScheduleViewSubAppView view,
                              ApiService apiService) {

        super(subAppContext, view);
        this.apiService = apiService;
        view.setListener(this);
    }

    @Override
    public boolean supportsLocation(Location location) {
        DefaultLocation newLocation = (DefaultLocation) location;
        String currentParameter = getCurrentLocation().getParameter();
        return currentParameter.equals(newLocation.getParameter());
    }
}
