package com.tbscg.doosan.magnolia.pmd.app.service;

import info.magnolia.ui.api.view.View;

public interface ScheduleViewSubAppView extends View {

    void setListener(Listener listener);

    interface Listener {
    }
}
