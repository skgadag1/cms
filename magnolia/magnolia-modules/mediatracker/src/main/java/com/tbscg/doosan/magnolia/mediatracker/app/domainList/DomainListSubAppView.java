package com.tbscg.doosan.magnolia.mediatracker.app.domainList;

import com.tbscg.doosan.magnolia.mediatracker.data.model.*;
import info.magnolia.ui.api.view.View;

/**
 * Domain List Sub App view interface
 */
public interface DomainListSubAppView extends View {

    void setListener(Listener listener);
    void setDivision(Division division, StatusChanged statusChanged);
    void refreshGrid(StatusChanged statusChanged);

    interface Listener {
        void updateLocation(Division division);
        void createNewDomain(Division division);
        void editDomain(VanityDomain domain);
        void deleteDomain(VanityDomain domain);
        void setDomainStatus(VanityDomain domain, Status status);
        void showRelatedUrls(VanityDomain domain);
    }
}
