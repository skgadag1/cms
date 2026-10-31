package com.tbscg.doosan.magnolia.mediatracker.app.domain;

import com.tbscg.doosan.magnolia.mediatracker.app.BaseDomainSubApp;
import com.tbscg.doosan.magnolia.mediatracker.app.MediatrackerNavigator;
import com.tbscg.doosan.magnolia.mediatracker.data.model.Status;
import com.tbscg.doosan.magnolia.mediatracker.data.model.VanityDomain;
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
 * Domain sub app class
 */
public class DomainSubApp extends BaseDomainSubApp<DomainSubAppView> implements DomainSubAppView.Listener {

    @Inject
    public DomainSubApp(SubAppContext subAppContext,
                        DomainSubAppView view,
                        MediatrackerNavigator navigator,
                        VanityDomainService domainService,
                        VanityUrlService vanityUrlService, Shell shell) {

        super(subAppContext, view, navigator, domainService, vanityUrlService, shell);
        view.setListener(this);
    }

    @Override
    public boolean supportsLocation(Location location) {
        DefaultLocation newLocation = (DefaultLocation) location;
        String currentParameter = getCurrentLocation().getParameter();
        return currentParameter.equals(newLocation.getParameter());
    }

    /**
     * Implementation of save domain action
     * @param domain new domain to save
     */
    @Override
    public VanityDomain saveDomain(VanityDomain domain) {
        if (StringUtils.isEmpty(domain.getCode())) {
            domain.setCode(UUID.randomUUID().toString());
        }
        domain.setModifiedTime(OffsetDateTime.now());
        domain.setModifiedBy(MgnlContext.getUser().getName());
        if (domain.getCreationTime() == null) {
            domain.setCreationTime(domain.getModifiedTime());
            domain.setCreatedBy(domain.getModifiedBy());
        }
        if(domain.getStatus() == null) {
            domain.setStatus(Status.DRAFT);
        }
        this.domainService.saveDomain(domain);
        getAppContext().sendLocalMessage(new Message(MessageType.INFO, "Domain saved",
                "Domain " + domain.getFullDomainName() + " saved successfully"));
        return domain;
    }

    @Override
    public void displayMessage(String subject, String message, MessageType msgType) {
        getAppContext().sendLocalMessage(new Message(msgType, subject, message));
    }

    @Override
    protected void returnToGrid(VanityDomain domain, boolean isDeleted) {
        navigator.domainList(domain, isDeleted);
        getSubAppContext().close();
    }

}
