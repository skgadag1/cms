package com.tbscg.doosan.magnolia.passport.app.documentView;

import com.tbscg.doosan.magnolia.passport.data.provider.api.ApiService;
import info.magnolia.ui.api.app.SubAppContext;
import info.magnolia.ui.api.location.DefaultLocation;
import info.magnolia.ui.api.location.Location;
import info.magnolia.ui.framework.app.BaseSubApp;
import lombok.extern.slf4j.Slf4j;

import javax.inject.Inject;

@Slf4j
public class DocumentViewSubApp extends BaseSubApp<DocumentViewSubAppView> implements DocumentViewSubAppView.Listener {

    private final ApiService apiService;

    @Inject
    public DocumentViewSubApp(SubAppContext subAppContext,
                              DocumentViewSubAppView view,
                              ApiService apiService) {

        super(subAppContext, view);
        log.info("Creating instance - DocumentViewSubApp");
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
