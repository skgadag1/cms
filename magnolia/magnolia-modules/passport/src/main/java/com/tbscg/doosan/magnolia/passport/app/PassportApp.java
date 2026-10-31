package com.tbscg.doosan.magnolia.passport.app;

import info.magnolia.i18nsystem.SimpleTranslator;
import info.magnolia.ui.api.app.AppContext;
import info.magnolia.ui.api.app.AppView;
import info.magnolia.ui.api.location.Location;
import info.magnolia.ui.framework.app.BaseApp;
import lombok.extern.slf4j.Slf4j;

import javax.inject.Inject;

@Slf4j
public class PassportApp extends BaseApp {

    @Inject
    public PassportApp(AppContext appContext, AppView view, SimpleTranslator i18n) {
        super(appContext, view, i18n);
    }

    @Override
    public void start(Location location) {
        super.start(location);
        log.info("PASSPORT UI started, location: {}", location);
    }

    @Override
    public void stop() {
        super.stop();
        log.info("PASSPORT UI stopped");
    }

    @Override
    public void locationChanged(Location location) {
        super.locationChanged(location);
        log.debug("PASSPORT UI location changed: {}", location);
    }
}
