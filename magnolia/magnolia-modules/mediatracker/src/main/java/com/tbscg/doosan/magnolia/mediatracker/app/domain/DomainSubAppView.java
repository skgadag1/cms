package com.tbscg.doosan.magnolia.mediatracker.app.domain;

import com.tbscg.doosan.magnolia.mediatracker.data.model.Status;
import com.tbscg.doosan.magnolia.mediatracker.data.model.VanityDomain;
import info.magnolia.ui.api.message.MessageType;
import info.magnolia.ui.api.view.View;

/**
 * Domain Sub App view interface
 */
public interface DomainSubAppView extends View {

    void setListener(Listener listener);

    interface Listener {
        VanityDomain saveDomain(VanityDomain domain);
        void displayMessage(String subject, String message, MessageType msgType);
        void deleteDomain(VanityDomain domain);
        void setDomainStatus(VanityDomain domain, Status status);
    }
}
