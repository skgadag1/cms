package com.tbscg.doosan.magnolia.mediatracker.app.url;

import com.tbscg.doosan.magnolia.mediatracker.app.MediatrackerNavigator;
import com.tbscg.doosan.magnolia.mediatracker.data.model.Status;
import com.tbscg.doosan.magnolia.mediatracker.data.model.VanityDomain;
import com.tbscg.doosan.magnolia.mediatracker.data.model.VanityUrl;
import com.tbscg.doosan.magnolia.mediatracker.data.model.request.EditRequest;
import com.tbscg.doosan.magnolia.mediatracker.data.provider.VanityDomainService;
import com.tbscg.doosan.magnolia.mediatracker.data.provider.VanityUrlService;
import com.tbscg.doosan.magnolia.mediatracker.error.HtmlFormattedMessageException;
import com.tbscg.doosan.magnolia.mediatracker.ui.ButtonBar;
import com.vaadin.icons.VaadinIcons;
import com.vaadin.server.Sizeable;
import com.vaadin.shared.ui.ContentMode;
import com.vaadin.ui.*;
import info.magnolia.ui.api.app.SubAppContext;
import info.magnolia.ui.api.message.MessageType;
import org.apache.commons.lang3.StringUtils;

import javax.inject.Inject;
import java.time.LocalDate;
import java.util.*;

/**
 * Url Sub App view implementation
 */
public class UrlSubAppViewImpl implements UrlSubAppView {

    private final VerticalLayout layout = new VerticalLayout();
    private final VanityUrlService urlService;
    private Listener listener;

    @Inject
    public UrlSubAppViewImpl(
            SubAppContext ctx,
            VanityDomainService domainService,
            VanityUrlService urlService,
            MediatrackerNavigator navigator) {

        this.urlService = urlService;
        EditRequest request = navigator.getRequestData(EditRequest.class);
        VanityUrl url;
        List<VanityDomain> domains;

        List<Component> cl;
        try {
            final UrlForm editForm;

            if (StringUtils.isEmpty(request.getCode())) {
                url = new VanityUrl();
                url.setRegion(null);
                url.setDivision(request.getDivision());
                domains = domainService.getDomains(request.getDivision());
                editForm = new UrlForm("Create New URL", url, domains, false);
            } else {
                url = urlService.getUrl(request.getCode());
                domains = domainService.getDomains(url.getDivision());
                editForm = new UrlForm("Edit URL", url, domains, true);
            }

            final ButtonBar buttonBar = createButtonBar(ctx, navigator, request, url, editForm);

            cl = Arrays.asList(editForm, buttonBar);
        } catch (HtmlFormattedMessageException e) {
            final Panel error = new Panel("Error", new Label(e.getMessage(), ContentMode.HTML));
            cl = Collections.singletonList(error);
        } catch (Exception e) {
            throw e;
        }

        layout.setWidth(100, Sizeable.Unit.PERCENTAGE);
        layout.setMargin(true);
        layout.setSpacing(true);
        cl.forEach(layout::addComponent);
    }

    private ButtonBar createButtonBar(SubAppContext ctx, MediatrackerNavigator navigator, EditRequest request, VanityUrl url, UrlForm editForm) {
        Button saveButton = new Button("Save", event -> {
            saveUrlEvent(ctx, navigator, editForm, null);
        });
        saveButton.setIcon(VaadinIcons.CHECK_CIRCLE_O);

        Button cancelButton = new Button("Cancel", event -> {
            navigator.urlList(url.getDivision());
            ctx.close();
        });
        cancelButton.setIcon(VaadinIcons.CLOSE_CIRCLE_O);

        Button publishButton = new Button("Publish", event -> {
            saveUrlEvent(ctx, navigator, editForm, Status.APPROVED);
        });
        publishButton.setIcon(VaadinIcons.PLUS_CIRCLE_O);

        Button archiveButton = new Button("Archive", event -> {
            saveUrlEvent(ctx, navigator, editForm, Status.ARCHIVED);
        });
        archiveButton.setIcon(VaadinIcons.ARCHIVE);

        VanityDomain domain = editForm.getDomainComboBox().getValue();

        Button deleteButton = new Button("Delete", event -> {
            listener.deleteUrl(url, domain);
        });
        deleteButton.setIcon(VaadinIcons.MINUS_CIRCLE_O);

        List<Component> buttons = new ArrayList<>();
        buttons.add(saveButton);
        buttons.add(cancelButton);

        if (StringUtils.isNotEmpty(request.getCode())) {
            if (url.getStatus() != Status.APPROVED && domain.getStatus() != Status.ARCHIVED
                    && domain.getStatus() != Status.DRAFT) {
                buttons.add(publishButton);
            }
            if (url.getStatus() == Status.APPROVED) {
                buttons.add(archiveButton);
            }
            if (url.getStatus() != Status.APPROVED) {
                buttons.add(deleteButton);
            }
        }

        final ButtonBar buttonBar = new ButtonBar(buttons.stream().toArray(Button[]::new));
        return buttonBar;
    }

    private void saveUrlEvent(SubAppContext ctx, MediatrackerNavigator navigator, UrlForm editForm, Status status) {
        String msg = validateForm(editForm);
        if (StringUtils.isBlank(msg)) {
            VanityUrl urlToSave = editForm.getEditedUrl();
            if (status != null) {
                listener.setUrlStatus(urlToSave, status, editForm.getDomainComboBox().getValue());
            } else {
                urlToSave = listener.saveUrl(urlToSave);
            }
            navigator.urlList(urlToSave, false);
            ctx.close();
        } else {
            listener.displayMessage("Invalid data", msg, MessageType.ERROR);
        }
    }

    @Override
    public void setListener(Listener listener) {
        this.listener = listener;
    }

    @Override
    public Component asVaadinComponent() {
        Panel panel = new Panel(layout);
        layout.setWidth(100, Sizeable.Unit.PERCENTAGE);
        return panel;
    }

    private String validateForm(UrlForm form) {
        if (form.getDomainComboBox().getSelectedItem().orElse(null) == null) {
            return "Domain shouldn't be empty";
        }
        if (form.getRegionComboBox().getSelectedItem().orElse(null) == null) {
            return "Region shouldn't be empty";
        }
        if (form.getEndDate().getValue() != null && form.getEndDate().getValue()
                .isBefore(Optional.ofNullable(form.getStartDate().getValue())
                        .orElse(LocalDate.now()))) {
            return "End date shouldn't be before start date";
        }
        if (StringUtils.isBlank(form.getPath().getValue())) {
            return "Path shouldn't be empty";
        }
        if (!form.getDeviceDependentCheckBox().getValue() && StringUtils.isBlank(form.getRedirectLocation().getValue())) {
            return "Redirect location can't be empty";
        }
        if (form.getDeviceDependentCheckBox().getValue() && form.getDeviceDependentComponents().stream()
                .filter(e -> e instanceof TextField)
                .anyMatch(e -> StringUtils.isBlank(((TextField) e).getValue()))) {

            return "Make sure that redirects are provided for each device";
        }
        if (!urlService.isUrlPathNotTaken(form.getEditedUrl())) {
            return "Url already exist.";
        }
        return null;
    }
}
