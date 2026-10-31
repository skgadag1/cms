package com.tbscg.doosan.magnolia.bobcat.customs.lock;

import info.magnolia.context.MgnlContext;
import info.magnolia.event.EventBus;
import info.magnolia.locking.api.ContentLockingManager;
import info.magnolia.ui.api.action.AbstractAction;
import info.magnolia.ui.api.action.ActionExecutionException;
import info.magnolia.ui.api.action.ConfiguredActionDefinition;
import info.magnolia.ui.api.context.UiContext;
import info.magnolia.ui.api.event.ContentChangedEvent;
import info.magnolia.ui.vaadin.integration.jcr.JcrItemAdapter;
import info.magnolia.ui.vaadin.overlay.MessageStyleTypeEnum;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Named;
import javax.jcr.Node;
import javax.jcr.RepositoryException;

public class BobcatUnlockItemAction<D extends BobcatUnlockItemAction.Definition> extends AbstractAction<D> {
    private static final Logger log = LoggerFactory.getLogger(BobcatUnlockItemAction.class);
    private final JcrItemAdapter item;
    private final UiContext uiContext;
    private final EventBus eventBus;
    private final ContentLockingManager lockingManager;


    @Inject
    public BobcatUnlockItemAction(D definition, JcrItemAdapter item, UiContext uiContext, @Named("admincentral") EventBus eventBus, BobcatContentLockingManager lockingManager) {
        super(definition);
        this.item = item;
        this.uiContext = uiContext;
        this.eventBus = eventBus;
        this.lockingManager = lockingManager;
    }

    public void execute() throws ActionExecutionException {
        try {
            Node node = (Node) this.item.getJcrItem();
            if (this.lockingManager.isParentLocked(node)) {
                this.uiContext.openAlert(MessageStyleTypeEnum.WARNING, "Parental lock", "This item is locked by it's parent.", "OK", () -> {
                });
            } else {
                this.lockingManager.unlock(node, MgnlContext.getUser().getName());
                this.uiContext.openNotification(MessageStyleTypeEnum.INFO, true, "Item unlocked");
                this.eventBus.fireEvent(new ContentChangedEvent(this.item.getItemId()));
            }

        } catch (RepositoryException e) {
            log.error(e.getMessage());
            throw new ActionExecutionException("Unable to unlock item.");
        }
    }

    public static class Definition extends ConfiguredActionDefinition {
        public Definition() {
            this.setImplementationClass(BobcatUnlockItemAction.class);
        }
    }
}
