package com.tbscg.doosan.magnolia.mediatracker.app;

import com.tbscg.doosan.magnolia.mediatracker.data.model.Status;
import com.tbscg.doosan.magnolia.mediatracker.data.model.StatusChanged;
import com.tbscg.doosan.magnolia.mediatracker.data.model.VanityDomain;
import com.tbscg.doosan.magnolia.mediatracker.data.model.VanityUrl;
import com.tbscg.doosan.magnolia.mediatracker.data.provider.VanityDomainService;
import com.tbscg.doosan.magnolia.mediatracker.data.provider.VanityUrlService;
import com.tbscg.doosan.magnolia.mediatracker.ui.ConfirmedAction;
import info.magnolia.context.MgnlContext;
import info.magnolia.ui.api.app.SubAppContext;
import info.magnolia.ui.api.message.Message;
import info.magnolia.ui.api.message.MessageType;
import info.magnolia.ui.api.shell.Shell;
import info.magnolia.ui.api.view.View;
import org.apache.commons.lang.StringUtils;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

import static com.tbscg.doosan.magnolia.mediatracker.util.DateFormatUtil.getDateIfBeforeOrCurrent;

/**
 * Base class for domain and domain list sub apps containing common methods
 *
 * @param <T> View implementation used by subApp
 */
public abstract class BaseDomainSubApp<T extends View> extends BaseMediatrackerSubApp<T> {

    protected final VanityDomainService domainService;

    public BaseDomainSubApp(SubAppContext subAppContext,
                            T view,
                            MediatrackerNavigator navigator,
                            VanityDomainService domainService,
                            VanityUrlService urlService,
                            Shell shell) {
        super(subAppContext, view, navigator, urlService, shell);
        this.domainService = domainService;
    }

    /**
     * Method to delete domain
     *
     * @param domain Domain to delete
     * @return confirmation status
     */
    public void deleteDomain(VanityDomain domain) {
        shell.askForConfirmation("Delete domain " + domain.getFullDomainName() + " ?",
                (ConfirmedAction) () -> deleteConfirmedAction(domain));
    }

    private void deleteConfirmedAction(VanityDomain domain) {
        List<VanityUrl> urlsByDomainCode = this.urlService.getUrlsByDomainCode(domain.getCode());
        if (urlsByDomainCode.size() > 0) {
            getAppContext().sendLocalMessage(new Message(MessageType.ERROR, "Unable to delete domain containing URLs.",
                    ""));
        } else {
            domainService.deleteDomain(domain.getCode());
            getAppContext().sendLocalMessage(new Message(MessageType.INFO, "Action finished",
                    "Domain " + domain.getFullDomainName() + " has been deleted successfully."));
        }
        returnToGrid(domain, true);
    }

    public void setDomainStatus(VanityDomain domain, Status status) {
        if (status.equals(Status.ARCHIVED)) {
            shell.askForConfirmation("By clicking OK you archive domain and ALL related urls. Are you sure you want to change the status?",
                    (ConfirmedAction) () -> {
                        VanityDomain changedDomain = processStatusChangeRequest(domain, status);
                        returnToGrid(changedDomain, false);
                    });
        } else {
            VanityDomain changedDomain = processStatusChangeRequest(domain, status);
            returnToGrid(changedDomain, false);
        }
    }

    protected void returnToGrid(VanityDomain domain, boolean isDeleted) {
        refreshGrid(new StatusChanged(domain.getCode(), domain.getModifiedTime(), isDeleted));
    }

    private VanityDomain processStatusChangeRequest(VanityDomain domain, Status status) {
        List<String> resultsMsg = new ArrayList<>();
        VanityDomain d = domainService.getDomain(domain.getCode());
        d.setStatus(status);
        d.setModifiedBy(MgnlContext.getUser().getName());
        d.setModifiedTime(OffsetDateTime.now());
        if (status.equals(Status.APPROVED)) {
            d.setStartDate(OffsetDateTime.now());
            d.setEndDate(null);
            domainService.saveDomain(d);
        } else if (status.equals(Status.ARCHIVED)) {
            d.setEndDate(OffsetDateTime.now());
            domainService.saveDomain(d);
            updateAllAssociatedURLs(d, resultsMsg);
        }
        resultsMsg.add(0, "Status of domain " + domain.getFullDomainName() + " has been changed to " + d.getDisplayedStatus());
        getAppContext().sendLocalMessage(new Message(MessageType.INFO, "Action finished", StringUtils.join(resultsMsg.listIterator(), "\n")));
        return d;
    }

    private void updateAllAssociatedURLs(VanityDomain vanityDomain, List<String> updatedUrls) {
        List<VanityUrl> urlsByDomainCode = this.urlService.getUrlsByDomainCode(vanityDomain.getCode());
        urlsByDomainCode.forEach(url -> {
            if (url.getStatus() != Status.ARCHIVED) {
                url.setStatus(Status.ARCHIVED);
                url.setStartDate(getDateIfBeforeOrCurrent(url.getStartDate()));
                url.setEndDate(getDateIfBeforeOrCurrent(url.getEndDate()));
                this.urlService.createNewOrReplace(url);
                updatedUrls.add("Vanity URL: " + url.getFullURL(vanityDomain) + " status was changed to archived.");
            }
        });
    }
}
