package com.tbscg.doosan.magnolia.pmd.app.service;

import com.tbscg.doosan.magnolia.pmd.app.PmdNavigator;
import com.tbscg.doosan.magnolia.pmd.data.model.Schedule;
import com.tbscg.doosan.magnolia.pmd.data.model.request.ScheduleViewRequest;
import com.tbscg.doosan.magnolia.pmd.data.provider.api.ApiService;
import com.vaadin.ui.Component;
import com.vaadin.ui.Panel;
import com.vaadin.ui.VerticalLayout;
import info.magnolia.ui.api.app.SubAppContext;

import javax.inject.Inject;

public class ScheduleViewSubAppViewImpl implements ScheduleViewSubAppView {

    private final ScheduleView scheduleView;
    private Listener listener;

    @Inject
    public ScheduleViewSubAppViewImpl(
            SubAppContext ctx,
            ApiService apiService,
            PmdNavigator navigator) {

        ScheduleViewRequest request = navigator.getRequestData(ScheduleViewRequest.class);
        Schedule schedule = apiService.getSchedule(request.getId());
        this.scheduleView = new ScheduleView(apiService, schedule);
    }

    @Override
    public void setListener(Listener listener) {
        this.listener = listener;
    }

    @Override
    public Component asVaadinComponent() {
        return this.scheduleView;
    }
}
