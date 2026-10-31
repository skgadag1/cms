package com.tbscg.doosan.magnolia.pmd.app.pmiBrowser;

import com.tbscg.doosan.magnolia.pmd.app.PmdNavigator;
import info.magnolia.ui.api.app.SubAppContext;
import info.magnolia.ui.api.location.Location;
import info.magnolia.ui.api.shell.Shell;
import info.magnolia.ui.framework.app.BaseSubApp;
import lombok.extern.slf4j.Slf4j;

import javax.inject.Inject;

@Slf4j
public class PmiBrowserSubApp extends BaseSubApp<PmiBrowserSubAppView> implements PmiBrowserSubAppView.Listener {

    private final PmdNavigator navigator;
    private final Shell shell;

    @Inject
    public PmiBrowserSubApp(SubAppContext subAppContext,
                                 PmdNavigator navigator,
                                 PmiBrowserSubAppView view,
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
}
