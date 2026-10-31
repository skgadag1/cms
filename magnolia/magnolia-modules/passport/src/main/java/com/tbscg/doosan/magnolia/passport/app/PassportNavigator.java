package com.tbscg.doosan.magnolia.passport.app;

import com.tbscg.doosan.magnolia.passport.data.model.PassportDocument;
import com.tbscg.doosan.magnolia.passport.data.model.request.PassportDocumentViewRequest;
import com.tbscg.doosan.magnolia.passport.data.model.request.PassportRequest;
import com.tbscg.doosan.magnolia.passport.util.ObjectEncodeUtils;
import info.magnolia.ui.api.location.DefaultLocation;
import info.magnolia.ui.api.location.Location;
import info.magnolia.ui.api.location.LocationController;
import lombok.extern.slf4j.Slf4j;

import javax.inject.Inject;

@Slf4j
public class PassportNavigator {

    private final LocationController locationController;

    private enum SubApp {
        documentBrowser,
        documentView
    }

    @Inject
    public PassportNavigator(LocationController locationController) {
        this.locationController = locationController;
    }

    public void documentView(PassportDocument document) {
        navigate(SubApp.documentView, new PassportDocumentViewRequest(document.getId()));
    }

    public <C> C getRequestData(Class<C> dataClass) {
        String parameter = locationController.getWhere().getParameter();
        return ObjectEncodeUtils.decode(parameter, dataClass);
    }

    private void navigate(SubApp subApp, PassportRequest requestData) {
        Location location = locationController.getWhere();

        DefaultLocation dl = new DefaultLocation(
                location.getAppType(),
                location.getAppName(),
                subApp.name(),
                ObjectEncodeUtils.encode(requestData));

        locationController.goTo(dl);
    }
}
