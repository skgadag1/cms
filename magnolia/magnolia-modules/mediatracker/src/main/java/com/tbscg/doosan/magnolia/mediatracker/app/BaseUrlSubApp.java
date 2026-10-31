package com.tbscg.doosan.magnolia.mediatracker.app;

import com.tbscg.doosan.magnolia.mediatracker.data.model.Status;
import com.tbscg.doosan.magnolia.mediatracker.data.model.VanityDomain;
import com.tbscg.doosan.magnolia.mediatracker.data.model.VanityUrl;
import com.tbscg.doosan.magnolia.mediatracker.data.model.*;
import com.tbscg.doosan.magnolia.mediatracker.data.provider.VanityUrlService;
import com.tbscg.doosan.magnolia.mediatracker.ui.ConfirmedAction;
import info.magnolia.context.MgnlContext;
import info.magnolia.ui.api.app.SubAppContext;
import info.magnolia.ui.api.message.Message;
import info.magnolia.ui.api.message.MessageType;
import info.magnolia.ui.api.shell.Shell;
import info.magnolia.ui.api.view.View;

import java.time.OffsetDateTime;
import java.util.Optional;

import static com.tbscg.doosan.magnolia.mediatracker.util.DateFormatUtil.getDateIfBeforeOrCurrent;

/**
 * Base class for url and url list sub apps containing common methods
 *
 * @param <T> View implementation used by subApp
 */
public abstract class BaseUrlSubApp<T extends View> extends BaseMediatrackerSubApp<T> {

    public BaseUrlSubApp(SubAppContext subAppContext,
                         T view,
                         MediatrackerNavigator navigator,
                         VanityUrlService urlService,
                         Shell shell) {
        super(subAppContext, view, navigator, urlService, shell);
    }

    /**
     * Method to delete url
     *  @param url          Url to delete
     * @param vanityDomain Domain that given url belongs to
     * @return confirmation status
     */
    public void deleteUrl(VanityUrl url, VanityDomain vanityDomain) {
        String fullUrlName = vanityDomain != null ? url.getFullURL(vanityDomain) : url.getCode();
        shell.askForConfirmation("Delete Url " + fullUrlName + " ?",
                (ConfirmedAction) () -> deleteConfirmedAction(url, fullUrlName));
    }

    private void deleteConfirmedAction(VanityUrl url, String fullUrlName) {
        urlService.deleteUrl(url.getCode());
        getAppContext().sendLocalMessage(new Message(MessageType.INFO, "Action finished",
                "Url " + fullUrlName + " has been deleted successfully."));
        returnToGrid(url, true);
    }

    protected void returnToGrid(VanityUrl url, boolean isDeleted) {
        refreshGrid(new StatusChanged(url.getCode(), url.getModifiedTime(), isDeleted));
    }

    protected void setUrlStatus(VanityUrl url, Status status, VanityDomain domain) {
        url.setStatus(status);
        url.setModifiedBy(MgnlContext.getUser().getName());
        url.setModifiedTime(OffsetDateTime.now());
        urlService.createNewOrReplace(url);
        getAppContext().sendLocalMessage(new Message(MessageType.INFO,
                "Action finished", "Status of url " + url.getFullURL(domain) + " has been changed to " + url.getDisplayedStatus()));
    }

    protected void setStartEndDates(VanityUrl vu, Status status, OffsetDateTime startDate, OffsetDateTime endDate) {
        OffsetDateTime currentTime = OffsetDateTime.now();
        if (status.equals(Status.APPROVED)) {
            vu.setStartDate(Optional.ofNullable(startDate).orElse(currentTime));
            vu.setEndDate(endDate);
        } else if (status.equals(Status.ARCHIVED)) {
            vu.setStartDate(getDateIfBeforeOrCurrent(startDate));
            vu.setEndDate(getDateIfBeforeOrCurrent(endDate));
        }
    }
}
