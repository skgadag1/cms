package com.tbscg.doosan.magnolia.pmd.ui;

import com.vaadin.ui.Alignment;
import com.vaadin.ui.Component;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;

public class ButtonBar extends HorizontalLayout {

    public ButtonBar(Component... buttons) {
        this("", buttons);
    }

    public ButtonBar(String title, Component... buttons) {
        super();
        setWidthFull();
        setDefaultComponentAlignment(Alignment.MIDDLE_CENTER);
        Label titleLabel = new Label(title);
        titleLabel.addStyleName("heading-3");
        addComponentsAndExpand(titleLabel);
        addComponents(buttons);
    }
}
