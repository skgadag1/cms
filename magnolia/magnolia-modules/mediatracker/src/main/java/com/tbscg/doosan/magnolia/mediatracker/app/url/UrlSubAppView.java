package com.tbscg.doosan.magnolia.mediatracker.app.url;

import com.tbscg.doosan.magnolia.mediatracker.data.model.Status;
import com.tbscg.doosan.magnolia.mediatracker.data.model.VanityDomain;
import com.tbscg.doosan.magnolia.mediatracker.data.model.VanityUrl;
import info.magnolia.ui.api.message.MessageType;
import info.magnolia.ui.api.view.View;

/**
 * Url Sub App view interface
 */
public interface UrlSubAppView extends View {

    void setListener(Listener listener);

    interface Listener {
        VanityUrl saveUrl(VanityUrl url);
        void displayMessage(String subject, String message, MessageType msgType);
        void deleteUrl(VanityUrl url, VanityDomain domain);
        void setUrlStatus(VanityUrl url, Status status, VanityDomain domain);
    }
}
