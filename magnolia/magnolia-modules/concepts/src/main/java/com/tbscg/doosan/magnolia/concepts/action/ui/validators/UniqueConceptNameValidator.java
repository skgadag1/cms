package com.tbscg.doosan.magnolia.concepts.action.ui.validators;

import com.vaadin.ui.Notification;
import info.magnolia.ui.form.EditorValidator;
import info.magnolia.ui.vaadin.integration.jcr.JcrNodeAdapter;
import com.tbscg.doosan.magnolia.dxp.core.util.NodeUtil;
import lombok.extern.slf4j.Slf4j;
import javax.jcr.Node;

@Slf4j
public class UniqueConceptNameValidator implements EditorValidator {

    private static final String PROPERTY_NAME = "jcrName";
    private static final String NODE_TYPE = "mgnl:content";

    protected final JcrNodeAdapter item;
    private final EditorValidator validator;

    public UniqueConceptNameValidator(JcrNodeAdapter item, EditorValidator validator) {
        this.item = item;
        this.validator = validator;
    }

    @Override
    public void showValidation(boolean b) {
        validator.showValidation(b);
    }

    @Override
    public boolean isValid() {
        Node node = item.getJcrItem();
        String name = item.getItemProperty(PROPERTY_NAME).getValue().toString();
        if (!NodeUtil.isNodePropertyUniqueInParent(node, PROPERTY_NAME, name, NODE_TYPE, false)) {
            Notification.show("Concept with that name already exists.", Notification.Type.TRAY_NOTIFICATION).setDelayMsec(3600);
            return false;
        }
        return true;
    }
}
