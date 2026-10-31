package com.tbscg.doosan.magnolia.mediatracker.app.domain;

import com.tbscg.doosan.magnolia.mediatracker.app.MediatrackerNavigator;
import com.tbscg.doosan.magnolia.mediatracker.data.model.Status;
import com.tbscg.doosan.magnolia.mediatracker.data.model.VanityDomain;
import com.tbscg.doosan.magnolia.mediatracker.data.model.request.EditRequest;
import com.tbscg.doosan.magnolia.mediatracker.data.provider.VanityDomainService;
import com.tbscg.doosan.magnolia.mediatracker.ui.ButtonBar;
import com.vaadin.icons.VaadinIcons;
import com.vaadin.server.Sizeable;
import com.vaadin.ui.Button;
import com.vaadin.ui.Component;
import com.vaadin.ui.Panel;
import com.vaadin.ui.VerticalLayout;
import info.magnolia.ui.api.app.SubAppContext;
import info.magnolia.ui.api.message.MessageType;
import org.apache.commons.lang3.StringUtils;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;

/**
 * Domain Sub App view implementation
 */
public class DomainSubAppViewImpl implements DomainSubAppView {

    private static final String DOMAIN_NAME_PATTERN = "^([a-zA-Z0-9-]{1,61}[a-zA-Z0-9]\\.)+[a-zA-Z]{2,}$";

    private final VanityDomainService domainService;
    private final VerticalLayout layout = new VerticalLayout();
    private final DomainForm editForm;
    private Listener listener;

    @Inject
    public DomainSubAppViewImpl(
            SubAppContext ctx,
            VanityDomainService domainService,
            MediatrackerNavigator navigator) {

        this.domainService = domainService;
        EditRequest request = navigator.getRequestData(EditRequest.class);
        VanityDomain domain;
        if (StringUtils.isEmpty(request.getCode())) {
            domain = new VanityDomain();
            domain.setDivision(request.getDivision());
            editForm = new DomainForm("Create New Domain", domain, false);
        } else {
            domain = domainService.getDomain(request.getCode());
            editForm = new DomainForm("Edit Domain", domain, true);
        }

        final ButtonBar buttonBar = createButtonBar(ctx, navigator, domain, request);

        layout.setWidth(100, Sizeable.Unit.PERCENTAGE);
        layout.setMargin(true);
        layout.setSpacing(true);
        layout.addComponent(this.editForm);
        layout.addComponent(buttonBar);
    }

    private ButtonBar createButtonBar(SubAppContext ctx, MediatrackerNavigator navigator, VanityDomain domain, EditRequest request) {
        Button saveButton = new Button("Save", event -> {
            saveDomainEvent(ctx, navigator, null);
        });
        saveButton.setIcon(VaadinIcons.CHECK_CIRCLE_O);

        Button cancelButton = new Button("Cancel", event -> {
            navigator.domainList(domain.getDivision());
            ctx.close();
        });
        cancelButton.setIcon(VaadinIcons.CLOSE_CIRCLE_O);


        Button publishButton = new Button("Publish", event -> {
            saveDomainEvent(ctx, navigator, Status.APPROVED);
        });
        publishButton.setIcon(VaadinIcons.PLUS_CIRCLE_O);

        Button archiveButton = new Button("Archive", event -> {
            saveDomainEvent(ctx, navigator, Status.ARCHIVED);
        });
        archiveButton.setIcon(VaadinIcons.ARCHIVE);

        Button deleteButton = new Button("Delete", event -> {
            listener.deleteDomain(domain);
        });
        deleteButton.setIcon(VaadinIcons.MINUS_CIRCLE_O);

        List<Component> buttons = new ArrayList<>();
        buttons.add(saveButton);
        buttons.add(cancelButton);

        if (StringUtils.isNotEmpty(request.getCode())) {
            if (domain.getStatus() != Status.APPROVED) {
                buttons.add(publishButton);
            }
            if (domain.getStatus() == Status.APPROVED) {
                buttons.add(archiveButton);
            }
            if (domain.getStatus() != Status.APPROVED) {
                buttons.add(deleteButton);
            }
        }

        final ButtonBar buttonBar = new ButtonBar(buttons.stream().toArray(Button[]::new));
        return buttonBar;
    }

    private void saveDomainEvent(SubAppContext ctx, MediatrackerNavigator navigator, Status status) {
        String msg = validateForm(editForm);
        if (StringUtils.isBlank(msg)) {
            VanityDomain domainToSave = editForm.getEditedDomain();
            if (status != null) {
                listener.setDomainStatus(domainToSave, status);
            } else {
                domainToSave = listener.saveDomain(domainToSave);
            }
            domainToSave.setStatus(status);
            if (!Status.ARCHIVED.equals(status)) {
                navigator.domainList(domainToSave, false);
                ctx.close();
            }
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

    private String validateForm(DomainForm form) {
        VanityDomain domain = form.getEditedDomain();
        if (domain == null || StringUtils.isBlank(domain.getFullDomainName())) {
            return "Domain can't be empty";
        }
        if (!domain.getFullDomainName().matches(DOMAIN_NAME_PATTERN)) {
            return "Invalid domain name";
        }

        if (!domainService.isDomainNameNotTaken(domain)) {
            return "Domain name already taken.";
        }

        return null;
    }

}
