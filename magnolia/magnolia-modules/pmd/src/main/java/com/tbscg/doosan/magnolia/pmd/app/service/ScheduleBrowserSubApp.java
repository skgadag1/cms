package com.tbscg.doosan.magnolia.pmd.app.service;

import com.tbscg.doosan.magnolia.pmd.app.PmdNavigator;
import com.tbscg.doosan.magnolia.pmd.data.model.Schedule;
import info.magnolia.ui.api.app.SubAppContext;
import info.magnolia.ui.api.location.Location;
import info.magnolia.ui.api.shell.Shell;
import info.magnolia.ui.framework.app.BaseSubApp;
import lombok.extern.slf4j.Slf4j;

import javax.inject.Inject;

@Slf4j
public class ScheduleBrowserSubApp extends BaseSubApp<ScheduleBrowserSubAppView> implements ScheduleBrowserSubAppView.Listener {

    private final PmdNavigator navigator;
    private final Shell shell;

    @Inject
    public ScheduleBrowserSubApp(SubAppContext subAppContext,
                                  PmdNavigator navigator,
                                  ScheduleBrowserSubAppView view,
                                  Shell shell) {

        super(subAppContext, view);
        view.setListener(this);
        this.navigator = navigator;
        this.shell = shell;
    }

    @Override
    protected void onSubAppStart() {
        log.info("{} subApp started, location: {}", getSubAppId(), getSubAppContext().getLocation());
    }

    @Override
    public void locationChanged(Location location) {
        super.locationChanged(location);
        log.info("{} subApp location changed: {}", getSubAppId(), location);
    }

    @Override
    public boolean supportsLocation(Location location) {
        return true;
    }

    @Override
    public void viewSchedule(Schedule schedule) {
        this.navigator.scheduleView(schedule);
    }
}
