package com.tbscg.doosan.magnolia.concepts.action.ui.validators;

import com.tbscg.doosan.magnolia.dxp.core.util.NodeUtil;
import com.vaadin.ui.Notification;
import info.magnolia.jcr.util.NodeTypes;
import info.magnolia.ui.form.EditorValidator;
import info.magnolia.ui.vaadin.integration.jcr.JcrNodeAdapter;

import javax.jcr.Node;
import javax.jcr.RepositoryException;

public class ProhibitToChangeNameValidator implements EditorValidator {
    protected final JcrNodeAdapter item;
    private final EditorValidator validator;

    public ProhibitToChangeNameValidator(JcrNodeAdapter item, EditorValidator validator) {
        this.item = item;
        this.validator = validator;
    }

    @Override
    public void showValidation(boolean b) {
        validator.showValidation(b);
    }

    @Override
    public boolean isValid() {
        String name = item.getNodeName();
        try {
            if (isBobcatPath() && !name.startsWith("untitled") && isNameChanged(name)) {
                Notification.show("You cannot edit the name of an existing concept.", Notification.Type.TRAY_NOTIFICATION).setDelayMsec(3600);
                return false;
            } else if(isPublished() && !name.startsWith("untitled") && isNameChanged(name)) {
                Notification.show("You cannot edit the name of an published concept.", Notification.Type.TRAY_NOTIFICATION).setDelayMsec(3600);
                return false;
            }
        } catch (RepositoryException e) {
            throw new RuntimeException(e);
        }
        return true;
    }

    private boolean isPublished() throws RepositoryException {
        Node nodeItem = item.getJcrItem();
        return NodeTypes.Activatable.isActivated(nodeItem);
    }

    public boolean isNameChanged(String name) throws RepositoryException {
        String newName = item.getItemProperty("jcrName").getValue().toString();

        return !name.equals(newName);
    }

    private boolean isBobcatPath(){
        Node node = item.getJcrItem();
        String path = NodeUtil.getPath(node);
        return path.startsWith("/bobcat");
    }
}
