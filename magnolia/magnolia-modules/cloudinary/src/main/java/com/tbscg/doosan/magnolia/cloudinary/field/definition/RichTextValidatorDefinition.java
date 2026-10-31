package com.tbscg.doosan.magnolia.cloudinary.field.definition;

import com.tbscg.doosan.magnolia.cloudinary.field.factory.RichTextValidatorFactory;
import info.magnolia.ui.field.SafeHtmlValidatorDefinition;

public class RichTextValidatorDefinition extends SafeHtmlValidatorDefinition {
    private boolean turnOn = true;

    public RichTextValidatorDefinition() {
        this.setFactoryClass(RichTextValidatorFactory.class);
    }

    public boolean isTurnOn() {
        return turnOn;
    }

    public void setTurnOn(boolean turnOn) {
        this.turnOn = turnOn;
    }
}
