package com.tbscg.doosan.magnolia.mediatracker.app.url;

import com.tbscg.doosan.magnolia.mediatracker.app.BaseUrlSubApp;
import com.tbscg.doosan.magnolia.mediatracker.app.MediatrackerNavigator;
import com.tbscg.doosan.magnolia.mediatracker.data.model.Status;
import com.tbscg.doosan.magnolia.mediatracker.data.model.VanityDomain;
import com.tbscg.doosan.magnolia.mediatracker.data.model.VanityUrl;
import com.tbscg.doosan.magnolia.mediatracker.data.provider.VanityDomainService;
import com.tbscg.doosan.magnolia.mediatracker.data.provider.VanityUrlService;
import info.magnolia.context.MgnlContext;
import info.magnolia.ui.api.app.SubAppContext;
import info.magnolia.ui.api.location.DefaultLocation;
import info.magnolia.ui.api.location.Location;
import info.magnolia.ui.api.message.Message;
import info.magnolia.ui.api.message.MessageType;
import info.magnolia.ui.api.shell.Shell;
import org.apache.commons.lang3.StringUtils;

import javax.inject.Inject;
import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * Url sub app class
 */
public class UrlSubApp extends BaseUrlSubApp<UrlSubAppView> implements UrlSubAppView.Listener {

    @Inject
    public UrlSubApp(SubAppContext subAppContext,
                     UrlSubAppView view,
                     MediatrackerNavigator navigator,
                     VanityUrlService urlService,
                     Shell shell,
                     VanityDomainService vanityDomainService) {

        super(subAppContext, view, navigator, urlService, shell);
        view.setListener(this);
    }

    /**
     * Method to update url's status
     *
     * @param url Url to update
     * @param status Status to set
     * @param domain Domain that given url belongs to
     */
    @Override
    public void setUrlStatus(VanityUrl url, Status status, VanityDomain domain) {
        setStartEndDates(url, status, url.getStartDate(), url.getEndDate());
        super.setUrlStatus(url, status, domain);
    }

    @Override
    public boolean supportsLocation(Location location) {
        DefaultLocation newLocation = (DefaultLocation) location;
        String currentParameter = getCurrentLocation().getParameter();
        return currentParameter.equals(newLocation.getParameter());
    }

    @Override
    public VanityUrl saveUrl(VanityUrl url) {
        String userName = MgnlContext.getUser().getName();
        OffsetDateTime now = OffsetDateTime.now();
        if(isNewlyCreated(url)) {
            url.setCode(UUID.randomUUID().toString());
            url.setCreationTime(now);
            url.setCreatedBy(userName);
        } else {
            url.setModifiedBy(userName);
        }
        url.setModifiedTime(now);
        this.urlService.createNewOrReplace(url);
        getAppContext().sendLocalMessage(
                new Message(MessageType.INFO, "URL saved","URL " + url.getFullURL() + " saved successfully"));
        return url;
    }

    @Override
    public void displayMessage(String subject, String message, MessageType msgType) {
        getAppContext().sendLocalMessage(new Message(msgType, subject, message));
    }

    private boolean isNewlyCreated(VanityUrl url) {
        return StringUtils.isEmpty(url.getCode());
    }

    @Override
    protected void returnToGrid(VanityUrl url, boolean isDeleted) {
        navigator.urlList(url, isDeleted);
        getSubAppContext().close();
    }
}
