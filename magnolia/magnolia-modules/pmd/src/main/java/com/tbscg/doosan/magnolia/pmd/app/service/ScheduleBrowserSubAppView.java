package com.tbscg.doosan.magnolia.pmd.app.service;

import com.tbscg.doosan.magnolia.pmd.data.model.Schedule;
import info.magnolia.ui.api.view.View;

public interface ScheduleBrowserSubAppView extends View {

    void setListener(Listener listener);

    interface Listener {
        void viewSchedule(Schedule schedule);
    }
}
