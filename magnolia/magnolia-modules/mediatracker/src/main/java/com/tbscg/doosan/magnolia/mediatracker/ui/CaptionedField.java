package com.tbscg.doosan.magnolia.mediatracker.ui;

import com.vaadin.ui.Alignment;
import com.vaadin.ui.Component;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;

public class CaptionedField extends HorizontalLayout {

    public CaptionedField(String caption, Component field) {
        setSpacing(true);
        setDefaultComponentAlignment(Alignment.MIDDLE_LEFT);
        addComponent(new Label(caption));
        addComponent(field);
    }
}
