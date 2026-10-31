package com.tbscg.doosan.magnolia.mediatracker.app.urlList;

import com.tbscg.doosan.magnolia.mediatracker.data.model.*;
import info.magnolia.ui.api.view.View;

/**
 * Url List Sub App view interface
 */
public interface UrlListSubAppView extends View {

    void setListener(Listener listener);
    void setDivision(Division division, StatusChanged statusChanged);
    void setCodeParam(String code);
    void refreshGrid(StatusChanged statusChanged);

    interface Listener {
        void updateLocation(Division division);
        void createNewUrl(Division division);
        void editUrl(VanityUrl url);
        void deleteUrl(VanityUrl url, VanityDomain domain);
        void setUrlStatus(VanityUrl url, Status status, VanityDomain domain);
    }
}
