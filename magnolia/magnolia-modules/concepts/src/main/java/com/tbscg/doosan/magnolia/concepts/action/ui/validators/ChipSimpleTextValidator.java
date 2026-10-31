package com.tbscg.doosan.magnolia.concepts.action.ui.validators;

import com.vaadin.ui.Notification;
import info.magnolia.ui.form.EditorValidator;
import info.magnolia.ui.vaadin.integration.jcr.JcrNodeAdapter;

public class ChipSimpleTextValidator implements EditorValidator {
    protected final JcrNodeAdapter item;
    private final EditorValidator validator;

    public ChipSimpleTextValidator(JcrNodeAdapter item, EditorValidator validator) {
        this.item = item;
        this.validator = validator;
    }

    @Override
    public void showValidation(boolean b) {
        validator.showValidation(b);
    }

    @Override
    public boolean isValid() {
        if (isChipConcept(item) && !isSimpleTextField(item)) {
            Notification.show("You can only use simple text in chips. Please reopen the concept editor", Notification.Type.TRAY_NOTIFICATION).setDelayMsec(3600);
            return false;
        }
        return true;
    }

    private boolean isChipConcept(JcrNodeAdapter item) {
        try {
            String chipProperty = item.getItemProperty("chip").getValue().toString();
            return chipProperty.equals("color");
        } catch (Exception e) {
            return false;
        }
    }

    private boolean isSimpleTextField(JcrNodeAdapter item) {
        try {
            String simpleTextFieldProperty = item.getItemProperty("value").getValue().toString();
            return simpleTextFieldProperty.equals("PlainText");
        } catch (Exception e) {
            return false;
        }
    }
}
