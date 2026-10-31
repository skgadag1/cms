package com.tbscg.doosan.magnolia.pmd.app.schematicBrowser;

import com.tbscg.doosan.magnolia.pmd.app.PmdNavigator;
import com.tbscg.doosan.magnolia.pmd.data.model.Schematic;
import info.magnolia.ui.api.app.SubAppContext;
import info.magnolia.ui.api.location.Location;
import info.magnolia.ui.api.shell.Shell;
import info.magnolia.ui.framework.app.BaseSubApp;
import lombok.extern.slf4j.Slf4j;

import javax.inject.Inject;

@Slf4j
public class SchematicBrowserSubApp extends BaseSubApp<SchematicBrowserSubAppView> implements SchematicBrowserSubAppView.Listener {

    private final PmdNavigator navigator;
    private final Shell shell;

    @Inject
    public SchematicBrowserSubApp(SubAppContext subAppContext,
                                  PmdNavigator navigator,
                                  SchematicBrowserSubAppView view,
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
    public void viewSchematic(Schematic schematic) {
        this.navigator.schematicView(schematic);
    }
}
