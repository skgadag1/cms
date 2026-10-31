package com.tbscg.doosan.magnolia.dxp.workflow.action.ui.datasources;

import com.tbscg.doosan.magnolia.dxp.workflow.action.ui.validators.RegionalActionNodeValidator;
import com.vaadin.shared.ui.grid.DropLocation;
import com.vaadin.ui.Notification;
import info.magnolia.ui.contentapp.Datasource;
import info.magnolia.ui.datasource.jcr.JcrNodeWrapper;

import java.util.Collection;
import java.util.stream.Stream;

public class RestrictedMoveItemsDatasource<T extends JcrNodeWrapper> implements Datasource<T> {
    private final RegionalActionNodeValidator<T> moveValidator;
    private final Datasource<T> datasource;

    public RestrictedMoveItemsDatasource(Datasource<T> datasource, Collection<String> allowedRoles) {
        this.datasource = datasource;
        this.moveValidator = new RegionalActionNodeValidator<>(allowedRoles);
    }

    @Override
    public void commit(T t) {
        datasource.commit(t);
    }

    @Override
    public void moveItems(Collection<T> items, T target, DropLocation dropLocation) {
        if (!this.moveValidator.validate(items.stream(), Stream.of(target))) {
            Notification.show("Only admins can MOVE between regions", Notification.Type.TRAY_NOTIFICATION).setDelayMsec(3600);
            return;
        }

        datasource.moveItems(items, target, dropLocation);
    }

    @Override
    public void remove(T t) {
        datasource.remove(t);
    }
}
