package com.tbscg.doosan.magnolia.mediatracker.ui;

import info.magnolia.ui.api.shell.ConfirmationHandler;

@FunctionalInterface
public interface ConfirmedAction extends ConfirmationHandler {

    default void onCancel() {
    }
}
