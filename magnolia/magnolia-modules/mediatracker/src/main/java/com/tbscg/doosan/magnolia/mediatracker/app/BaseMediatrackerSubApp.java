package com.tbscg.doosan.magnolia.mediatracker.app;

import com.tbscg.doosan.magnolia.mediatracker.data.model.*;
import com.tbscg.doosan.magnolia.mediatracker.data.model.location.ExtendedLocation;
import com.tbscg.doosan.magnolia.mediatracker.data.provider.VanityUrlService;
import info.magnolia.ui.api.app.SubAppContext;
import info.magnolia.ui.api.location.Location;
import info.magnolia.ui.api.shell.Shell;
import info.magnolia.ui.api.view.View;
import info.magnolia.ui.framework.app.BaseSubApp;

import java.util.Optional;

/**
 * Base class for url and domain sub apps containing common methods
 *
 * @param <T> View implementation used by subApp
 */
public abstract class BaseMediatrackerSubApp<T extends View> extends BaseSubApp<T> {

    protected final MediatrackerNavigator navigator;
    protected final VanityUrlService urlService;
    protected final Shell shell;

    public BaseMediatrackerSubApp(SubAppContext subAppContext,
                                  T view,
                                  MediatrackerNavigator navigator,
                                  VanityUrlService urlService,
                                  Shell shell) {
        super(subAppContext, view);
        this.navigator = navigator;
        this.urlService = urlService;
        this.shell = shell;
    }

    protected void refreshGrid(StatusChanged statusChanged) {};

    protected StatusChanged getStatusChanged(Location location) {
        StatusChanged statusChanged = Optional.ofNullable(location)
                .filter(ExtendedLocation.class::isInstance)
                .map(ExtendedLocation.class::cast)
                .map(loc -> loc.getAdditionalParams().get(RequestParameter.STATUS_CHANGED))
                .filter(StatusChanged.class::isInstance)
                .map(StatusChanged.class::cast)
                .orElse(null);
        return statusChanged;
    }
}
