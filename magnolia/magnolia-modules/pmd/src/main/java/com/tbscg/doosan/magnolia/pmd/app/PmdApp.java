package com.tbscg.doosan.magnolia.pmd.app;

import info.magnolia.i18nsystem.SimpleTranslator;
import info.magnolia.ui.api.app.AppContext;
import info.magnolia.ui.api.app.AppView;
import info.magnolia.ui.api.location.Location;
import info.magnolia.ui.framework.app.BaseApp;
import lombok.extern.slf4j.Slf4j;

import javax.inject.Inject;

@Slf4j
public class PmdApp extends BaseApp {

    @Inject
    public PmdApp(AppContext appContext, AppView view, SimpleTranslator i18n) {
        super(appContext, view, i18n);
    }

    @Override
    public void start(Location location) {
        super.start(location);
        log.info("PMD UI started, location: {}", location);
    }

    @Override
    public void stop() {
        super.stop();
        log.info("PMD UI stopped");
    }

    @Override
    public void locationChanged(Location location) {
        super.locationChanged(location);
        log.debug("PMD UI location changed: {}", location);
    }

}
