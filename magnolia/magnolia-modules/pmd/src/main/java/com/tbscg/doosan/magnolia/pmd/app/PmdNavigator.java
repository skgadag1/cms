package com.tbscg.doosan.magnolia.pmd.app;

import com.tbscg.doosan.magnolia.pmd.data.model.Document;
import com.tbscg.doosan.magnolia.pmd.data.model.Schedule;
import com.tbscg.doosan.magnolia.pmd.data.model.Schematic;
import com.tbscg.doosan.magnolia.pmd.data.model.request.DocumentViewRequest;
import com.tbscg.doosan.magnolia.pmd.data.model.request.PmdRequest;
import com.tbscg.doosan.magnolia.pmd.data.model.request.ScheduleViewRequest;
import com.tbscg.doosan.magnolia.pmd.data.model.request.SchematicViewRequest;
import com.tbscg.doosan.magnolia.pmd.util.ObjectEncodeUtils;
import info.magnolia.ui.api.location.DefaultLocation;
import info.magnolia.ui.api.location.Location;
import info.magnolia.ui.api.location.LocationController;

import javax.inject.Inject;

public class PmdNavigator {

    private final LocationController locationController;

    private enum SubApp {
        documentBrowser,
        documentView,
        schematicBrowser,
        schematicView,
        pmiBrowser,
        scheduleBrowser,
        scheduleView
    }

    @Inject
    public PmdNavigator(LocationController locationController) {
        this.locationController = locationController;
    }

    public void setParameter(PmdRequest requestData) {
        Location location = locationController.getWhere();
        locationController.goTo(new DefaultLocation(
                location.getAppType(),
                location.getAppName(),
                location.getSubAppId(),
                ObjectEncodeUtils.encode(requestData)));
    }

    public void documentView(Document document) {
        navigate(SubApp.documentView, new DocumentViewRequest(document.getId()));
    }

    public void schematicView(Schematic schematic) {
        navigate(SubApp.schematicView, new SchematicViewRequest(schematic.getId()));
    }

    public void scheduleView(Schedule schedule) {
        navigate(SubApp.scheduleView, new ScheduleViewRequest(schedule.getId()));
    }

    public <C> C getRequestData(Class<C> dataClass) {
        String parameter = locationController.getWhere().getParameter();
        return ObjectEncodeUtils.decode(parameter, dataClass);
    }

    private void navigate(SubApp subApp, PmdRequest requestData) {
        Location location = locationController.getWhere();
        locationController.goTo(new DefaultLocation(
                location.getAppType(),
                location.getAppName(),
                subApp.name(),
                ObjectEncodeUtils.encode(requestData)));
    }
}
