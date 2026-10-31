package com.tbscg.doosan.magnolia.mediatracker.app;

import info.magnolia.i18nsystem.SimpleTranslator;
import info.magnolia.ui.api.app.AppContext;
import info.magnolia.ui.api.app.AppView;
import info.magnolia.ui.api.location.Location;
import info.magnolia.ui.framework.app.BaseApp;
import lombok.extern.slf4j.Slf4j;

import javax.inject.Inject;

@Slf4j
public class MediatrackerApp extends BaseApp {

    @Inject
    public MediatrackerApp(AppContext appContext, AppView view, SimpleTranslator i18n) {
        super(appContext, view, i18n);
    }

    @Override
    public void start(Location location) {
        super.start(location);
        log.info("Mediatracker UI started, location: {}", location);
    }

    @Override
    public void stop() {
        super.stop();
        log.info("Mediatracker UI stopped");
    }

    @Override
    public void locationChanged(Location location) {
        super.locationChanged(location);
        log.debug("Mediatracker UI location changed: {}", location);
    }

}
